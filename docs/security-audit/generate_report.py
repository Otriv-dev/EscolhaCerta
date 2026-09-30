#!/usr/bin/env python3
"""Gera o PDF a partir das evidências auditadas; não executa uma nova auditoria."""
from pathlib import Path
import json,io,textwrap,os
from xml.sax.saxutils import escape
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,PageBreak,Table,TableStyle,Image,KeepTogether,Preformatted
from reportlab.lib.styles import getSampleStyleSheet,ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
BASE=Path(__file__).resolve().parent
DATA=json.loads((BASE/'audit-data.json').read_text())
PALETTE={'crítica':'#B91C1C','alta':'#EA580C','média':'#D97706','baixa':'#2563EB','informativa':'#64748B','forte':'#059669'}
FONT=Path(os.environ.get('AUDIT_FONT_DIR','/usr/share/fonts/truetype/dejavu'))
for name,file in [('Body','DejaVuSans.ttf'),('Bold','DejaVuSans-Bold.ttf'),('Mono','DejaVuSansMono.ttf')]:pdfmetrics.registerFont(TTFont(name,str(FONT/file)))
pdfmetrics.registerFontFamily('Body',normal='Body',bold='Bold',italic='Body',boldItalic='Bold')
styles=getSampleStyleSheet()
styles.add(ParagraphStyle('Text',fontName='Body',fontSize=9,leading=14,spaceAfter=8,textColor=colors.HexColor('#334155')))
styles.add(ParagraphStyle('SmallAudit',parent=styles['Text'],fontSize=7.4,leading=10.5,spaceAfter=3,wordWrap='CJK'))
styles.add(ParagraphStyle('TitleAudit',fontName='Bold',fontSize=25,leading=33,textColor=colors.HexColor('#164FA3'),spaceAfter=20))
styles.add(ParagraphStyle('HeadingAudit',fontName='Bold',fontSize=16,leading=22,textColor=colors.HexColor('#164FA3'),spaceAfter=14))
styles.add(ParagraphStyle('SubAudit',fontName='Bold',fontSize=11,leading=16,textColor=colors.HexColor('#059669'),spaceBefore=10,spaceAfter=7))
styles.add(ParagraphStyle('CodeAudit',fontName='Mono',fontSize=6.6,leading=9,backColor=colors.HexColor('#F1F5F9'),borderPadding=7,spaceAfter=10))
W,H=A4;CONTENT=W-112
story=[]
def p(t,style='Text'):return Paragraph(escape(t),styles[style])
def add(t,style='Text'):story.append(p(t,style))
def title(t):add(t,'HeadingAudit')
def page(t):story.append(PageBreak());title(t)
def table(rows,widths,header=True):
 converted=[[p(str(v),'SmallAudit') if not isinstance(v,Paragraph) else v for v in row] for row in rows]
 t=Table(converted,colWidths=widths,repeatRows=1 if header else 0,hAlign='LEFT')
 commands=[('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LINEBELOW',(0,0),(-1,0),.8,colors.HexColor('#164FA3'))]
 if header:commands += [('BACKGROUND',(0,0),(-1,0),colors.HexColor('#DBEAFE')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#F8FAFC')])]
 t.setStyle(TableStyle(commands));story.append(t)
def code(text):
 lines=[]
 for line in text.splitlines():lines+=textwrap.wrap(line,width=108,replace_whitespace=False,drop_whitespace=False) or ['']
 return Preformatted('\n'.join(lines),styles['CodeAudit'])
def footer(c,doc):
 c.saveState();c.setStrokeColor(colors.HexColor('#CBD5E1'));c.line(56,43,W-56,43)
 c.setFont('Body',7);c.setFillColor(colors.HexColor('#475569'));c.drawString(56,30,'Auditoria de Segurança | Escolha Certa');c.drawRightString(W-56,30,f'{DATA["date"]}  |  Página {doc.page}');
 if doc.page>1:c.drawString(56,H-32,'ESCOLHA CERTA / REVISÃO ESTÁTICA DE CÓDIGO')
 c.restoreState()
# Cover
story.append(Spacer(1,62));add('ESCOLHA CERTA • SEGURANÇA','SubAudit')
add('Relatório de Auditoria de Segurança — Escolha Certa','TitleAudit')
add('30 de setembro de 2026 | pt-BR')
add('Escopo: código Java, handlers MVC, acesso JPA, autenticação e autorização, templates Thymeleaf, JavaScript/CSS, migrations, configurações, testes, scripts, Docker e documentação.')
add('Cinco categorias. Evidências verificáveis. Nenhuma alteração no código da aplicação.','SubAudit')
add('A auditoria examina a cópia local recebida. Não certifica a implantação, o banco em execução ou a ausência de vulnerabilidades. A aplicação não foi executada neste ambiente.')
add('Nota metodológica','SubAudit')
add('RLS foi mapeado para a fronteira de autorização da API MVC: ADMIN global versus conteúdo público publicado. Gates do navegador foram cruzados com as rotas do CMS. IDOR foi revisto em todos os handlers e fluxos por ID. Segredos foram procurados nos arquivos de código e deploy. XSS foi mapeado para th:text/th:field, URLs e APIs DOM. Histórico Git e artefato compilado estavam indisponíveis.')
page('Resumo executivo')
add('0 vulnerabilidades exploráveis verificadas nas cinco categorias','SubAudit')
add('A cópia atual implementa controles coerentes com um CMS de uma única empresa. A ausência de achados é restrita ao código recebido e ao escopo solicitado; ainda há validações necessárias antes da publicação.')
table([['Crítica','Alta','Média','Baixa','Informativa'],['0','0','0','0','0']],[CONTENT/5]*5)
fig,ax=plt.subplots(figsize=(5.4,2.65));ax.pie([1],colors=['#E2E8F0'],wedgeprops=dict(width=.22,edgecolor='white'));ax.text(0,.1,'0',ha='center',va='center',fontsize=32,color='#059669');ax.text(0,-.32,'achados verificados',ha='center',fontsize=10,color='#334155');ax.axis('equal');buf=io.BytesIO();fig.savefig(buf,format='png',dpi=170,bbox_inches='tight');plt.close(fig);buf.seek(0);story.append(Image(buf,width=CONTENT,height=220))
add('Rosca neutra: não há distribuição percentual quando o total é zero. Cores de severidade são reservadas para achados reais.','SmallAudit')
fig,ax=plt.subplots(figsize=(6.3,2.2));names=['Isolamento','Permissão','IDOR','Segredos','XSS'];ax.barh(names,[0]*5,color='#2563EB');ax.set_xlim(0,1);ax.set_xticks([0,1]);ax.set_xlabel('Quantidade de achados verificados');ax.spines[['top','right']].set_visible(False)
for n in range(5):ax.text(.025,n,'0',va='center',color='#334155')
b=io.BytesIO();fig.savefig(b,format='png',dpi=180,bbox_inches='tight');plt.close(fig);b.seek(0);story.append(Image(b,width=CONTENT,height=170))
page('Stack detectada e fronteiras de confiança')
table([['Camada','Detecção / evidência'],['Linguagem e build','Java 17, Maven; pom.xml:2-3'],['Servidor','Spring Boot 3.5.16, Spring MVC; pom.xml:2,5'],['Banco e ORM','MySQL, Spring Data JPA/Hibernate, Flyway; pom.xml:8,13-14; V1__schema.sql:1-8'],['Autenticação','Spring Security, sessão e BCrypt custo 12; SecurityConfig.java:29-39,59-66. Sem JWT ou login de cliente/cuidador.'],['Frontend','25 templates Thymeleaf, site.js de 18 linhas e CSS estático. Sem SPA ou bundler. pom.xml:6,12'],['Deploy','Dockerfile multistage, Docker Compose de desenvolvimento e override local. Profiles dev/prod e storage local/S3. Sem CI/Helm/Terraform entregues.'],['Isolamento real','Acervo global da empresa. ADMIN pode gerir todo o acervo; visitantes veem publicados e criam leads. V1__schema.sql não contém tenant/user proprietário.']],[102,CONTENT-102])
add('Como a revisão foi conduzida','SubAudit')
add(f'Leitura e rastreamento dos {len(DATA["files"])} arquivos textuais inventariados, incluindo buscas de segredos e sinks. Foram percorridos todos os 31 handlers explícitos de PublicController e AdminController: {DATA["route_patterns"]} combinações método/padrão, sem expandir valores possíveis dos enums. Também foram verificados advice, serviços, repositories, handlers de recursos, login/logout e Actuator.')
add('Não se aplica: isolamento entre tenants ou donos individuais, porque não existem esses atores no modelo atual. Aplicam-se: autorização administrativa, privacidade dos leads e estado de publicação. Não há e-mails HTML ou rich text implementados para auditar.')
add('Limitações de cobertura','SubAudit')
for x in DATA['limitations']:add('• '+x)
page('Resultados nas cinco categorias')
for cat,result in DATA['categories']:add(cat,'SubAudit');add(result)
add('Pontos fracos centrais / riscos residuais','SubAudit')
add('Não foi demonstrada uma falha explorável nas categorias solicitadas. Os principais limites para a decisão de publicar são a falta de revisão do histórico e da implantação e a ausência de validação dinâmica. A arquitetura concede a qualquer administrador autenticado o acervo inteiro por definição; ela precisará mudar se houver clientes autenticados ou múltiplas empresas.')
page('Pontos fortes com evidência no código')
add('Os trechos abaixo são da cópia auditada. Linhas de HTML estão minificadas em uma única linha; o excerto preserva o número real dessa linha. Os arquivos completos e hashes constam no inventário.')
for e in DATA['strengths']:
 heading=p(e['id']+' | '+e['title'],'SubAudit')
 meta=p(e['file']+f':{e["start"]}-{e["end"]}','SmallAudit')
 # Excerpts bounded but exact: choose most relevant lines; all evidence ranges stay recorded.
 sn=e['snippet']
 if e['id']=='S04':sn='86:     private CustomForm publicForm(long id) {\n87:         CustomForm f=forms.get(id);\n88:         if(!f.getPublished())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);\n89:         return f;'
 if e['id']=='S12':sn='20:                 String format=reader.getFormatName().toLowerCase();\n21:                 if(!java.util.Set.of("png","jpeg","jpg").contains(format))throw new IllegalArgumentException("Use PNG ou JPEG");\n27:                 if(!javax.imageio.ImageIO.write(image,ext,output))throw new IllegalArgumentException("Não foi possível processar a imagem");'
 if e['id']=='S10':sn='11:  let fields;try{fields=JSON.parse(source.value);if(!Array.isArray(fields))throw Error();}catch(e){fields=[];}\n14:  const input=(label,value,onchange,type=\'text\')=>{const wrap=document.createElement(\'label\');wrap.textContent=label;const element=document.createElement(\'input\');element.type=type;element.value=value||\'\';element.maxLength=type===\'text\'?100:2000;element.addEventListener(\'input\',()=>onchange(element.value));wrap.appendChild(element);return wrap;};'
 story.append(KeepTogether([heading,meta,p(e['description']),code(sn)]))
page('Tabela de achados por categoria')
add('Nenhum registro de vulnerabilidade confirmou uma condição explorável. “Sem achado” é um resultado de revisão, não uma severidade e não uma aprovação de produção.')
rows=[['Severidade','Arquivo:linha','Descrição']]
for cat,_ in DATA['categories']:rows.append(['Não atribuída','Não se aplica','Sem achado verificado: '+cat])
table(rows,[75,105,CONTENT-180])
add('Legenda de chips de severidade','SubAudit')
chips=[Paragraph(f'<font color="white"><b>{escape(k.upper())}</b></font>',styles['SmallAudit']) for k in PALETTE if k!='forte']
t=Table([chips],colWidths=[CONTENT/5]*5);t.setStyle(TableStyle([('BACKGROUND',(i,0),(i,0),colors.HexColor(v)) for i,v in enumerate(list(PALETTE.values())[:5])]+[('TOPPADDING',(0,0),(-1,-1),8),('BOTTOMPADDING',(0,0),(-1,-1),8)]));story.append(t)
add('A tabela está vazia de achados porque não foi comprovada uma falha, e não por falta de aplicação das cinco categorias. Os controles e as limitações são apresentados separadamente para evitar confundir evidência correta com vulnerabilidade.')
add('Credencial de teste observada e classificada','SubAudit')
story.append(code('src/test/resources/application-test.yml\n11:     email: admin@test.local\n12:     password: TesteSeguro123456'))
add('Severidade de vulnerabilidade: não atribuída. A senha pertence aos testes e não há recurso equivalente em src/main. README.md:96 declara seu uso apenas no banco de teste. Não foi observado deploy desse banco ou inclusão de test resources no artefato final. Se houver essa configuração na nuvem, ela deve ser corrigida; não é uma condição demonstrada nesta cópia.')
page('Cobertura integral dos handlers MVC')
add('Base dos caminhos de arquivos abaixo: src/main/java/br/com/escolhacerta/controller/. Todos os handlers de AdminController recebem a guarda SecurityConfig.java:55. Os IDs administrativos representam objetos globais autorizados para esse papel.')
rows=[['Método / caminho','Handler / evidência','Controle verificado']]
for r in DATA['routes']:rows.append([r['method']+' '+', '.join(r['paths']),Path(r['file']).name+':'+str(r['line'])+'\n'+r['handler'],r['control']])
table(rows,[157,98,CONTENT-255])
page('Rotas e hooks fora dos handlers explícitos')
for x in ['POST /login e POST /logout: filtros Spring Security, SecurityConfig.java:59-63. Login autentica; logout é POST com CSRF padrão.','GET /actuator/health e probes: SecurityConfig.java:56-57; application.yml:47-59. Somente health é exposto, sem detalhes.','GET /media/**: WebConfig.java:10-11; somente imagens aceitas por UploadService.java:13-29. GET /css/**, /js/** e /images/**: recursos estáticos públicos.','/error: handler automático Spring Boot, ErrorAdvice.java:8-10 e templates error/*.html, sem dados privados por ID.','GlobalViewAdvice.java:14-18: configurações públicas da empresa id 1 e formulários publicados.']:add('• '+x)
page('Cruzamento de UI, endpoints e consultas')
table([['View / gate','Endpoint / guarda de backend'],['admin/contents.html:1; content-edit.html:1','/admin/contents/**: listar, novo, editar, salvar, excluir. AdminController.java:45,50,55,70,78; SecurityConfig.java:55.'],['admin/leads.html:1; lead-detail.html:1','/admin/leads/**: listar, ler, status, excluir. AdminController.java:83,89,93,97; SecurityConfig.java:55.'],['admin/forms.html:1; form-edit.html:1','/admin/forms/**: listar, novo, editar, salvar, excluir. AdminController.java:101,105,111,122,134; SecurityConfig.java:55.'],['admin/settings.html:1; media.html:1; account.html:1','/admin/settings, /admin/media, /admin/account GET/POST: AdminController.java:138,149,161,164,173,176; SecurityConfig.java:55.'],['fragments/layout.html:5','Menu do CMS não faz gate por role no navegador. Dashboard /admin:38 e todos os destinos acima são protegidos no servidor.'],['forms.html:1 - th:if published','Gate de apresentação. /formularios/{id} GET/POST também valida published em PublicController.java:86-92.'],['listing.html:1; home.html:1','Gate de apresentação do tipo de conteúdo, não de privilégio. PublicController usa ContentService.published; rota blog por ID usa publicPost.']],[160,CONTENT-160])
add('Repositories e consultas globais','SubAudit')
add('ContentRepository.java:7-8 separa publicado de global. CustomFormRepository.java:7 filtra publicação; findAll é usado apenas no CMS. LeadRepository.java:7-8 (findByKind/countByStatus), findAll/count e findById chegam ao inbox/dashboard protegidos. AdminUserRepository.java:7 é usado na autenticação e na troca da própria senha. SiteSettingsRepository usa somente id 1 de configuração pública. Não há relatórios, exportações, GraphQL, API REST adicional ou SQL/query builder construído com entrada recebida.')
page('Inventário arquivo por arquivo')
add('Base: EscolhaCerta/. Cada intervalo cobre o arquivo textual completo lido. Inclui deploy, scripts e testes. SHA-256 completo disponível em audit-data.json e cobertura-auditoria.md para identificar a cópia examinada. Arquivos gerados pela auditoria não entram no escopo de código original.')
rows=[['Arquivo','Linhas verificadas','Hash SHA-256 (prefixo)']]
for f in DATA['files']:rows.append([f['path'],'1-'+str(f['lines']),f['sha256'][:16]])
table(rows,[CONTENT-156,64,92])
add('Assets binários','SubAudit');add('static/images/logo-wide.png e logo.jpg: imagens do frontend, sem revisão de metadados binários. Nenhum bundle/JAR compilado foi fornecido. A auditoria de segredos de frontend cobre os assets textuais efetivamente entregues, não um build inexistente.')
page('Recomendações priorizadas')
add('Pendências para publicar, sem confundi-las com achados de vulnerabilidade. Não é necessário criar issues de segurança fictícias para registrar essas tarefas.')
for priority,t in [('P1','Validar a aplicação real em staging: mvn -B verify, Docker/MySQL e smoke.py em base de teste. Acrescentar/rodar uma matriz negativa para todas as rotas do CMS (anônimo e papel USER), leitura de rascunhos em GET/POST e payloads XSS em campos e erros. Os testes existentes foram lidos, não executados.'),('P1','Auditar o repositório Git completo e o JAR da release: histórico de todos os refs, tags e branches, resources empacotados e assets finais. Confirmar ausência de .env e de application-test.yml no JAR; rotacionar qualquer credencial real encontrada.'),('P2','Conferir a implantação: profile prod, HTTPS/cookie Secure, TLS no MySQL, secrets exclusivos, banco sem acesso público e storage externo limitado ao prefixo de imagens. O Compose recebido é dev e não prova a configuração da nuvem.'),('P3','Manter texto simples escapado no CMS. Se rich text/Markdown for adicionado, implementar sanitização por allowlist nos sinks e testar URLs/protocolos. Se contas de clientes ou tenants forem adicionadas, redesenhar as queries e verificações de posse antes de ativar os novos endpoints.')]:add(priority,'SubAudit');add(t)
add('Reprodução do relatório','SubAudit');add('O gerador lê audit-data.json e recompõe o PDF; ele não refaz a revisão do código. Após uma mudança, atualizar evidências, linhas, hashes e achados antes de regerar. Use Python em venv e requirements.txt; comando: python generate_report.py. DejaVu Sans e DejaVu Sans Mono precisam estar disponíveis; AUDIT_FONT_DIR permite apontar o diretório de fontes.')
page('ISSUES PARA O GITHUB')
add('Nenhum achado acionável de vulnerabilidade foi verificado nesta auditoria das cinco categorias. Não há issues de vulnerabilidade para copiar nesta versão do relatório.')
add('As limitações de acesso e as tarefas de validação são registradas nas seções anteriores. Elas não demonstram exploração e, portanto, não receberam títulos, labels ou severidades de vulnerabilidade inventados.')
add('O arquivo issues-github.md acompanha o relatório com o mesmo resultado. Caso uma nova auditoria confirme um achado, cada issue deve conter título [Segurança], labels security + severidade, evidência exata, impacto, correção e critérios de aceite, delimitada por --- ISSUE n --- e --- FIM ISSUE n ---.')
SimpleDocTemplate(str(BASE/'relatorio-auditoria-seguranca.pdf'),pagesize=A4,rightMargin=56,leftMargin=56,topMargin=56,bottomMargin=58,title='Relatório de Auditoria de Segurança — Escolha Certa',author='Revisão de código - Escolha Certa').build(story,onFirstPage=footer,onLaterPages=footer)
print(BASE/'relatorio-auditoria-seguranca.pdf')
