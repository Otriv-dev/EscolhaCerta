# Correções de segurança - 30/09/2026

Esta atualização implementa OW-01 a OW-04 da auditoria ampliada. Não representa homologação da implantação ou garantia de ausência de outras falhas. Os PDFs anteriores são evidências históricas e não foram reescritos como se uma nova auditoria integrada tivesse ocorrido.

## OW-01: limite por conta compartilhado

- AccountLoginThrottle usa uma tabela MySQL, com chave SHA-256 do identificador normalizado e incrementos SQL condicionais atômicos.
- São até 10 tentativas por conta em janela de 5 minutos. Sucessos e falhas consomem quota. O limite por IP também permanece (10/minuto).
- Reiniciar ou trocar instância não renova a quota no banco. Janela expirada libera nova tentativa. O limite não bloqueia permanentemente a conta.
- Falha do banco de quota devolve 503 e não segue com autenticação desprotegida. Quota excedida devolve 429/Retry-After.
- A proteção limita abuso, mas pode afetar o administrador durante ataque à conta. MFA e proteção no ingresso continuam recomendados.

## OW-02: política central de senha

- PasswordPolicy é aplicada na criação inicial e na troca: pelo menos 15 caracteres Unicode, no máximo 72 bytes UTF-8 (BCrypt), blocklist inicial local e rejeição de repetição de um único caractere.
- A blocklist é inicial, não uma base completa de todas as senhas vazadas. Ela deve ser ampliada conforme a operação; nenhuma senha é enviada a terceiros.
- Mantém passphrases e caracteres Unicode, sem exigir composição por maiúsculas/símbolos.
- Senhas já existentes não são alteradas ou invalidadas automaticamente. Use Minha senha para revisar uma credencial fraca antes de publicar.

## OW-03: leituras limitadas

- Home consulta no banco até 3 posts (Slice pode buscar 1 extra para metadados), usando projeção que não contém body. Serviços/depoimentos da home têm até 12 itens.
- Blog, serviços e depoimentos têm páginas de 12 itens; ordenação inclui ID para estabilidade. Listagem de serviços preserva seu texto completo, limitado por página.
- Menu público usa projeção id/título, no máximo 20 links e cache de 30 segundos. Invalidação local após commit do CMS; outras instâncias expiram pelo TTL.
- GET/HEAD dinâmicos compartilham orçamento de 120/minuto/IP na instância. Health, CSS, JS, imagens e mídia ficam fora dessa quota.
- CDN/ingresso deve proteger contra abuso distribuído e volumétrico. Não foi simulado DDoS.

## OW-04: trilha de auditoria

- Tabela security_audit contém evento, horário, ator, ação, objeto, resultado e request_id. Ator conhecido usa ID da conta; identidade não resolvida usa digest, sem e-mail em texto.
- Registra salvar/excluir conteúdo e formulários, status/exclusão de leads, configurações, imagem enviada e alteração/tentativa negada de senha; sucesso/falha de login também.
- Mutações e auditoria participam da transação. Logs de confirmação só são emitidos após commit. O request_id é gerado pelo servidor, não aceito do navegador.
- Não registra senhas, hashes de senha, tokens, mensagens de leads ou o corpo dos formulários. Defina retenção, backup e acesso à trilha na infraestrutura.

## Verificado neste ambiente

- Classes reais PasswordPolicy, AccountLoginThrottle e RateLimitFilter compiladas e executadas contra stubs mínimos de Servlet/JDBC/Spring.
- Senhas previsíveis rejeitadas; passphrase Unicode e fixture aceitas.
- 40 chamadas concorrentes da classe real, duas instâncias e identidade normalizada: exatamente 10 aceitas com JDBC stub; instância nova não renova quota; janela expirada libera.
- Filtro real bloqueou a leitura 121 e preservou health.
- SQL exato da quota executado em SQLite em conexões distintas: 60 tentativas concorrentes, exatamente 10 aceitas. Isso não valida o driver MySQL/H2.
- Parser Java, templates e YAML. Compilação completa com dependências, migrations e SQL de auditoria no MySQL/H2, autenticação e integração HTTP não executados: Maven/Docker/MySQL indisponíveis.

## Testes de integração preparados

A suíte contém 30 métodos @Test. SecurityHardeningTest cobre política, concorrência/expiração da quota no banco, GET/health, paginação/auditoria e invalidação do menu. A suíte foi preparada, mas não executada aqui.

```powershell
docker compose up -d --build --force-recreate app
docker compose logs -f app mysql
```

O Dockerfile executa mvn verify antes de gerar a imagem final. Migrations V5/V6 serão aplicadas no startup. Faça backup antes e não use down -v. Depois execute python scripts/smoke.py com credenciais atualizadas e valide sessão/troca de senha, quota, auditoria e conteúdo em staging.

Para provas isoladas, na raiz do projeto:

```powershell
python docs/security-audit/verify_security_logic.py
```

Esse script requer Python e JDK, usa arquivos temporários e não conecta ao banco do usuário. Não substitui Maven/MySQL. HTTPS/TLS, proxy confiável, secrets, storage externo e varredura de dependências ainda precisam ser confirmados antes da nuvem.
