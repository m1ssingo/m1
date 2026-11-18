"""Launch the pinned graphical client in a disposable Xorg display for a smoke test."""
import json
import ctypes
import ctypes.util
import os
from pathlib import Path
import re
import subprocess
import time
import zipfile

ROOT = Path(__file__).resolve().parents[1]
GAME = ROOT / 'work/client-test'
vanilla = json.loads((GAME / 'versions/1.21.1/1.21.1.json').read_text())
neoforge = json.loads((GAME / 'versions/neoforge-21.1.267/neoforge-21.1.267.json').read_text())

def allowed(entry):
    rules = entry.get('rules')
    if rules is None:
        return True
    result = False
    for rule in rules:
        target = rule.get('os', {})
        if target.get('name', 'linux') == 'linux' and not rule.get('features'):
            result = rule['action'] == 'allow'
    return result

libraries = {}
for entry in [*vanilla['libraries'], *neoforge['libraries']]:
    if allowed(entry):
        libraries[entry['name'].split(':')[0] + ':' + entry['name'].split(':')[1]
                  + ':' + ':'.join(entry['name'].split(':')[3:])] = entry
classpath = []
for entry in libraries.values():
    artifact = entry.get('downloads', {}).get('artifact')
    if artifact:
        file = GAME / 'libraries' / artifact['path']
        assert file.is_file(), file
        classpath.append(str(file))
variables = {
    'library_directory': str(GAME / 'libraries'), 'classpath_separator': os.pathsep,
    'version_name': neoforge['id'], 'classpath': os.pathsep.join(classpath),
    'natives_directory': str(GAME / 'natives'), 'launcher_name': 'M1LocalTest',
    'launcher_version': '1'
}

def expand(value):
    for key, text in variables.items():
        value = value.replace('${' + key + '}', text)
    return value

jvm = []
for entry in [*vanilla['arguments']['jvm'], *neoforge['arguments']['jvm']]:
    if isinstance(entry, str):
        jvm.append(expand(entry))
    elif allowed(entry):
        values = entry['value']
        jvm.extend(expand(v) for v in (values if isinstance(values, list) else [values]))
java = ['java', '-Xms1G', '-Xmx6G', '-XX:ActiveProcessorCount=4', '-Dm1pack.smoke=true', *jvm, neoforge['mainClass'],
        '--username', 'M1LocalTest', '--version', neoforge['id'], '--gameDir', str(GAME),
        '--assetsDir', str(GAME / 'assets'), '--assetIndex', vanilla['assetIndex']['id'],
        '--uuid', '00000000000000000000000000000001', '--accessToken', '0',
        '--userType', 'legacy', '--versionType', 'release', '--width', '1672', '--height', '941',
        *neoforge['arguments']['game']]
display_number = 140 + int(time.time()) % 1000
env = dict(os.environ, DISPLAY=f'127.0.0.1:{display_number}', LIBGL_ALWAYS_SOFTWARE='true', ALSOFT_DRIVERS='null')
with (ROOT / 'work/client-xorg-output.log').open('w') as xlog, (ROOT / 'work/client-smoke.log').open('w') as log:
    display = subprocess.Popen(['Xorg', f':{display_number}', '-config', str(ROOT / 'work/xorg-test.conf'),
        '-noreset', '-nolisten', 'unix', '-nolisten', 'local', '-listen', 'tcp', '-ac',
        '-logfile', str(ROOT / 'work/xorg.log')], stdout=xlog, stderr=subprocess.STDOUT)
    time.sleep(2)
    assert display.poll() is None, 'Xorg did not start'
    subprocess.run(['xdpyinfo', '-display', env['DISPLAY']], stdout=subprocess.DEVNULL, check=True)
    process = subprocess.Popen(java, cwd=GAME, env=env, stdout=log, stderr=subprocess.STDOUT)
    start = time.monotonic()
    try:
        while process.poll() is None and time.monotonic() - start < 300:
            time.sleep(5)
            content = (ROOT / 'work/client-smoke.log').read_text(errors='replace')
            if '[M1-KEYS]' in content:
                time.sleep(18)
                screenshot = ROOT / 'work/menu-live.png'
                subprocess.run(['import', '-display', env['DISPLAY'], '-window', 'root', str(screenshot)], check=True)
                print('MENU_CAPTURED', screenshot, flush=True)
                x11 = ctypes.CDLL(ctypes.util.find_library('X11'))
                xtest = ctypes.CDLL(ctypes.util.find_library('Xtst'))
                x11.XOpenDisplay.argtypes = [ctypes.c_char_p]
                x11.XOpenDisplay.restype = ctypes.c_void_p
                x11.XFlush.argtypes = [ctypes.c_void_p]
                xtest.XTestFakeMotionEvent.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_ulong]
                xtest.XTestFakeButtonEvent.argtypes = [ctypes.c_void_p, ctypes.c_uint, ctypes.c_int, ctypes.c_ulong]
                xtest.XTestFakeKeyEvent.argtypes = [ctypes.c_void_p, ctypes.c_uint, ctypes.c_int, ctypes.c_ulong]
                connection = x11.XOpenDisplay(env['DISPLAY'].encode())
                assert connection
                def click(x, y):
                    xtest.XTestFakeMotionEvent(connection, -1, x, y, 0)
                    xtest.XTestFakeButtonEvent(connection, 1, 1, 0)
                    xtest.XTestFakeButtonEvent(connection, 1, 0, 0)
                    x11.XFlush(connection)
                    time.sleep(3)
                def escape():
                    xtest.XTestFakeKeyEvent(connection, 9, 1, 0)
                    xtest.XTestFakeKeyEvent(connection, 9, 0, 0)
                    x11.XFlush(connection)
                    time.sleep(3)
                click(650, 750)
                subprocess.run(['import', '-display', env['DISPLAY'], '-window', 'root', str(ROOT / 'work/options-live.png')], check=True)
                escape()
                click(650, 800)
                subprocess.run(['import', '-display', env['DISPLAY'], '-window', 'root', str(ROOT / 'work/sound-live.png')], check=True)
                escape()
                time.sleep(3)
                break
        print('CLIENT_EXIT', process.poll(), flush=True)
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=20)
    finally:
        if process.poll() is None:
            process.kill()
        display.terminate()
        display.wait(timeout=10)
