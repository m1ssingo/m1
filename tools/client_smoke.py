"""Launch the pinned graphical client in a disposable Xorg display for a smoke test."""
import json
import ctypes
import ctypes.util
import math
import os
from pathlib import Path
import re
import subprocess
import time
from PIL import Image, ImageChops

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
work = ROOT / 'work'
log_path = work / 'client-smoke.log'


def wait_for(process, pattern, offset=0, timeout=45):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        content = log_path.read_text(errors='replace')
        match = re.search(pattern, content[offset:])
        if match:
            return match
        assert process.poll() is None, f'Client exited, see {log_path}'
        time.sleep(0.2)
    raise AssertionError(f'Missing {pattern}, see {log_path}')


def layout(process, name, count, offset=0):
    pattern = r'\[M1-' + name + r'-LAYOUT\]' + r' (\d+)' * count
    return tuple(int(value) for value in wait_for(process, pattern, offset).groups())


class GameWindow:
    def __init__(self):
        self.x11 = ctypes.CDLL(ctypes.util.find_library('X11'))
        self.xtest = ctypes.CDLL(ctypes.util.find_library('Xtst'))
        self.x11.XOpenDisplay.argtypes = [ctypes.c_char_p]
        self.x11.XOpenDisplay.restype = ctypes.c_void_p
        self.x11.XFlush.argtypes = [ctypes.c_void_p]
        self.x11.XCloseDisplay.argtypes = [ctypes.c_void_p]
        self.x11.XMoveResizeWindow.argtypes = [
            ctypes.c_void_p, ctypes.c_ulong, ctypes.c_int, ctypes.c_int, ctypes.c_uint, ctypes.c_uint
        ]
        self.xtest.XTestFakeMotionEvent.argtypes = [
            ctypes.c_void_p, ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_ulong
        ]
        self.xtest.XTestFakeButtonEvent.argtypes = [
            ctypes.c_void_p, ctypes.c_uint, ctypes.c_int, ctypes.c_ulong
        ]
        self.xtest.XTestFakeKeyEvent.argtypes = [
            ctypes.c_void_p, ctypes.c_uint, ctypes.c_int, ctypes.c_ulong
        ]
        self.connection = self.x11.XOpenDisplay(env['DISPLAY'].encode())
        assert self.connection
        tree = subprocess.check_output(['xwininfo', '-root', '-children'], env=env, text=True)
        window = re.search(r'(0x[0-9a-f]+) "Minecraft[^"]*"', tree)
        assert window, tree
        self.id = int(window.group(1), 16)

    def resize(self, width, height):
        self.x11.XMoveResizeWindow(self.connection, self.id, 0, 0, width, height)
        self.x11.XFlush(self.connection)

    def capture(self, name):
        subprocess.run(['import', '-window', hex(self.id), str(work / f'{name}.png')], env=env, check=True)

    def click(self, x, y, gui_width, gui_height, width, height):
        pixel_x, pixel_y = round(x * width / gui_width), round(y * height / gui_height)
        self.xtest.XTestFakeMotionEvent(self.connection, -1, pixel_x, pixel_y, 0)
        self.xtest.XTestFakeButtonEvent(self.connection, 1, 1, 0)
        self.xtest.XTestFakeButtonEvent(self.connection, 1, 0, 0)
        self.x11.XFlush(self.connection)

    def escape(self):
        self.xtest.XTestFakeKeyEvent(self.connection, 9, 1, 0)
        self.xtest.XTestFakeKeyEvent(self.connection, 9, 0, 0)
        self.x11.XFlush(self.connection)

    def close(self):
        self.x11.XCloseDisplay(self.connection)


def log_offset():
    return len(log_path.read_text(errors='replace'))


def check_screens(process, game, width, height, suffix, resize=False):
    offset = log_offset() if resize else 0
    if resize:
        game.resize(width, height)
    menu = layout(process, 'MENU', 12, offset)
    gw, gh, ax, ay, aw, ah, left, right, top, bw, bh, pitch = menu
    assert abs(aw - ah * 1672 / 941) <= 2, menu
    assert (gw, gh) in {(math.ceil(width / scale), math.ceil(height / scale))
                        for scale in range(1, 5)}, menu
    # The title/byline ends above 0.66h, with the foreground characters outside this center area.
    assert ax + aw * 0.26 <= left - 1 < left + bw < right, menu
    assert right + bw + 1 <= ax + aw * 0.74, menu
    assert ay + ah * 0.66 < top - 1 < top + bh < top + pitch, menu
    assert top + 2 * pitch + bh + 1 <= ay + ah * 0.97, menu
    if not resize:
        wait_for(process, r'\[M1-ANIMATION\] 1672x941 200 frames every 80ms')
        wait_for(process, r'\[M1-MENU\] menu music is playing')
        wait_for(process, r'\[M1-ANIMATION\] loop')
    time.sleep(0.5)
    game.capture('menu-live' + suffix)
    if not resize:
        time.sleep(1.3)
        game.capture('menu-motion-live')
        with Image.open(work / 'menu-live.png') as first, Image.open(work / 'menu-motion-live.png') as second:
            difference = ImageChops.difference(first.convert('RGB'), second.convert('RGB'))
            assert difference.crop((1010, 34, 1330, 216)).getbbox(), 'Station is static'
            assert difference.crop((150, 295, 435, 390)).getbbox(), 'Tower smoke is static'
            assert difference.crop((1460, 210, 1505, 370)).getbbox() is None, 'Rocket moved'

    offset = log_offset()
    game.click(left + bw / 2, top + pitch + bh / 2, gw, gh, width, height)
    options = layout(process, 'OPTIONS', 6, offset)
    ow, oh, openfm, done, y, footer_width = options
    assert (ow, oh) == (gw, gh), options
    assert 0 <= openfm < openfm + footer_width < done <= ow - footer_width, options
    assert 0 <= y <= oh - 20, options
    time.sleep(0.5)
    game.capture('options-live' + suffix)

    offset = log_offset()
    game.click(openfm + footer_width / 2, y + 10, gw, gh, width, height)
    wait_for(process, r'\[M1-SCREEN\] OpenFMAudioSettingsScreen', offset)
    time.sleep(0.5)
    game.capture('openfm-live' + suffix)
    offset = log_offset()
    game.escape()
    assert layout(process, 'OPTIONS', 6, offset) == options
    offset = log_offset()
    game.click(done + footer_width / 2, y + 10, gw, gh, width, height)
    layout(process, 'MENU', 12, offset)

    offset = log_offset()
    game.click(left + bw / 2, top + 2 * pitch + bh / 2, gw, gh, width, height)
    wait_for(process, r'\[M1-SCREEN\] SoundOptionsScreen', offset)
    time.sleep(0.5)
    game.capture('sound-live' + suffix)
    offset = log_offset()
    game.escape()
    layout(process, 'MENU', 12, offset)
    print(f'MENU_UI_PASSED {width}x{height}, GUI {gw}x{gh}', flush=True)


with (work / 'client-xorg-output.log').open('w') as xlog, log_path.open('w') as log:
    display = subprocess.Popen(['Xorg', f':{display_number}', '-config', str(work / 'xorg-test.conf'),
        '-noreset', '-nolisten', 'unix', '-nolisten', 'local', '-listen', 'tcp', '-ac',
        '-logfile', str(work / 'xorg.log')], stdout=xlog, stderr=subprocess.STDOUT)
    process = None
    try:
        time.sleep(2)
        assert display.poll() is None, 'Xorg did not start'
        subprocess.run(['xdpyinfo', '-display', env['DISPLAY']], stdout=subprocess.DEVNULL, check=True)
        process = subprocess.Popen(java, cwd=GAME, env=env, stdout=log, stderr=subprocess.STDOUT)
        wait_for(process, r'\[M1-MENU-LAYOUT\]', timeout=300)
        game = GameWindow()
        try:
            check_screens(process, game, 1672, 941, '')
            check_screens(process, game, 960, 720, '-4x3', resize=True)
        finally:
            game.close()
    finally:
        if process is not None and process.poll() is None:
            process.terminate()
            try:
                process.wait(timeout=20)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
        display.terminate()
        display.wait(timeout=10)
