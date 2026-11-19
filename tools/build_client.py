"""Build the client menu addon against the installed NeoForge client."""
import argparse
import gzip
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import tomllib
import zipfile

from PIL import Image

from pack_menu_animation import HEADER, HEIGHT, WIDTH, pack

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
    parser.add_argument('--animation', type=Path, default=ROOT / 'work/M1-menu-animated-v4.gif')
    parser.add_argument('--libraries', type=Path, default=ROOT / 'work/client-test/libraries')
    parser.add_argument('--java', default='java')
    args = parser.parse_args()

    priority = {'neoforge-21.1.267-client.jar': 0, 'client-1.21.1-20240808.144430-srg.jar': 1}
    libraries = sorted((path.resolve() for path in args.libraries.rglob('*.jar') if zipfile.is_zipfile(path)),
                       key=lambda path: (priority.get(path.name, 2), path))
    sources = sorted((ROOT / 'client/src').rglob('*.java'))
    metadata = tomllib.loads((RESOURCES / 'META-INF/neoforge.mods.toml').read_text(encoding='utf-8'))
    version = metadata['mods'][0]['version']
    out = ROOT / f'client-overrides/mods/m1packclient-{version}.jar'
    out.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=out.parent) as temp:
        staging = Path(temp)
        classes = staging / 'classes'
        classes.mkdir()
        compiler_args = ['-encoding', 'UTF-8', '-source', '21', '-target', '21', '-proc:none',
                         '-cp', os.pathsep.join(map(str, libraries)), '-d', str(classes), *map(str, sources)]
        argfile = staging / 'javac.args'
        argfile.write_text('\n'.join('"' + arg.replace('\\', '/').replace('"', '\\"') + '"'
                                     for arg in compiler_args), encoding='utf-8')
        subprocess.run([args.java, '-m', 'jdk.compiler/com.sun.tools.javac.Main', '@' + str(argfile)], check=True)
        animation = staging / 'background.m1a'
        fallback = staging / 'background.png'
        if args.animation.suffix.lower() == '.m1a':
            with gzip.open(args.animation, 'rb') as stream:
                if stream.read(len(HEADER)) != HEADER:
                    raise ValueError('Unexpected packed menu animation header')
            original_fallback = args.animation.with_suffix('.png')
            with Image.open(original_fallback) as image:
                if image.size != (WIDTH, HEIGHT) or image.format != 'PNG':
                    raise ValueError('Expected the packed animation’s first frame as a PNG next to it')
            shutil.copyfile(args.animation, animation)
            shutil.copyfile(original_fallback, fallback)
        else:
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
