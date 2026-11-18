#!/usr/bin/env python3
"""Run a disposable local server and record a real startup/save/restart check."""
import argparse
import asyncio
import json
import shutil
import time
import urllib.parse
import urllib.request
from pathlib import Path
from industry_regression import FIXTURE, exercise_industry, extract_results

ROOT=Path(__file__).resolve().parents[1]
SERVER=ROOT/'work/server-test'

async def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--label',default='initial')
    parser.add_argument('--function',default=None)
    parser.add_argument('--progression',action='store_true')
    parser.add_argument('--probe',action='store_true')
    parser.add_argument('--reload',action='store_true')
    parser.add_argument('--industry',action='store_true')
    parser.add_argument('--quest-book',choices=('seed','verify'))
    parser.add_argument('--timeout',type=int,default=420)
    args=parser.parse_args()
    report={'label':args.label,'started_at':time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()),
            'ready':False,'exit_code':None,'commands':[],'timeout':False}
    java=['java','-Xms1G','-Xmx5G','-XX:ActiveProcessorCount=4']
    proxy=urllib.request.getproxies().get('https') or urllib.request.getproxies().get('http')
    if proxy:
        u=urllib.parse.urlparse(proxy)
        if not u.username:
            for scheme in ('http','https'):
                java += [f'-D{scheme}.proxyHost={u.hostname}',f'-D{scheme}.proxyPort={u.port or 80}']
    java += ['@libraries/net/neoforged/neoforge/21.1.267/unix_args.txt','nogui']
    log_path=ROOT/'work'/f'server-{args.label}.log'
    test_sources = ([FIXTURE] if args.industry else [])
    if args.quest_book:
        test_sources.append(ROOT / 'testing/quest_book_fixture.js')
    fixtures = [SERVER / 'kubejs/server_scripts' / source.name for source in test_sources]
    for fixture in fixtures:
        if fixture.exists():
            raise RuntimeError(f'Test fixture already exists: {fixture}')
    for source, fixture in zip(test_sources, fixtures, strict=True):
        shutil.copy2(source, fixture)
    started=time.monotonic()
    proc=await asyncio.create_subprocess_exec(*java,cwd=SERVER,stdin=asyncio.subprocess.PIPE,
        stdout=asyncio.subprocess.PIPE,stderr=asyncio.subprocess.STDOUT)
    ready=asyncio.Event()
    lines = asyncio.Queue()
    async def read_output():
        with log_path.open('w',encoding='utf-8') as output:
            while line:=await proc.stdout.readline():
                s=line.decode('utf-8',errors='replace')
                output.write(s);output.flush()
                if test_sources:
                    lines.put_nowait(s)
                if 'Done (' in s and 'For help' in s:
                    report['ready']=True
                    report['startup_seconds']=round(time.monotonic()-started,2)
                    ready.set()
                    print('SERVER_READY',report['startup_seconds'],flush=True)
    async def command(text):
        report['commands'].append(text)
        proc.stdin.write((text+'\n').encode())
        await proc.stdin.drain()
    async def wait_for(marker):
        async with asyncio.timeout(45):
            while True:
                line = await lines.get()
                if marker in line:
                    return line
    async def exercise():
        await ready.wait()
        await command('list')
        await command('seed')
        await command('time query gametime')
        if args.quest_book:
            try:
                await command(f'm1questbook {args.quest_book}')
                line = await wait_for('[M1-QUEST-BOOK] {')
                report['quest_book'] = json.loads(line.split('[M1-QUEST-BOOK] ', 1)[1])
                if 'error' in report['quest_book']:
                    raise RuntimeError(report['quest_book']['error'])
            except Exception:
                await command('stop')
                raise
        if args.industry:
            try:
                await exercise_industry(command, wait_for)
            except Exception:
                await command('tick rate 20')
                await command('tick unfreeze')
                await command('stop')
                raise
        if args.progression:
            await command('history global lock m1_proto_electronics')
            await asyncio.sleep(3)
        if args.progression or args.probe:
            await command('m1probe')
            await asyncio.sleep(3)
        if args.reload:
            await command('reload')
            await asyncio.sleep(20)
            await command('m1probe')
            await asyncio.sleep(3)
        if args.function:
            await command('forceload add 0 0')
            await asyncio.sleep(12)
            await command('function '+args.function)
            await asyncio.sleep(12)
            await command('data get storage m1prototype:checks')
            await command('m1probe')
            await asyncio.sleep(3)
        else:
            await asyncio.sleep(12)
        if args.progression:
            for action in ['unlock','lock','unlock']:
                await command('history global '+action+' m1_proto_electronics')
                await asyncio.sleep(4)
                await command('m1probe')
                await asyncio.sleep(3)
        await command('save-all flush')
        await asyncio.sleep(5)
        await command('stop')
    reader=asyncio.create_task(read_output())
    exerciser=asyncio.create_task(exercise())
    try:
        await asyncio.wait_for(proc.wait(),args.timeout)
    except TimeoutError:
        report['timeout']=True
        if proc.returncode is None:
            try:
                await command('stop')
                await asyncio.wait_for(proc.wait(),30)
            except Exception:
                proc.kill();await proc.wait()
    finally:
        exerciser.cancel()
        try:await exerciser
        except asyncio.CancelledError:pass
        except Exception as e:report['exercise_error']=str(e)
        await reader
        for fixture in fixtures:
            fixture.unlink(missing_ok=True)
    report['exit_code']=proc.returncode
    report['duration_seconds']=round(time.monotonic()-started,2)
    text=log_path.read_text(errors='replace')
    report['stopped_cleanly']='Stopping server' in text and proc.returncode==0
    report['saved']='Saved the game' in text or 'All dimensions are saved' in text
    report['log']=log_path.name
    report['error_lines']=[l for l in text.splitlines() if '/ERROR]' in l or 'Exception in thread' in l][-50:]
    report['probe_results']=[]
    for line in text.splitlines():
        if '[M1-PROBE] {' in line:
            try:report['probe_results'].append(json.loads(line.split('[M1-PROBE] ',1)[1]))
            except json.JSONDecodeError:pass
    if args.industry:
        try:
            report['industry'] = extract_results(text)
        except (AssertionError, KeyError, ValueError) as error:
            report['industry_error'] = str(error)
    (ROOT/'work'/f'result-{args.label}.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
    print(json.dumps(report,ensure_ascii=False,indent=2),flush=True)
    if not report['ready'] or not report['stopped_cleanly'] or report['timeout'] or report.get('industry_error') is not None or 'exercise_error' in report:
        print('\n'.join(text.splitlines()[-70:]),flush=True)
        raise SystemExit(1)

if __name__=='__main__':asyncio.run(main())
