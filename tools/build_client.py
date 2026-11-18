"""Build the physical-client menu addon against the installed NeoForge client."""
import os
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
classes = ROOT / 'work/client-classes'
classes.mkdir(parents=True, exist_ok=True)
libraries = sorted((ROOT / 'work/client-test/libraries').rglob('*.jar'))
sources = sorted((ROOT / 'client/src').rglob('*.java'))
subprocess.run(['java', '-m', 'jdk.compiler/com.sun.tools.javac.Main', '-source', '21',
                '-target', '21', '-proc:none', '-cp', os.pathsep.join(map(str, libraries)),
                '-d', str(classes), *map(str, sources)], check=True)
out = ROOT / 'client-overrides/mods/m1packclient-0.3.1.jar'
out.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as archive:
    archive.write(ROOT / 'compat/LICENSE', 'META-INF/LICENSE')
    for base in (classes, ROOT / 'client/resources'):
        for file in sorted(base.rglob('*')):
            if file.is_file():
                archive.write(file, file.relative_to(base).as_posix())
print(out, out.stat().st_size)
