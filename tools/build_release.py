#!/usr/bin/env python3
"""Package the pinned prototype, after verifying real server test results."""
import csv
import hashlib
import html
import io
import json
import re
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'dist'
VERSION = json.loads((ROOT / 'mods.lock.json').read_text())['pack']['version']
PACK = OUT / f'M1-SciFi-Prototype-{VERSION}'

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def copy(source, target):
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, target)

def zip_tree(source, target):
    with zipfile.ZipFile(target, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        for p in sorted(source.rglob('*')):
            if p.is_file():
                z.write(p, p.relative_to(source).as_posix())

def read_result(label):
    result = json.loads((ROOT / 'work' / f'result-{label}.json').read_text())
    assert result['ready'] and result['saved'] and result['stopped_cleanly']
    assert result['exit_code'] == 0 and not result['timeout']
    unexpected = [line for line in result['error_lines'] if 'yggdrasil public key' not in line]
    assert not unexpected, unexpected
    return result

def main():
    lock = json.loads((ROOT / 'mods.lock.json').read_text())
    assert not lock['errors']
    files = lock['files']
    assert len(files) == 57
    backups = next(f for f in files if f['slug'] == 'ftb-backups-3')
    assert backups['side'] == 'both', 'FTB Backups 3 requires matching client and server installation'
    ids = {f['project_id'] for f in files}
    for f in files:
        assert all(dep['project_id'] in ids for dep in f['required_dependencies']), f['slug']
        jar = ROOT / 'work/jars' / f['file_name']
        assert jar.stat().st_size == f['size'] and digest(jar) == f['sha256'], f['slug']
    own = lock['extra_files'][0]
    assert digest(ROOT / 'overrides' / own['path']) == own['sha256']
    client_own = next(file for file in lock['extra_files'] if file['side'] == 'client')
    client_overrides = ROOT / 'client-overrides'
    assert digest(client_overrides / client_own['path']) == client_own['sha256']
    # A local rebuild can leave the preceding client addon beside the locked version.
    client_override_files = [
        p for p in sorted(client_overrides.rglob('*'))
        if p.is_file() and (not p.match('mods/m1packclient-*.jar')
                            or p == client_overrides / client_own['path'])
    ]
    assert digest(ROOT / 'work/neoforge-21.1.267-installer.jar') == lock['neoforge_installer']['sha256']

    cycle = read_result('industry-first-boot')
    expected = [False, True, False, True]
    assert len(cycle['probe_results']) == 5
    cycle_checks = []
    keys = ['stage_unlocked', 'basic_circuit_recipe', 'controller_recipe',
            'basic_recipe_retained', 'controller_recipe_retained', 'total_recipes']
    probes = cycle['probe_results']
    for probe, unlocked in zip([probes[0], *probes[2:]], expected, strict=True):
        assert probe['stage_unlocked'] == probe['basic_circuit_recipe'] == probe['controller_recipe'] == unlocked
        assert probe['basic_recipe_retained'] and probe['controller_recipe_retained']
        cycle_checks.append({key: probe[key] for key in keys})

    before_quest_update = read_result('quest-book-before-update')
    assert before_quest_update['quest_book'] == {'mode': 'seed', 'counts': [9, 6], 'teams_saved': 2}
    industry = read_result('quest-book-after-update')
    assert 'industry' in industry and not industry.get('industry_error')
    quest_book = read_result('quest-book-final-data')['quest_book']
    assert quest_book == industry['quest_book']
    assert quest_book['counts'] == [16, 15, 12] and quest_book['optional'] == 20
    assert quest_book['assembler_advanced_circuits'] == 2 and quest_book['mode'] == 'verify'
    assert all(quest_book[key] is True for key in [
        'old_progress_preserved', 'claimed_reward_preserved', 'chemistry_parallel',
        'ntm_requires_chemistry', 'new_chapter_complete_without_optional',
    ])
    restart = read_result('industry-restart')
    assert len(restart['probe_results']) == 2, restart['probe_results']
    for probe in restart['probe_results']:
        assert all(probe[key] is True for key in keys[:5]), probe
    fixture = probes[1]['world_checks']
    fixture_checks = dict(re.findall(r'([a-z0-9_]+):([01])b', fixture))
    assert len(fixture_checks) == 12 and set(fixture_checks.values()) == {'1'}, fixture
    radio = json.loads((ROOT / 'work/radio-decoder-results.json').read_text())
    assert len(radio) == 2 and all(r['exit_code'] == 0 and r['decoded']['pcm_bytes'] > 0 for r in radio)

    if PACK.exists():
        shutil.rmtree(PACK)
    PACK.mkdir(parents=True)
    copy(ROOT / 'README_RU.md', PACK / 'README_RU.md')
    for name in ['TEST_PROTOCOL_RU.md', 'RADIO_RU.md', 'STATUS_RU.md', 'HOTFIX_0.1.1_RU.md',
                 'FIRST_INDUSTRY_RU.md', 'UPDATE_0.2.0_RU.md', 'UPDATE_0.3.0_RU.md', 'KEYS_RU.md',
                 'HOTFIX_0.3.1_RU.md', 'UPDATE_0.4.0_RU.md']:
        copy(ROOT / 'docs' / name, PACK / 'docs' / name)
    copy(ROOT / 'docs/minecraft_modpack_spec_v1.md', PACK / 'docs/minecraft_modpack_spec_v1.md')
    for p in (ROOT / 'server-template').iterdir():
        if p.is_file():
            copy(p, PACK / 'server' / p.name)
    copy(ROOT / 'mods.lock.json', PACK / 'server/mods.lock.json')
    shutil.copytree(ROOT / 'overrides', PACK / 'server/overrides')

    client_files = [f for f in files if f['side'] != 'server']
    server_files = [f for f in files if f['side'] != 'client']
    assert len(client_files) == 57 and len(server_files) == 53
    assert backups in client_files and backups in server_files
    manifest = {
        'minecraft': {'version': lock['minecraft'], 'modLoaders': [
            {'id': 'neoforge-' + lock['neoforge'], 'primary': True}]},
        'manifestType': 'minecraftModpack', 'manifestVersion': 1,
        'name': lock['pack']['name'], 'version': lock['pack']['version'],
        'author': 'M1ssing0',
        'files': [{'projectID': f['project_id'], 'fileID': f['file_id'], 'required': True}
                  for f in client_files], 'overrides': 'overrides'
    }
    client_path = PACK / f'client/M1-SciFi-Prototype-{VERSION}-CurseForge.zip'
    client_path.parent.mkdir(parents=True)
    modlist = '<!doctype html><meta charset="utf-8"><title>M1 Prototype mods</title><ul>' + ''.join(
        '<li><a href="' + html.escape(f['source']) + '">' + html.escape(f['display_name']) + '</a></li>'
        for f in client_files) + '</ul>'
    with zipfile.ZipFile(client_path, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        z.writestr('manifest.json', json.dumps(manifest, ensure_ascii=False, indent=2))
        z.writestr('modlist.html', modlist)
        for p in sorted((ROOT / 'overrides').rglob('*')):
            if p.is_file():
                z.write(p, p.relative_to(ROOT).as_posix())
        for p in client_override_files:
            z.write(p, 'overrides/' + p.relative_to(client_overrides).as_posix())
    with zipfile.ZipFile(client_path) as z:
        assert z.testzip() is None
        assert json.loads(z.read('manifest.json')) == manifest
        assert z.read('overrides/' + own['path']) == (ROOT / 'overrides' / own['path']).read_bytes()
        assert sorted(name for name in z.namelist() if name.endswith('.jar')) == sorted([
            'overrides/' + own['path'], 'overrides/' + client_own['path']])
        assert 'overrides/config/ftbquests/quests/chapters/first_industry.snbt' in z.namelist()

    inventory = [f'# Зафиксированный состав {VERSION}', '',
        f'57 файлов CurseForge: 57 на клиенте, 53 на сервере. На обеих сторонах также есть свой `{own["file_name"]}`; только на клиенте — `{client_own["file_name"]}`.', '',
        'Значение `both` означает включение в оба набора, `client` — только клиент, `server` — только выделенный сервер.', '',
        '| Проект | Зафиксированный файл | Сторона |', '|---|---|---|']
    for f in files:
        inventory.append(f"| [{f['slug']}]({f['source']}) | `{f['file_name']}` | {f['side']} |")
    (PACK / 'docs/MODS.md').write_text('\n'.join(inventory) + '\n', encoding='utf-8')
    csv_stream = io.StringIO(newline='')
    writer = csv.writer(csv_stream)
    writer.writerow(['slug', 'project_id', 'file_id', 'file_name', 'side', 'sha256', 'source'])
    for f in files:
        writer.writerow([f[k] for k in ['slug','project_id','file_id','file_name','side','sha256','source']])
    (PACK / 'docs/mods.csv').write_text(csv_stream.getvalue(), encoding='utf-8-sig')

    evidence = PACK / 'evidence'
    evidence.mkdir()
    verification = {
        'pack_version': VERSION,
        'server_test_basis': 'Dedicated server with 0.4.0 overrides installed over 0.3.1 test world. Two FTB Quest teams were seeded before the upgrade. The electronics reward was exercised with a NeoForge FakePlayer. Historic stage restart and reload baseline comes from 0.2.0.',
        'client_hotfix': {'mod': backups['file_name'], 'project_id': backups['project_id'],
                          'file_id': backups['file_id'], 'included_in_client': True,
                          'included_in_server': True, 'sha256': backups['sha256']},
        'environment': 'Linux x86_64, Java 21.0.13; dedicated server with no players; graphical client using software OpenGL and OpenAL null output',
        'pinned_curseforge_files_verified': len(files),
        'client_curseforge_files': len(client_files), 'server_curseforge_files': len(server_files),
        'own_addon_sha256': own['sha256'],
        'client_addon_sha256': client_own['sha256'],
        'graphical_client': {'title_screen_rendered': True, 'options_opened': True,
                             'sound_options_opened': True, 'music_manager_playing': True,
                             'keybindings_registered': 238, 'default_bindings_changed': 19,
                             'repeat_changes_after_restart': 0, 'baseline_version': '0.3.0'},
        'login_error_hotfix': {'test_endpoint': 'Local session service returning HTTP 503 with an HTML body',
                               'session_join_denied': True, 'localized_disconnect_screen_rendered': True},
        'recipe_stage_cycle': cycle_checks,
        'cycle_startup_seconds': cycle['startup_seconds'],
        'restart_startup_seconds': restart['startup_seconds'],
        'restart_reload_probe_results': restart['probe_results'],
        'first_industry': industry['industry'],
        'quest_book_migration': {'before': before_quest_update['quest_book'], 'after': quest_book},
        'world_fixture_success': fixture_checks,
        'save_and_clean_stop': True,
        'known_environment_error': 'Yggdrasil public-key fetch: DNS unavailable for the authlib network path. Online player authentication not tested.',
        'not_tested': ['Physical audible menu music', 'Windows script execution', 'Two authenticated players',
                       'Actual rocket flight', 'Audible radio playback in game', 'Reactor operation',
                       'Energy balance', 'AE2 autocrafting and player-action stage bypasses',
                       'Whole quest route in survival and long-running production']
    }
    (evidence / 'verification.json').write_text(json.dumps(verification, ensure_ascii=False, indent=2) + '\n')
    copy(ROOT / 'work/radio-decoder-results.json', evidence / 'radio-decoder-results.json')
    copy(ROOT / 'work/keybindings-first-launch.json', evidence / 'keybindings-first-launch.json')
    copy(ROOT / 'work/client-test/config/m1pack-keybindings.json', evidence / 'keybindings-restart.json')
    log = (ROOT / 'work/client-smoke.log').read_text(errors='replace')
    assert '[M1-MENU] menu music is playing' in log
    assert '[M1-SCREEN] OptionsScreen' in log and '[M1-SCREEN] SoundOptionsScreen' in log
    (evidence / 'menu-client-log.txt').write_text('\n'.join(
        line for line in log.splitlines() if any(word in line for word in ['[M1-', 'Sound engine started'])) + '\n')
    login_log = (ROOT / 'work/join-0.3.1/client.log').read_text(errors='replace')
    markers = ['Connecting to 127.0.0.1, 25578',
               'Minecraft session join received HTML instead of an auth response',
               '[M1-SCREEN] DisconnectedScreen']
    assert all(marker in login_log for marker in markers)
    (evidence / 'auth-error-client-log.txt').write_text('\n'.join(
        line for line in login_log.splitlines() if any(marker in line for marker in markers)) + '\n')
    copy(ROOT / 'work/menu-live.png', PACK / 'docs/menu-preview.png')
    selected_lines = []
    for label in ['industry-first-boot', 'industry-restart', 'quest-book-before-update',
                  'quest-book-after-update', 'quest-book-final-data']:
        selected_lines.append('\n--- ' + label + ' ---')
        for line in (ROOT / 'work' / f'server-{label}.log').read_text(errors='replace').splitlines():
            if any(token in line for token in ['Done (', '[M1-PROBE]', 'Golem Core recipes:',
                    'Saved the game', 'Stopping server', 'All dimensions are saved',
                    'Loaded 1 global', 'Reloading!', 'chapters,', '[M1-INDUSTRY]',
                    'verification has the following contents:', '[M1-QUEST-BOOK]']):
                selected_lines.append(re.sub(r'\x1b\[[0-9;]*m', '', line))
    (evidence / 'server-log-excerpts.txt').write_text('\n'.join(selected_lines) + '\n')

    source = PACK / 'source'
    source.mkdir()
    shutil.copytree(ROOT / 'compat', source / 'compat')
    shutil.copytree(ROOT / 'client', source / 'client')
    for p in client_override_files:
        copy(p, source / 'client-overrides' / p.relative_to(client_overrides))
    shutil.copytree(ROOT / 'overrides', source / 'overrides')
    copy(ROOT / 'mods.lock.json', source / 'mods.lock.json')
    for p in (ROOT / 'tools').glob('*'):
        if p.is_file() and p.suffix in ['.py', '.java']:
            copy(p, source / 'tools' / p.name)
    copy(ROOT / 'SOURCE_RU.md', source / 'README_RU.md')
    datapack = ROOT / 'testing/disposable-datapack'
    shutil.copytree(datapack, source / 'testing/disposable-datapack')
    copy(ROOT / 'testing/industry_fixture.js', source / 'testing/industry_fixture.js')
    copy(ROOT / 'testing/quest_book_fixture.js', source / 'testing/quest_book_fixture.js')

    sums = []
    for p in sorted(PACK.rglob('*')):
        if p.is_file():
            sums.append(digest(p) + '  ' + p.relative_to(PACK).as_posix())
    (PACK / 'SHA256SUMS.txt').write_text('\n'.join(sums) + '\n')
    bundle = OUT / f'M1-SciFi-Prototype-{VERSION}.zip'
    zip_tree(PACK, bundle)
    with zipfile.ZipFile(bundle) as z:
        assert z.testzip() is None
        assert not any('/work/' in name or '/world/' in name for name in z.namelist())
        assert all('..' not in Path(name).parts for name in z.namelist())
    print(json.dumps({'bundle': str(bundle), 'bytes': bundle.stat().st_size,
                      'sha256': digest(bundle), 'files': len(sums)}, ensure_ascii=False, indent=2))

if __name__ == '__main__':
    main()
