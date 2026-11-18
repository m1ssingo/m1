#!/usr/bin/env python3
"""Compile the small compatibility addon against the pinned server libraries."""
import hashlib
import json
import os
import shutil
import subprocess
import tomllib
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
classes=ROOT/'work/compat-classes'
if classes.exists():shutil.rmtree(classes)
classes.mkdir(parents=True)
libraries=list((ROOT/'work/server-test/libraries').rglob('*.jar'))
libraries+=list((ROOT/'work/jars').glob('*.jar'))
classpath=os.pathsep.join(str(p) for p in libraries)
sources=sorted((ROOT/'compat/src').rglob('*.java'))
args=['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','21','-target','21','-proc:none',
      '-classpath',classpath,'-d',str(classes)]+[str(p) for p in sources]
subprocess.run(args,check=True)
metadata = tomllib.loads((ROOT / 'compat/resources/META-INF/neoforge.mods.toml').read_text())
version = metadata['mods'][0]['version']
out = ROOT / 'overrides/mods' / f'm1packcompat-{version}.jar'
out.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
    z.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\n\n')
    z.write(ROOT/'compat/LICENSE','META-INF/LICENSE')
    for base in [classes,ROOT/'compat/resources']:
        for p in sorted(base.rglob('*')):
            if p.is_file():z.write(p,p.relative_to(base).as_posix())
print(json.dumps({'file':out.name,'bytes':out.stat().st_size,
                 'sha256':hashlib.sha256(out.read_bytes()).hexdigest()}))
