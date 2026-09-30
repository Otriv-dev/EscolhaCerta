# Publicação em nuvem

O projeto é um serviço Java com MySQL. Funciona em infraestrutura que execute containers Java (VM, ECS, Kubernetes ou plataforma equivalente). Não é compatível com hospedagem apenas de páginas estáticas.

## 1. Preparar

1. Execute `mvn -B verify` e o teste com MySQL descrito no README.
2. Construa a imagem: `docker build -t escolhacerta:1.0.0 .`.
3. Envie-a ao registry da sua infraestrutura. Mantenha a tag imutável para rollback.
4. Provisione um banco MySQL 8.4 dedicado, com backups e conexão TLS. Não exponha a porta 3306 ao público.
5. Crie um bucket para **imagens públicas do site** e, se necessário, uma distribuição CDN. Este bucket não deve receber documentos pessoais.

## 2. Variáveis no serviço

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL=jdbc:mysql://HOST:3306/escolhacerta?sslMode=VERIFY_IDENTITY&serverTimezone=UTC`
- `DB_USER`, `DB_PASSWORD`: secrets do ambiente; conta com permissões DDL para migrations e DML para o app.
- `ADMIN_EMAIL`, `ADMIN_PASSWORD`: cadastro inicial; senha com pelo menos 15 caracteres, até 72 bytes e fora da lista de senhas previsíveis. Não ficam no código e a senha só é utilizada quando não existe administrador.
- `PORT=8080`
- `STORAGE_TYPE=s3`
- `S3_BUCKET`, `AWS_REGION`, `S3_PUBLIC_BASE_URL=https://SEU_CDN`
- `S3_ENDPOINT`: opcional para provedor compatível com S3.

O SDK usa a cadeia padrão de credenciais AWS. Prefira IAM role/workload identity com `s3:PutObject` restrito ao prefixo `media/` do bucket. Configure a CDN para leitura ou uma política de leitura pública apenas desse prefixo. O app não altera ACLs do bucket. Em um provedor compatível, forneça credenciais via secret manager; nunca as grave no Git.

Em um único servidor, storage local pode ser usado com volume persistente montado em `/app/uploads`, pertencente ao UID 10001. Em vários servidores, use S3 para evitar imagens diferentes em cada instância.

## 3. Rede, sessões e saúde

- Configure domínio e certificado TLS no load balancer/reverse proxy. Produção exige HTTPS para enviar o cookie seguro.
- Encaminhe `X-Forwarded-Proto=https`, host e endereço do cliente. O ingresso deve substituir headers recebidos externamente e bloquear acesso direto ao app.
- Saúde geral: `GET /actuator/health`. Readiness do banco: `/actuator/health/readiness`. Liveness: `/actuator/health/liveness`.
- Aguarde até 90 segundos para startup e migrations. Configure pelo menos 512 MB de RAM; comece com 1 GB para app e monitore.
- Use uma instância inicialmente. Sessões e rate limiter são locais à instância. Para escalar, implemente Spring Session com Redis ou afinidade de sessão, e limites compartilhados no gateway.
- A migração Flyway ocorre no startup, com histórico no banco e lock de execução. Não altere migrations já aplicadas: adicione `V3__...sql` e versões seguintes.

## 4. Depois de publicar

1. Entre em `/login` com as credenciais iniciais e altere a senha em “Minha senha”. Alterar ADMIN_PASSWORD depois não modifica uma conta existente.
2. Preencha contatos e região em “Dados da empresa”. Revise o texto de privacidade com os dados e processos reais da empresa.
3. Revise serviços e publicações e cadastre apenas depoimentos autorizados. O depoimento de exemplo é rascunho.
4. Teste um orçamento, uma candidatura, uma imagem, um formulário e uma publicação no domínio final.
5. Execute o smoke test com `SMOKE_BASE_URL=https://seu-dominio` e credenciais adequadas, sabendo que ele grava registros de teste.
6. Faça backup periódico do MySQL e do bucket; teste restauração. Não use `docker compose down -v` no ambiente com dados a preservar.

## 5. Atualizar e reverter

Faça backup antes de publicar mudanças que envolvam schema. Rode migrations e testes em staging. Publique a nova imagem, aguarde readiness e verifique o painel. Para rollback de código, retorne à imagem anterior somente se ela for compatível com o schema atual. Flyway não executa rollback automático de schema. Faça restore a partir do backup quando necessário.

## Limites desta versão

Não há checkout, pagamentos, marketplace de contas de clientes, agenda de cuidadores, notificações por e-mail, redefinição de senha por e-mail ou upload de currículo. A candidatura registra dados e experiência no banco e a equipe atende pelo painel. O armazenamento S3 está implementado, mas exige configuração e validação no provedor escolhido. O site utiliza apenas cookies técnicos.

## Controles desta versão

O login usa quota por conta no MySQL (10 tentativas em 5 minutos, contando sucesso e falha), além de 10 POSTs/minuto/IP. Todas as instâncias devem usar a mesma base e manter relógios sincronizados. GET/HEAD dinâmicos têm limite local de 120/minuto/IP; ingresso/CDN deve aplicar limites compartilhados quando houver múltiplas instâncias. Health e assets são excluídos desse orçamento. O proxy deve remover headers externos Forwarded/X-Forwarded-* e o app não deve estar diretamente acessível.

O menu de formulários contém até 20 links e cache local de 30 segundos; escrita no CMS invalida o cache da instância após commit. Outras instâncias atualizam em até 30 segundos. Audit logs persistem em security_audit e também são emitidos após commit, com ator e request_id. Configure retenção/backup e acesso restrito no banco e na plataforma; a aplicação não apaga automaticamente a trilha. Não são armazenadas senhas ou tokens nessa tabela.
