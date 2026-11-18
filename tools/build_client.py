"""Build the client menu addon against the installed NeoForge client."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import tomllib
import zipfile

from pack_menu_animation import pack

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / 'client/resources'
MENU = 'assets/m1packclient/textures/menu'


def add_file(archive, file, name, compression=zipfile.ZIP_DEFLATED):
    info = zipfile.ZipInfo(name)
    info.compress_type = compression
    with file.open('rb') as source, archive.open(info, 'w') as target:
        shutil.copyfileobj(source, target)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--animation', type=Path, default=ROOT / 'work/M1-menu-animation-v2.webp')
    args = parser.parse_args()

    libraries = sorted((ROOT / 'work/client-test/libraries').rglob('*.jar'))
    sources = sorted((ROOT / 'client/src').rglob('*.java'))
    metadata = tomllib.loads((RESOURCES / 'META-INF/neoforge.mods.toml').read_text())
    version = metadata['mods'][0]['version']
    out = ROOT / f'client-overrides/mods/m1packclient-{version}.jar'
    out.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=out.parent) as temp:
        staging = Path(temp)
        classes = staging / 'classes'
        classes.mkdir()
        subprocess.run(['java', '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-source', '21',
                        '-target', '21', '-proc:none', '-cp', os.pathsep.join(map(str, libraries)),
                        '-d', str(classes), *map(str, sources)], check=True)
        animation = staging / 'background.m1a'
        fallback = staging / 'background.png'
        pack(args.animation, animation, fallback)

        jar = staging / out.name
        with zipfile.ZipFile(jar, 'w') as archive:
            add_file(archive, ROOT / 'compat/LICENSE', 'META-INF/LICENSE')
            for base in (classes, RESOURCES):
                for file in sorted(base.rglob('*')):
                    name = file.relative_to(base).as_posix()
                    if file.is_file() and name not in {f'{MENU}/background.png', f'{MENU}/background.m1a'}:
                        add_file(archive, file, name)
            add_file(archive, fallback, f'{MENU}/background.png', zipfile.ZIP_STORED)
            add_file(archive, animation, f'{MENU}/background.m1a', zipfile.ZIP_STORED)
        jar.replace(out)
    print(out, out.stat().st_size)


if __name__ == '__main__':
    main()
