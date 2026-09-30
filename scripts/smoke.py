#!/usr/bin/env python3
"""Live HTTP smoke test against a running app/MySQL, using only stdlib. Writes test records."""
import os, re, urllib.request, urllib.parse, http.cookiejar, json, time
from pathlib import Path
for line in Path('.env').read_text().splitlines():
    if '=' in line and not line.startswith('#'):
        key,value=line.split('=',1);os.environ.setdefault(key,value)
base=os.environ.get('SMOKE_BASE_URL','http://localhost:'+os.environ.get('APP_PORT','8080'))
cookies=http.cookiejar.CookieJar(); client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookies))
def get(path):
    with client.open(base+path, timeout=30) as r: return r.read().decode(),r.url
def post(path,data,token):
    data['_csrf']=token
    with client.open(base+path, urllib.parse.urlencode(data).encode(),timeout=30) as r:return r.read().decode(),r.url
def csrf(html):
    match=re.search(r'<input[^>]*name="_csrf"[^>]*value="([^"]+)"',html)
    if not match:raise AssertionError('CSRF ausente')
    return match.group(1)
for path in ['/','/sobre','/servicos','/blog','/depoimentos','/contato','/orcamento','/sou-cuidador','/privacidade','/login']:
    html,url=get(path);assert 'Escolha Certa' in html,path
health,_=get('/actuator/health');assert json.loads(health)['status']=='UP'
_,url=get('/admin');assert '/login' in url
stamp=str(int(time.time()))
for path in ['/orcamento','/sou-cuidador','/contato']:
    html,_=get(path)
    html,_=post(path,{'name':'Teste smoke '+stamp,'email':'teste@example.com','phone':'34999999999','city':'Uberaba','message':'Solicitação de teste automatizado '+stamp,'consent':'true'},csrf(html))
    assert 'Recebemos suas informações' in html,path
html,_=get('/login');html,url=post('/login',{'username':os.environ['ADMIN_EMAIL'],'password':os.environ['ADMIN_PASSWORD']},csrf(html));assert url.endswith('/admin'),url
for path in ['/admin','/admin/leads','/admin/contents/SERVICE','/admin/contents/POST','/admin/contents/TESTIMONIAL','/admin/forms','/admin/media','/admin/settings','/admin/account']:
    html,_=get(path);assert 'ADMINISTRAÇÃO' in html,path
html,_=get('/admin/contents/POST/new')
html,_=post('/admin/contents/POST/save',{'title':'Publicação smoke '+stamp,'summary':'Teste CRUD','body':'Texto de teste','imageUrl':'','sortOrder':'1','published':'true'},csrf(html))
html,_=get('/blog');assert 'Publicação smoke '+stamp in html
html,_=get('/admin/leads');assert 'Teste smoke '+stamp in html
print('PASS: páginas, health/banco, redirecionamento protegido, login, formulários, inbox e publicação pelo CMS.')
print('Os registros de teste ficaram no painel para revisão e exclusão.')
