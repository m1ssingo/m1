#!/usr/bin/env python3
"""Compile the small compatibility addon against the pinned server libraries."""
import hashlib
import json
import os
import argparse
import subprocess
import tempfile
import tomllib
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--libraries', type=Path, default=ROOT / 'work/server-test/libraries')
    parser.add_argument('--mods', type=Path, default=ROOT / 'work/jars')
    parser.add_argument('--java', default='java')
    options = parser.parse_args()
    priority = {'neoforge-21.1.267-server.jar': 0, 'server-1.21.1-20240808.144430-srg.jar': 1}
    libraries = [*options.libraries.rglob('*.jar'), *options.mods.glob('*.jar')]
    libraries = sorted((path.resolve() for path in libraries if zipfile.is_zipfile(path)),
                       key=lambda path: (priority.get(path.name, 2), path))
    sources = sorted((ROOT / 'compat/src').rglob('*.java'))
    resources = ROOT / 'compat/resources'
    metadata = tomllib.loads((resources / 'META-INF/neoforge.mods.toml').read_text(encoding='utf-8'))
    version = metadata['mods'][0]['version']
    out = ROOT / 'overrides/mods' / f'm1packcompat-{version}.jar'
    out.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=out.parent) as temp:
        staging = Path(temp)
        classes = staging / 'classes'
        classes.mkdir()
        args = ['-encoding', 'UTF-8', '-source', '21', '-target', '21', '-proc:none',
                '-classpath', os.pathsep.join(map(str, libraries)), '-d', str(classes), *map(str, sources)]
        argfile = staging / 'javac.args'
        argfile.write_text('\n'.join('"' + arg.replace('\\', '/').replace('"', '\\"') + '"'
                                     for arg in args), encoding='utf-8')
        subprocess.run([options.java, '-m', 'jdk.compiler/com.sun.tools.javac.Main', '@' + str(argfile)], check=True)
        jar = staging / out.name
        with zipfile.ZipFile(jar, 'w', zipfile.ZIP_DEFLATED) as archive:
            archive.writestr('META-INF/MANIFEST.MF', 'Manifest-Version: 1.0\n\n')
            archive.write(ROOT / 'compat/LICENSE', 'META-INF/LICENSE')
            for base in (classes, resources):
                for path in sorted(base.rglob('*')):
                    if path.is_file():
                        archive.write(path, path.relative_to(base).as_posix())
        jar.replace(out)
    print(json.dumps({'file': out.name, 'bytes': out.stat().st_size,
                      'sha256': hashlib.sha256(out.read_bytes()).hexdigest()}))


if __name__ == '__main__':
    main()
