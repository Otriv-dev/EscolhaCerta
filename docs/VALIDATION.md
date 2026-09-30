# Validação da atualização de segurança - 30/09/2026

## Executado

- Parser Java oficial: 47 arquivos (43 de produção e 4 classes de teste), zero erros sintáticos. Não resolve as dependências Maven.
- Compilação e execução das classes reais PasswordPolicy, AccountLoginThrottle e RateLimitFilter contra stubs mínimos de Servlet/Spring/JDBC.
- Senhas previsíveis rejeitadas; passphrase Unicode e fixture aceitas; limite de bytes verificado.
- Quota normalizada compartilhada entre instâncias, concorrência e expiração verificadas com JDBC stub.
- SQL real da quota em SQLite, com 60 tentativas concorrentes em conexões distintas: exatamente 10 aceitas.
- GET 121 rejeitado, HEAD não contorna a quota, health preservado.
- 25 templates e YAML analisados; V1-V4 preservadas; DDL V5/V6 e insert parametrizado de auditoria verificados em SQLite.
- Datas no fuso de Brasília verificadas em Java, incluindo virada do dia em UTC.

Resultados e limitações detalhados em docs/security-audit/validacao-correcoes.txt e docs/SECURITY_FIXES.md.

## Não executado

- mvn verify, compilação completa com dependências e os 30 métodos JUnit/MockMvc.
- Docker, inicialização MySQL, migrations e queries no MySQL/H2 reais.
- HTTP/login/CSRF/CMS em servidor, transações de auditoria com Hibernate e invalidação de cache em execução integrada.
- Navegador desktop/mobile e storage S3 real.

O ambiente não dispõe de Maven, Docker e MySQL. Stubs e SQLite não substituem essas verificações. A imagem Docker executa mvn verify antes de ser produzida; valide também MySQL e staging antes de publicar.

## Executar localmente

Faça backup e preserve .env e volumes. Na raiz do projeto:

```powershell
docker compose up -d --build --force-recreate app
docker compose logs -f app mysql
python scripts/smoke.py
```

Para provas isoladas sem servidor/banco do usuário:

```powershell
python docs/security-audit/verify_security_logic.py
```

A prova isolada requer Python e JDK. Use dados fictícios e banco dedicado nos testes integrados.
