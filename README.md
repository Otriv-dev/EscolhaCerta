# Escolha Certa — site e CMS

Primeira implementação em Java 17, Maven, Spring Boot 3.5.16, Spring MVC, Spring Security, Thymeleaf, MySQL, Flyway e Docker. Logo horizontal fornecida pelo usuário incorporada ao cabeçalho, rodapé e painel. Visual Moderna aprovado: azul #164FA3, verde #16734A, fundo #EFF6FF, branco, chamada principal centralizada e botões arredondados. Frontend azul, verde e branco, responsivo e com formulários integrados ao banco.

**Situação da validação:** arquivos implementados e revisados com verificações estáticas. Compilação, execução da aplicação, testes JUnit, MySQL e Docker não puderam ser executados no ambiente de criação por ausência de Maven/Docker/MySQL e falha de acesso à rede. Não trate esta entrega como homologada em execução. A suíte e o teste HTTP estão prontos para rodar pelos comandos abaixo. Consulte `docs/VALIDATION.md`.

## Rodar no Windows (Docker Desktop)

Abra a pasta `EscolhaCerta` no VS Code e, no PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start.ps1
```

O script gera `.env` com senhas aleatórias e executa `docker compose up -d --build`. A construção executa os testes; em caso de falha, não produz a imagem final.

- Site: http://localhost:8080
- Login: http://localhost:8080/login
- Painel: http://localhost:8080/admin
- Health: http://localhost:8080/actuator/health

Consulte `ADMIN_EMAIL` e `ADMIN_PASSWORD` no `.env` para entrar. Troque a senha pelo painel. Não envie `.env` a terceiros ou ao Git. Na primeira execução, Docker precisa de internet para baixar imagens e dependências.

```powershell
docker compose logs -f app mysql
docker compose ps
docker compose stop
docker compose up -d
```

`docker compose down` remove containers e rede, preservando volumes. **`down -v` apaga o banco e as imagens locais.** Evite esse comando com dados reais.

## Rodar no Linux/macOS

```bash
bash scripts/start.sh
```

Para desenvolver com Java/Maven na máquina e MySQL no Docker:

```bash
bash scripts/run-local.sh
```

No Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-local.ps1
```

Requisitos para executar fora do container: JDK 17 ou superior e Maven 3.6.3 ou superior. Os scripts carregam as variáveis do `.env`; Spring Boot não lê esse arquivo automaticamente. Se mudar DB_PORT, ajuste também a porta em DB_URL. Se a porta 8080 estiver ocupada, altere APP_PORT no compose; para execução da aplicação direta, configure PORT.

## O que está implementado

- Público: início, quem somos, serviços, blog e leitura de publicação, depoimentos, contato e privacidade.
- Caminhos distintos: “Preciso de um cuidador” e “Sou cuidador e quero trabalhar”.
- Formulários: validação servidor/cliente, consentimento, honeypot, CSRF e limite de envio. Dados salvos no MySQL.
- Login: autenticação por e-mail/senha com BCrypt, sessão protegida e acesso ao CMS restrito a ADMIN.
- CMS: dashboard, inbox paginada e filtro por tipo, detalhes e mudança de status, exclusão de dados recebidos, CRUD de serviços/publicações/depoimentos com rascunhos e ordem, editor visual de perguntas para formulários personalizados, dados de contato e texto sobre a empresa, alteração de senha e upload de imagens.
- Migrations: schema e conteúdos iniciais. Serviços e um artigo são publicados; depoimento fictício permanece em rascunho. A migration V4 remove o serviço de cuidado com crianças, que não é oferecido pela empresa.
- Cloud: imagem com usuário sem privilégios, profiles dev/prod, health/readiness/liveness, shutdown gracioso, config por env e storage local/S3.

Os conteúdos são texto simples com quebras de linha, escapados pelo Thymeleaf para impedir execução de HTML recebido. Não há rich text HTML nesta versão. Publicações têm URL `/blog/{id}`. Formulários publicados ficam no rodapé em `/formularios/{id}`.

## Fluxo pelo painel

1. Cadastre informações reais em **Dados da empresa**. WhatsApp usa DDI + DDD + número, sem símbolos.
2. Em **Serviços**, revise cada conteúdo e marque “Publicado”.
3. Em **Imagens**, envie PNG/JPEG até 5 MB, copie a URL e cole no campo de imagem do conteúdo.
4. Em **Publicações**, adicione conteúdos e datas especiais. Pode salvar rascunhos.
5. Em **Depoimentos**, substitua o exemplo por um relato autorizado antes de publicar.
6. Em **Formulários**, use “Adicionar pergunta”, escolha tipo e obrigatoriedade; para listas, escreva uma opção por linha. O identificador aceita letras minúsculas, números e `_`, começando por letra. Até 20 perguntas, sem nomes repetidos.
7. Em **Solicitações recebidas**, filtre orçamento, candidatura, contato ou formulário e altere NEW → IN_PROGRESS → COMPLETED ou ARCHIVED. Exclua os dados quando necessário.

## Testar

Com JDK e Maven:

```bash
mvn -B verify
```

O compose padrão mantém o MySQL apenas na rede interna, evitando conflitos com a porta 3306 do Windows. O arquivo `docker-compose.local.yml` expõe opcionalmente a porta DB_PORT (padrão 3307) para desenvolvimento com Maven fora do Docker.

A suíte padrão usa H2 em modo MySQL, aplica as migrations, verifica o schema JPA e executa os fluxos HTTP com MockMvc. Esse teste não substitui o MySQL real.

Para testar o mesmo conjunto no MySQL, crie um banco **exclusivo de teste** (nunca a base de produção), dê permissões ao usuário e exporte:

```bash
export TEST_DB_URL='jdbc:mysql://localhost:3306/escolhacerta_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export TEST_DB_USER='usuario_teste'
export TEST_DB_PASSWORD='senha_teste'
mvn -B verify
```

A conta inicial desse banco será `admin@test.local`, senha `TesteSeguro!2026Validacao`. Estes dados existem apenas no profile de testes. Os testes gravam dados; descarte o banco depois. No Windows use `$env:TEST_DB_URL = '...'` e equivalentes.

Após subir a aplicação real:

```bash
python3 scripts/smoke.py
```

No Windows, `python scripts/smoke.py`. O smoke test usa credenciais do `.env`, navega páginas, valida health, login, grava formulários e publica um artigo. Limpe os registros identificados como “smoke” pelo painel. Se mudou a senha inicial no painel, forneça a atual em ADMIN_PASSWORD no ambiente do teste.

## Arquitetura

```text
src/main/java/br/com/escolhacerta/
  controller/   HTTP, binding e escolha das views
  dto/          Dados de entrada e validação
  model/        Entidades JPA e enums
  repository/   Acesso ao banco via Spring Data JPA
  service/      Regras de negócio e transações
  security/     Autenticação, permissões e rate limiter
  config/       Bootstrap, recursos e configuração MVC
  storage/      Contrato de upload, local e S3
src/main/resources/
  templates/    Views Thymeleaf públicas e admin
  static/       CSS, JS e logo
  db/migration/ Migrations Flyway versionadas
  application*.yml
src/test/       Testes de fluxos e validação
scripts/        Inicialização, execução e smoke test
```

`PublicController` recebe formulários públicos; `LeadService` grava as solicitações. `ContentService` controla leitura pública e rascunhos. `AdminController` expõe o CMS. `FormService` valida definições e respostas de perguntas dinâmicas. `SecurityConfig` restringe `/admin/**` e mantém CSRF ativo. `MediaStorage` permite trocar disco local por S3 por configuração. `AdminBootstrap` cria o primeiro administrador sem colocar senha em migrations.

## Produção

Siga [docs/DEPLOY.md](docs/DEPLOY.md). O compose incluído é para desenvolvimento; em produção use HTTPS, MySQL gerenciado com TLS, secrets, backup e armazenamento externo. Ajuste `SPRING_PROFILES_ACTIVE=prod` e variáveis exigidas. **Não publique dados de teste ou credenciais padrão.**

## Solução de problemas

- Container não inicia: `docker compose logs --tail=200 app`.
- Credenciais ausentes: execute init e confira `.env`.
- Alterou senha do MySQL no `.env` após criar volume: a imagem MySQL não recria usuário no volume existente. Altere a conta no banco e alinhe os secrets; não apague o volume com dados.
- Login falha em produção: confirme HTTPS, cookie seguro e headers do proxy.
- Imagem não aparece: confira URL, permissões da CDN/bucket e acesso público ao prefixo de mídia.
- Flyway checksum: não edite migration já aplicada; crie nova versão.
- Senha perdida: não há reset por e-mail. Faça backup, gere hash BCrypt e atualize a conta com acesso autorizado ao banco. Reinicie o serviço para invalidar sessões. Não remova o banco.

## Escopo

Não inclui pagamentos, agendamento, contas de clientes ou cuidadores, encaminhamento automático de mensagens por e-mail, avaliação de antecedentes ou garantias de contratação. A operação começa com o recebimento e gestão das solicitações pelo CMS. As imagens S3 são públicas; não há upload de documentos pessoais. Não foram fabricados números de experiência ou depoimentos de clientes.

## WhatsApp da empresa

Atalho no canto inferior direito para (11) 94706-7755, com mensagem pronta de orçamento. A migration V3 configura o número em novas bases e bases existentes. Para alterar depois: painel → Dados da empresa → WhatsApp.

## Idioma e datas

Tipos e status no CMS aparecem em português. Datas e horários exibidos usam `dd/MM/yyyy HH:mm`, no horário de Brasília (`America/Sao_Paulo`). Respostas de data nos formulários aparecem em `dd/MM/yyyy`. Os códigos dos enums e datas UTC/ISO no banco permanecem compatíveis com registros existentes. `DISPLAY_TIME_ZONE` permite configurar outro fuso na aplicação. As auditorias em `docs/security-audit` documentam o snapshot anterior a esta alteração de apresentação.

## Correções de segurança implementadas

Veja [docs/SECURITY_FIXES.md](docs/SECURITY_FIXES.md). Inclui migrations V5/V6 para quota de autenticação e auditoria, política central de senhas, paginação/projeções públicas, menu limitado com cache e quota GET/HEAD. Mantenha os volumes e .env; faça backup e reconstrua a imagem. As senhas existentes não são alteradas automaticamente: revise sua senha administrativa antes de publicar. Os relatórios antigos de auditoria documentam o snapshot anterior às correções.
