#!/usr/bin/env python3
"""Build the CurseForge and dedicated-server kit from the pinned pack and own JARs."""
import argparse
import hashlib
import html
import io
import json
from pathlib import Path, PurePosixPath
import tempfile
from zipfile import ZIP_DEFLATED, ZIP_STORED, ZipFile

ROOT = Path(__file__).resolve().parents[1]


def sha(data):
    return hashlib.sha256(data).hexdigest()


def bundle_basename(lock):
    pack = lock['pack']
    return f"{pack['name'].replace(' ', '-')}-{pack['version']}"


def json_bytes(value):
    return (json.dumps(value, ensure_ascii=False, indent=2) + '\n').encode('utf-8')


def files_under(directory):
    return {
        path.relative_to(ROOT).as_posix(): path.read_bytes()
        for path in sorted((ROOT / directory).rglob('*'))
        if path.is_file() and not (set(path.relative_to(ROOT).parts) & {'__pycache__'})
        and path.suffix not in {'.jar', '.pyc', '.zip', '.ogg'}
    }


def zip_entries(stream, entries):
    with ZipFile(stream, 'w') as archive:
        for name, data in sorted(entries.items()):
            method = ZIP_STORED if Path(name).suffix in {'.jar', '.zip', '.png', '.ogg'} else ZIP_DEFLATED
            archive.writestr(name, data, compress_type=method)


def addon(entry, directory):
    path = ROOT / directory / entry['path']
    data = path.read_bytes()
    if sha(data) != entry['sha256']:
        raise ValueError(f'Unexpected SHA-256 for {path}')
    with ZipFile(io.BytesIO(data)) as archive:
        if archive.testzip() is not None:
            raise ValueError(f'Invalid mod archive {path}')
    return data


def build(output, verification):
    lock_data = (ROOT / 'mods.lock.json').read_bytes()
    lock = json.loads(lock_data)
    if lock['errors']:
        raise ValueError('The mod lock contains unresolved errors')
    version = lock['pack']['version']
    name = bundle_basename(lock)
    client_name = name + '-CurseForge.zip'
    files = lock['files']
    projects = {entry['project_id'] for entry in files}
    assert len(projects) == len(files)
    assert all(d['project_id'] in projects for entry in files for d in entry['required_dependencies'])
    client_files = [entry for entry in files if entry['side'] != 'server']
    server_files = [entry for entry in files if entry['side'] != 'client']
    common = files_under('overrides')
    client_source = files_under('client-overrides')
    clients = {'overrides/' + path.removeprefix('client-overrides/'): data
               for path, data in client_source.items()}
    for entry in lock['extra_files']:
        path = 'overrides/' + entry['path']
        if entry['side'] == 'client':
            clients[path] = addon(entry, 'client-overrides')
        else:
            common[path] = addon(entry, 'overrides')

    manifest = {
        'minecraft': {'version': lock['minecraft'],
                      'modLoaders': [{'id': 'neoforge-' + lock['neoforge'], 'primary': True}]},
        'manifestType': 'minecraftModpack',
        'manifestVersion': 1,
        'name': lock['pack']['name'],
        'version': version,
        'author': 'M1ssing0',
        'files': [{'projectID': entry['project_id'], 'fileID': entry['file_id'], 'required': True}
                  for entry in client_files],
        'overrides': 'overrides',
    }
    modlist = '<!doctype html><meta charset="utf-8"><title>M1 SciFi mods</title><ul>' + ''.join(
        '<li><a href="' + html.escape(entry['source'], quote=True) + '">' + html.escape(entry['slug']) + '</a></li>'
        for entry in client_files
    ) + '</ul>'
    profile = io.BytesIO()
    zip_entries(profile, {'manifest.json': json_bytes(manifest), 'modlist.html': modlist.encode('utf-8'),
                          **common, **clients})

    server_template = files_under('server-template')
    server = {'server/' + key.removeprefix('server-template/'): value for key, value in server_template.items()}
    server.update({'server/' + key: value for key, value in common.items()})
    server['server/mods.lock.json'] = lock_data
    docs = files_under('docs')
    summary = [f'# Зафиксированный состав {version}', '',
               f'Файлов CurseForge на клиенте: {len(client_files)}. На сервере: {len(server_files)}.',
               'Собственные моды входят в комплект согласно указанной стороне.', '',
               '| Проект | Зафиксированный файл | Сторона |', '|---|---|---|']
    for entry in files:
        summary.append(f'| [{entry["slug"]}]({entry["source"]}) | `{entry["file_name"]}` | {entry["side"]} |')
    for entry in lock['extra_files']:
        summary.append(f'| M1 | `{entry["file_name"]}` | {entry["side"]} |')
    docs['docs/MODS.md'] = ('\n'.join(summary) + '\n').encode('utf-8')
    code = {**server_template, **common, **client_source, **docs}
    for directory in ('compat', 'client', 'tools', 'testing'):
        code.update(files_under(directory))
    for filename in ('README.md', 'README_RU.md', 'SOURCE_RU.md', '.gitignore'):
        code[filename] = (ROOT / filename).read_bytes()
    code['mods.lock.json'] = lock_data
    source = {'source/' + key: value for key, value in code.items()}

    bundle = {'README_RU.md': code['README_RU.md'], 'client/' + client_name: profile.getvalue(),
              **server, **source, **docs}
    if verification is not None:
        evidence = json.loads(verification.read_bytes())
        if evidence['release'] != version:
            raise ValueError('Verification belongs to a different release')
        bundle['evidence/verification.json'] = json_bytes(evidence)
    sums = {key: sha(value) for key, value in bundle.items()}
    bundle['SHA256SUMS.txt'] = ''.join(f'{digest}  {key}\n' for key, digest in sorted(sums.items())).encode()

    output.mkdir(parents=True, exist_ok=True)
    result = output / (name + '.zip')
    with tempfile.TemporaryDirectory(dir=output) as temp:
        candidate = Path(temp) / result.name
        zip_entries(candidate, bundle)
        with ZipFile(candidate) as archive:
            assert archive.testzip() is None
            names = archive.namelist()
            assert len(names) == len(set(names))
            assert all(not PurePosixPath(path).is_absolute()
                       and not set(PurePosixPath(path).parts) & {'.git', 'world', 'logs', '..'} for path in names)
            assert all(sha(archive.read(path)) == digest for path, digest in sums.items())
            assert not any('m1packclient' in path for path in names if path.startswith('server/'))
            with ZipFile(io.BytesIO(archive.read('client/' + client_name))) as packed:
                assert packed.testzip() is None
                assert json.loads(packed.read('manifest.json')) == manifest
                assert set(packed.namelist()) == {'manifest.json', 'modlist.html', *common, *clients}
                for path in common:
                    assert packed.read(path) == archive.read('server/' + path)
        candidate.replace(result)
    with result.open('rb') as stream:
        digest = hashlib.file_digest(stream, 'sha256').hexdigest()
    print(f'{result}\n{result.stat().st_size} bytes | SHA-256 {digest}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'dist')
    parser.add_argument('--verification', type=Path)
    options = parser.parse_args()
    build(options.output, options.verification)
