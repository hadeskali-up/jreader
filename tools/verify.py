#!/usr/bin/env python3
from pathlib import Path
import re
root=Path(__file__).parents[1]
kt=list((root/'composeApp/src').rglob('*.kt'))
assert kt
for f in kt:
 s=f.read_text()
 # deterministic lexical smoke checks; strips strings before delimiter counts
 clean=re.sub(r'"(?:\\.|[^"\\])*"','""',s)
 assert clean.count('{')==clean.count('}'),f'brace mismatch: {f}'
 assert clean.count('(')==clean.count(')'),f'paren mismatch: {f}'
package_files={}
for f in kt:
 s=f.read_text(); p=re.search(r'^package\s+([\w.]+)',s,re.M)
 assert p,f'missing package: {f}'
 for kind,name in re.findall(r'\b(?:data\s+)?(class|object|interface)\s+(\w+)',s):
  key=(p.group(1),name);package_files.setdefault(key,[]).append(f)
dups={k:v for k,v in package_files.items() if len(v)>1 and k[1] not in {'PlatformStorage','FileDownloader','HtmlParser'}}
assert not dups,dups
required=['core/model','core/state','navigation','source','repository','storage','screens','reader','downloads']
paths='\n'.join(map(str,kt));assert all(x in paths for x in required)
text='\n'.join(f.read_text() for f in kt)
for source in ['MangaDexAdapter','EhentaiAdapter']:assert source in text
for state in ['QUEUED','DOWNLOADING','COMPLETED','FAILED']:assert state in text
assert 'pornographic' in text and ('limit=50' in text or 'limit = 50' in text)
assert '/manga/tag' in text and 'SourceTag' in text
assert 'canPan = { scale > 1f }' in text
assert 'offset=${filter.offset}' in text and 'order[followedCount]=desc' in text
assert 'Load 50 more' in text
assert "'\\u001d'" in text and "'\\u001e'" in text and 'snapshot_v2' in text
store=(root/'composeApp/src/commonMain/kotlin/com/aliworld/jreader/storage/JsonStore.kt').read_text()
migration=store.split('private fun migrateLegacy',1)[1].split('private fun persist',1)[0]
assert 'persist(' not in migration, 'startup migration must not access _data before initialization'
assert 'SystemBackHandler' in text and 'dropLast(1)' in text
print(f'OK: {len(kt)} Kotlin files; architecture, migration, navigation, queue, query checks passed')
