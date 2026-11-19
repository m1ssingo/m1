#!/usr/bin/env python3
"""Resolve public CurseForge file metadata and download a pinned test set."""
import concurrent.futures
import hashlib
import json
import re
import sys
import time
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CACHE = ROOT / 'work' / 'curseforge'
CACHE.mkdir(parents=True, exist_ok=True)
UA = 'Mozilla/5.0 (compatible; M1ssing0-Minecraft-Prototype/0.1)'
ROOTS = [
 ('applied-energistics-2',223794,'both'),
 ('applied-energistics-2-wireless-terminals',459929,'both'),
 ('mega-cells',622112,'both'),
 ('ae2-network-analyser',961856,'both'),
 ('ex-pattern-provider',892005,'both'),
 ('applied-mekanistics',574300,'both'),
 ('mekanism',268560,'both'),
 ('immersive-engineering',231951,'both'),
 ('immersive-petroleum',268250,'both'),
 ('ntm-continuum',1719328,'both'),
 ('golem-core',1719361,'both'),
 ('deepfield-project',1759685,'both'),
 ('openfm',236839,'both'),
 ('history-stages',1473453,'both'),
 ('ftb-quests-forge',289412,'both'),
 ('ftb-teams-forge',None,'both'),
 ('kubejs',238086,'both'),
 ('jei',238222,'both'),
 ('jade',324717,'both'),
 ('lootr',None,'both'),
 ('gravestone-mod',None,'both'),
 ('sophisticated-backpacks',422301,'both'),
 ('iron-chests',228756,'both'),
 ('iron-jetpacks',284497,'both'),
 ('xaeros-minimap',263420,'client'),
 ('xaeros-world-map',317780,'client'),
 ('carry-on',274259,'both'),
 ('falling-tree',None,'both'),
 ('chunk-loaders',None,'both'),
 ('ftb-backups-3',None,'both'),
 ('tectonic',686836,'both'),
 ('terralith',None,'both'),
 ('serene-seasons',291874,'both'),
 ('farmers-delight',398521,'both'),
 ('chipped',456956,'both'),
 ('stellarview',865273,'client'),
 ('mutant-monsters',852665,'both'),
 ('ferritecore',None,'both'),
 ('inventory-tweaks-refoxed',976858,'client'),
 ('sodium',None,'client'),
 ('spark',None,'both'),
]

def fetch(url, cache_name=None):
    p = CACHE / cache_name if cache_name else None
    if p and p.exists():
        return p.read_bytes()
    for attempt in range(3):
        try:
            req = urllib.request.Request(url, headers={'User-Agent':UA})
            with urllib.request.urlopen(req, timeout=35) as r:
                data = r.read()
            if p:
                p.write_bytes(data)
            return data
        except Exception:
            if attempt == 2:
                raise
            time.sleep(attempt + 1)

def json_get(path, cache_name):
    return json.loads(fetch('https://www.curseforge.com/api/v1/mods/' + path, cache_name))

def project_id(slug):
    s = fetch('https://www.curseforge.com/minecraft/mc-mods/' + slug, slug + '.html').decode()
    unescaped = s.replace('\\"', '"')
    match = re.search(r'"project":\{"id":(\d+)',unescaped)
    if not match:
        raise ValueError('Project ID absent from public project page: ' + slug)
    return int(match.group(1))

def resolve_one(slug, pid, side):
    pid = pid or project_id(slug)
    candidates=[]
    for page in range(12):
        d = json_get(f'{pid}/files?pageIndex={page}&pageSize=50',f'{pid}-all-files-{page}.json')
        files=d.get('data') or []
        candidates += [f for f in files if '1.21.1' in f.get('gameVersions',[]) and 'NeoForge' in f.get('gameVersions',[]) and f.get('status') == 4]
        releases=[f for f in candidates if f.get('releaseType')==1]
        if releases or len(files)<50:
            break
    pool=[f for f in candidates if f.get('releaseType')==1] or candidates
    if not pool:
        raise ValueError(f'No 1.21.1 NeoForge file for {slug} ({pid})')
    f=sorted(pool,key=lambda x:x['dateCreated'],reverse=True)[0]
    fid=f['id']
    deps=json_get(f'{pid}/files/{fid}/dependencies?pageSize=50',f'{pid}-{fid}-deps.json')
    required=[{'slug':x['slug'],'project_id':x['id']} for x in deps.get('data',[]) if x.get('type')=='RequiredDependency']
    return {'slug':slug,'project_id':pid,'file_id':fid,'file_name':f['fileName'],
        'display_name':f['displayName'],'size':f['fileLength'],'published':f['dateCreated'],
        'release_type':f['releaseType'],'side':side,'required_dependencies':required,
        'source':'https://www.curseforge.com/minecraft/mc-mods/'+slug+'/files/'+str(fid),
        'download_url':f'https://edge.forgecdn.net/files/{fid//1000}/{fid%1000}/{urllib.parse.quote(f["fileName"])}'}

def resolve():
    done={}; pending=list(ROOTS); errors=[]
    while pending:
        batch=pending; pending=[]
        with concurrent.futures.ThreadPoolExecutor(max_workers=6) as ex:
            futs={ex.submit(resolve_one,*entry):entry for entry in batch}
            for future in concurrent.futures.as_completed(futs):
                entry=futs[future]
                try:
                    f=future.result()
                    if f['project_id'] in done:
                        if f['side']=='both':done[f['project_id']]['side']='both'
                        continue
                    done[f['project_id']]=f
                    print('RESOLVED',f['slug'],f['file_name'],f['file_id'],flush=True)
                except Exception as e:
                    errors.append({'slug':entry[0],'error':str(e)})
                    print('ERROR',entry[0],str(e),flush=True)
        seen=set(done)
        for f in list(done.values()):
            for dep in f['required_dependencies']:
                if dep['project_id'] not in seen:
                    pending.append((dep['slug'],dep['project_id'],f['side']))
                    seen.add(dep['project_id'])
        (ROOT/'mods.lock.json').write_text(json.dumps({'minecraft':'1.21.1','loader':'neoforge','files':sorted(done.values(),key=lambda x:x['slug']),'errors':errors},ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('TOTAL',len(done),'ERRORS',len(errors),flush=True)

def download_one(f):
    out=ROOT/'work'/'jars'/f['file_name'];out.parent.mkdir(parents=True,exist_ok=True)
    if not out.exists() or out.stat().st_size!=f['size']:
        data=fetch(f['download_url'])
        if len(data)!=f['size'] or not data.startswith(b'PK'):
            raise ValueError(f'Invalid download for {f["slug"]}: {len(data)} bytes')
        out.write_bytes(data)
    with zipfile.ZipFile(out) as z:
        corrupt=z.testzip()
        if corrupt:raise ValueError(f'Corrupt entry {corrupt} in {out.name}')
        f['metadata_paths']=[x for x in ['META-INF/neoforge.mods.toml','META-INF/mods.toml','fabric.mod.json'] if x in z.namelist()]
    data=out.read_bytes()
    f['sha256']=hashlib.sha256(data).hexdigest()
    f['sha512']=hashlib.sha512(data).hexdigest()
    print('DOWNLOADED',f['slug'],len(data),flush=True)
    return f

def download():
    lock=json.loads((ROOT/'mods.lock.json').read_text())
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as ex:
        results=[]
        for f in ex.map(download_one,lock['files']):results.append(f)
    lock['files']=results
    (ROOT/'mods.lock.json').write_text(json.dumps(lock,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':
    {'resolve':resolve,'download':download}[sys.argv[1]]()
