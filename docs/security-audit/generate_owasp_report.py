#!/usr/bin/env python3
"""Renderiza a revisão OWASP; evidências precisam ser atualizadas após alterações no código."""
from pathlib import Path
from xml.sax.saxutils import escape
import json,io,textwrap,os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,PageBreak,Table,TableStyle,Image,Preformatted,KeepTogether
from reportlab.lib import colors
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
BASE=Path(__file__).resolve().parent;D=json.loads((BASE/'owasp-ampliada.json').read_text())
FD=Path(os.environ.get('AUDIT_FONT_DIR','/usr/share/fonts/truetype/dejavu'))
for n,f in [('Body','DejaVuSans.ttf'),('Bold','DejaVuSans-Bold.ttf'),('Mono','DejaVuSansMono.ttf')]:pdfmetrics.registerFont(TTFont(n,str(FD/f)))
pdfmetrics.registerFontFamily('Body',normal='Body',bold='Bold',italic='Body',boldItalic='Bold')
styles={'body':ParagraphStyle('body',fontName='Body',fontSize=9,leading=14,spaceAfter=8,textColor=colors.HexColor('#334155')),'small':ParagraphStyle('small',fontName='Body',fontSize=7.5,leading=11,wordWrap='CJK'),'title':ParagraphStyle('title',fontName='Bold',fontSize=25,leading=32,textColor=colors.HexColor('#164FA3'),spaceAfter=20),'heading':ParagraphStyle('heading',fontName='Bold',fontSize=16,leading=22,textColor=colors.HexColor('#164FA3'),spaceAfter=14),'sub':ParagraphStyle('sub',fontName='Bold',fontSize=11,leading=16,spaceBefore=10,spaceAfter=7,textColor=colors.HexColor('#059669')),'code':ParagraphStyle('code',fontName='Mono',fontSize=6.5,leading=9,spaceAfter=9,backColor=colors.HexColor('#F1F5F9'),borderPadding=6)}
W,H=A4;CW=W-112;story=[]
def P(t,s='body'):return Paragraph(escape(t),styles[s])
def add(t,s='body'):story.append(P(t,s))
def page(t):story.append(PageBreak());add(t,'heading')
def code(t):
 lines=[]
 for line in t.splitlines():lines+=textwrap.wrap(line,width=105,replace_whitespace=False,drop_whitespace=False) or ['']
 return Preformatted('\n'.join(lines),styles['code'])
def table(rows,widths):
 t=Table([[x if isinstance(x,Paragraph) else P(str(x),'small') for x in row] for row in rows],colWidths=widths,repeatRows=1,hAlign='LEFT')
 t.setStyle(TableStyle([('VALIGN',(0,0),(-1,-1),'TOP'),('BACKGROUND',(0,0),(-1,0),colors.HexColor('#DBEAFE')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#F8FAFC')]),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7)]));story.append(t)
def footer(c,d):
 c.saveState();c.setFont('Body',7);c.setFillColor(colors.HexColor('#475569'));c.drawString(56,30,'Escolha Certa | Auditoria OWASP ampliada');c.drawRightString(W-56,30,f'30/09/2026 | Página {d.page}');c.setStrokeColor(colors.HexColor('#CBD5E1'));c.line(56,43,W-56,43)
 if d.page>1:c.drawString(56,H-32,'REVISÃO DE CÓDIGO / OWASP TOP 10:2025')
 c.restoreState()
story.append(Spacer(1,70));add('ESCOLHA CERTA • REVISÃO AMPLIADA','sub');add('Relatório de Auditoria de Segurança — Escolha Certa','title');add('OWASP Top 10:2025 + mass assignment, IDOR, CSRF, XSS, path traversal, SSRF e DoS');add('30 de setembro de 2026 | pt-BR')
add('Escopo ampliado','sub');add('Java 17, Spring Boot/MVC/Security, JPA/Hibernate, MySQL/Flyway, Thymeleaf e Docker. Revisão de todos os 31 handlers MVC (37 combinações método/padrão), entradas, saídas, autorização, storage, configurações e scripts. A aplicação permaneceu inalterada.')
add('Nota metodológica','sub');add('As categorias OWASP foram adaptadas à stack: autorização por filtro Security; binding por DTOs; consultas JPA; escape Thymeleaf; sessões e BCrypt; upload ImageIO/UUID; limites e observabilidade. Este complemento amplia a auditoria anterior de cinco categorias. A revisão estática e provas de lógica isoladas não substituem pentest ou homologação da nuvem.')
page('Resumo executivo')
add('4 achados: 3 médios e 1 baixo','sub');add('Os novos achados são de autenticação, resistência a abuso e trilha de auditoria. Não foi comprovado acesso sem ADMIN, IDOR privado, mass assignment, XSS, SQLi, command injection ou travessia de caminhos. O resultado não garante ausência de outras falhas.')
table([['Crítica','Alta','Média','Baixa'],['0','0','3','1']],[CW/4]*4)
fig,axs=plt.subplots(1,2,figsize=(7.7,3.5));axs[0].pie([3,1],labels=['Média (3)','Baixa (1)'],colors=['#D97706','#2563EB'],wedgeprops={'width':.28,'edgecolor':'white'},startangle=90,textprops={'fontsize':9});axs[0].text(0,0,'4',ha='center',va='center',fontsize=28,color='#164FA3');axs[0].set_title('Severidade',fontsize=11)
axs[1].bar(['A06','A07','A09'],[1,2,1],color=['#D97706','#D97706','#2563EB']);axs[1].set_ylim(0,2.6);axs[1].set_yticks([0,1,2]);axs[1].set_title('Achados por categoria',fontsize=11);axs[1].spines[['top','right']].set_visible(False)
for i,v in enumerate([1,2,1]):axs[1].text(i,v+.07,str(v),ha='center')
b=io.BytesIO();fig.tight_layout();fig.savefig(b,format='png',dpi=180,bbox_inches='tight');plt.close(fig);b.seek(0);story.append(Image(b,width=CW,height=220))
add('Prioridades','sub');add('P1: fortalecer limitação do login e política de senhas; conferir o ingresso da nuvem. P2: limitar consultas públicas, estabelecer orçamento de GET e registrar o autor das alterações. P3: árvore Maven/SBOM/scanner de dependências, histórico Git e artefato final.')
add('Limitações materiais','sub')
for t in D['limitations']:add('• '+t)
page('Matriz das dez categorias OWASP')
add('DDoS não é resolvido somente com código. A análise cobre abuso de recursos da aplicação; rede, CDN/WAF, capacidade e ataques volumétricos dependem do provedor.')
table([['Categoria','Resultado / evidência']]+[[a,b+'\n'+c] for a,b,c in D['matrix']],[122,CW-122])
page('Tabela dos achados verificados')
add('Base dos caminhos abreviados: src/main/java/br/com/escolhacerta/. Os trechos completos, condições e evidências estão na seção seguinte.')
rows=[['Severidade','Arquivo:linha','Descrição']]
for f in D['findings']:
 shade='#D97706' if f['severity']=='média' else '#2563EB'
 chip=Paragraph('<font color="'+shade+'"><b>'+escape(f['severity'].upper())+'</b></font>',styles['small'])
 location='; '.join(e['file'].replace('src/main/java/br/com/escolhacerta/','')+':'+str(e['start'])+'-'+str(e['end']) for e in f['evidence'])
 rows.append([chip,location,f['id']+' - '+f['title']])
table(rows,[65,205,CW-270])
add('Pontos fracos centrais','sub');add('A autenticação depende apenas de senha, aceita valores previsíveis e não agrega tentativas por conta. O custo de leitura pública cresce com o acervo e o filtro não limita GETs. A trilha de mutações não identifica o administrador.')
add('Condições e severidade','sub');add('As severidades refletem o desenho confirmado no código, não um comprometimento observado. OW-01 depende de alternância de IP/instância; OW-02 depende de escolha de senha fraca; OW-03 depende de volume de acervo e tráfego para causar degradação; OW-04 prejudica a investigação de ações administrativas. Nenhum crítico/alto foi demonstrado.')
page('Achados detalhados com evidência')
for f in D['findings']:
 add(f['id']+' | '+f['title'],'sub');add('Severidade: '+f['severity'].upper()+' | '+f['category'],'small');add(f['description']);add('Condições: '+f['condition']);add('Impacto: '+f['impact']);add('Verificação: '+f['validation'])
 for e in f['evidence']:story.append(KeepTogether([P(e['file']+f':{e["start"]}-{e["end"]}','small'),code(e['snippet'])]))
 add('Recomendação: '+f['fix'])
page('Controles corretos e verificações pendentes')
for name,path,description in D['controls']:story.append(KeepTogether([P(name,'sub'),P(path,'small'),P(description)]))
add('Riscos de implantação não contados como achados','sub');add('application-prod.yml:7 habilita tratamento de headers encaminhados. O proxy confiável precisa remover headers externos e impedir acesso direto ao app; sem isso, a identidade usada pelo limite pode ser falsificada. Não recebemos o ingresso e não demonstramos essa configuração insegura. Da mesma forma, TLS da base, segurança do bucket e HTTPS precisam ser conferidos na plataforma.')
add('Não se aplica ao produto atual','sub');add('Contas de clientes/cuidadores, tenant, JWT, pagamentos, webhook, rich text e e-mails HTML não existem. Não foram forçados achados nessas funcionalidades ausentes. Sanitização de rich text será necessária se esse recurso for introduzido.')
page('Provas de lógica e limites da execução')
add('Executado com Java 17: RateLimitFilter original compilado contra stubs mínimos de Servlet/Spring; seus métodos foram invocados diretamente. As condições de senha foram extraídas literalmente dos arquivos. Isso não inicializa Spring, MySQL, CSRF ou HTTP e não simula DDoS.')
story.append(code(D['test_results']))
add('Reproduzir','sub');add('Na raiz do projeto: python docs/security-audit/verify_security_logic.py. Requer Python e JDK com compilador disponível por ToolProvider. O script cria arquivos temporários de stubs, compila apenas o filtro e imprime o resultado. Não altera o banco nem sobe o site. A ausência de Maven/Docker impediu a suíte integrada; não foram inventados resultados.')
add('Antes da exposição pública','sub');add('Rodar mvn -B verify e smoke.py em staging com base descartável; testar todas as rotas ADMIN com anônimo/USER, POST sem CSRF, rascunhos, campos extras role/id/status, ../ e variações codificadas, URLs perigosas, upload falso e expiração de sessões. Resolver dependências e rodar scanner SCA na release real. Testes de carga só em ambiente autorizado com limites definidos.')
page('Cobertura de todos os handlers')
add('Base: src/main/java/br/com/escolhacerta/controller/. Inventário completo e hashes dos 93 arquivos textuais da cópia permanecem em cobertura-auditoria.md e audit-data.json da auditoria anterior. O snapshot foi comparado e permaneceu intacto.')
table([['Método / rota','Handler:linha','Guarda / fluxo']]+[[r['method']+' '+', '.join(r['paths']),Path(r['file']).name+':'+str(r['line']),r['control']] for r in D['routes']],[158,100,CW-258])
add('Outros handlers: login/logout e Actuator em SecurityConfig:56-63; recursos /media em WebConfig:10-11; advice público em GlobalViewAdvice:14-18; upload excessivo em ErrorAdvice:8-10. Todos revisitados.')
page('Recomendações e referências')
table([['Prioridade','Ação / aceite resumido'],['P1 | OW-01','Quota por conta compartilhada + IP, backoff/TTL, testes com IPs e instâncias distintos.'],['P1 | OW-02','Política única, mínimo apropriado sem MFA, blocklist e testes em bootstrap/troca.'],['P2 | OW-03','Limite SQL, projeções, paginação, cache com invalidação e quota de GET. Medir carga em staging.'],['P2 | OW-04','Ator + ação + objeto + resultado + correlation ID; mudança de senha auditada sem segredos.'],['P1 | implantação','Confirmar proxy confiável, HTTPS, profile prod, TLS de banco e ingress/WAF. Não confirmado nesta auditoria.'],['P3 | cadeia de software','Scanner SCA/SBOM, histórico completo e inspeção do JAR; fixar digests conforme política de release.']],[98,CW-98])
for u in D['references']:add(u,'small')
add('O OWASP Top 10 é referência de riscos; não é uma lista exaustiva ou um certificado. A versão 2025 foi consultada para esta revisão. As recomendações de correção são propostas para esta stack e precisam ser verificadas após implementação.')
page('ISSUES PARA O GITHUB')
add('Quatro issues acionáveis. O texto completo está também em issues-owasp.md para cópia sem quebras visuais do PDF. A próxima implementação deve atender os critérios de aceite e repetir a verificação.')
for n,f in enumerate(D['findings'],1):
 add(f'--- ISSUE {n} ---','sub');add('[Segurança] '+f['title']);add('Labels sugeridas: security, '+f['severity']);add('## Problema','sub');add(f['description']);add('## Condições de exploração','sub');add(f['condition']);add('## Evidência','sub')
 for e in f['evidence']:story.append(KeepTogether([P(e['file']+f':{e["start"]}-{e["end"]}','small'),code(e['snippet'])]))
 add('## Impacto','sub');add(f['impact']);add('## Sugestão de correção','sub');add(f['fix']);add('## Critérios de aceite','sub')
 for a in f['acceptance']:add('- [ ] '+a)
 add(f'--- FIM ISSUE {n} ---','sub')
SimpleDocTemplate(str(BASE/'relatorio-owasp-ampliado.pdf'),pagesize=A4,leftMargin=56,rightMargin=56,topMargin=56,bottomMargin=58,title='Relatório de Auditoria de Segurança — Escolha Certa | OWASP ampliado',author='Revisão de código').build(story,onFirstPage=footer,onLaterPages=footer)
print(BASE/'relatorio-owasp-ampliado.pdf')
