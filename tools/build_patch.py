#!/usr/bin/env python3
"""Build a client and server update from two complete M1 archives."""
import argparse
from io import BytesIO
import json
from pathlib import Path
import tempfile
from zipfile import ZipFile

from build_release import ROOT, bundle_basename, json_bytes, sha, zip_entries


def read_overrides(archive, side, lock):
    if side == 'server':
        prefix = 'server/overrides/'
        return {name.removeprefix(prefix): archive.read(name)
                for name in archive.namelist() if name.startswith(prefix)}
    profile = f'client/{bundle_basename(lock)}-CurseForge.zip'
    with ZipFile(BytesIO(archive.read(profile))) as client:
        prefix = 'overrides/'
        return {name.removeprefix(prefix): client.read(name)
                for name in client.namelist() if name.startswith(prefix)}


def side_mods(lock, side):
    return {item['file_name']: item for item in lock['files'] + lock['extra_files']
            if item['side'] in {'both', side}}


def build_patch(previous, current, output):
    with ZipFile(previous) as before, ZipFile(current) as after:
        old_lock = json.loads(before.read('server/mods.lock.json'))
        new_lock = json.loads(after.read('server/mods.lock.json'))
        old_version, version = [lock['pack']['version'] for lock in (old_lock, new_lock)]
        old_mods = {item['file_name']: item for item in old_lock['files']}
        downloads = [item for item in new_lock['files']
                     if old_mods.get(item['file_name'], {}).get('sha256') != item['sha256']]
        plan = {'version': version, 'shared': [], 'downloads': downloads}
        payload = {
            'Update-Pack.ps1': (ROOT / 'tools/Apply-Pack-Update.ps1').read_bytes(),
            'Common.ps1': after.read('server/Common.ps1'),
            'README_RU.md': after.read(f'docs/UPDATE_{version}_RU.md'),
        }
        deltas = {}
        for side in ('client', 'server'):
            old, new = [read_overrides(archive, side, lock)
                        for archive, lock in ((before, old_lock), (after, new_lock))]
            old_side, new_side = side_mods(old_lock, side), side_mods(new_lock, side)
            prefix = 'm1packclient-' if side == 'client' else 'm1packcompat-'
            retired = sorted(old_side[name].get('path', 'mods/' + name)
                             for name in set(old_side) - set(new_side))
            removed = set(old) - set(new)
            if removed - set(retired):
                raise ValueError(f'Update requires removing non-mod files: {removed}')
            deltas[side] = {path: data for path, data in new.items() if old.get(path) != data}
            plan[side] = {
                'baseline': next(name for name in old_side if name.startswith(prefix)),
                'replacement': next(name for name in new_side if name.startswith(prefix)),
                'retire': retired,
                'files': [],
            }

        for path, data in sorted(deltas['client'].items()):
            if deltas['server'].get(path) == data:
                source = 'payload/shared/' + path
                plan['shared'].append({'target': path, 'source': source, 'sha256': sha(data)})
                payload[source] = data
                deltas['server'].pop(path)
        shared_paths = {file['target'] for file in plan['shared']}
        for side, delta in deltas.items():
            for path, data in sorted(delta.items()):
                if path in shared_paths:
                    continue
                source = f'payload/{side}/{path}'
                plan[side]['files'].append({'target': path, 'source': source, 'sha256': sha(data)})
                payload[source] = data
        payload['patch.json'] = json_bytes(plan)

    output.mkdir(parents=True, exist_ok=True)
    result = output / f'M1-SciFi-Patch-{old_version}-to-{version}.zip'
    with tempfile.TemporaryDirectory(dir=output) as folder:
        staged = Path(folder) / result.name
        zip_entries(staged, payload)
        with ZipFile(staged) as archive:
            if archive.testzip() is not None:
                raise ValueError('Update archive failed its CRC check')
        staged.replace(result)
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('previous', type=Path)
    parser.add_argument('current', type=Path)
    parser.add_argument('--output', type=Path, default=ROOT / 'dist')
    args = parser.parse_args()
    print(build_patch(args.previous, args.current, args.output))
