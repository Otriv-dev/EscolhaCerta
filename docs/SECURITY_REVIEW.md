# Revisão de segurança — 30/09/2026

Escopo: código local do projeto, templates, configuração e Docker. Não houve pentest HTTP em servidor, execução Maven/MySQL, auditoria da infraestrutura ou varredura completa de CVEs das dependências transitivas. A revisão não certifica ausência de vulnerabilidades.

## Achados e correções

| Prioridade | Achado | Correção / situação |
|---|---|---|
| Média | Troca de senha mantinha sessões autenticadas abertas | Registro de sessões compartilhado com Spring Security; expiração das sessões da conta, invalidação da sessão atual e novo login obrigatório |
| Média | Alternar URLs de formulário contornava limite por rota | Limite único por IP para todos os formulários públicos, independente da URL |
| Média | Limpeza do rate limiter percorria todos os registros a cada envio | Limpeza periódica, atualização sincronizada e limite de 10 mil chaves; limite continua local à instância |
| Média | Upload podia decodificar imagem com 20 megapixels em hospedagem com pouca RAM | Limite reduzido para 4 MP/3000 pixels por lado; limite também sobre o arquivo regravado; máximo de 5 uploads/minuto/IP |
| Baixa | Alteração de senha podia sofrer tentativas repetidas sem quota específica | Limite de 10 solicitações/minuto/IP na rota da conta; continua exigindo senha atual e CSRF |
| Baixa | Referer podia carregar endereço de páginas para destinos externos | Header Referrer-Policy: no-referrer |
| Confiabilidade | Expressões regulares de e-mail inicial e URL tinham barras excedentes | Escapes corrigidos; teste isolado do e-mail inicial executado no Java |
| Defesa adicional | Limites de requisição e concorrência implícitos | Formulários URL-encoded até 256 KB, headers até 8 KB, até 50 threads e pool de até 5 conexões com banco |

Não alteramos senhas existentes nem migrations já aplicadas. Depois de trocar a senha, o administrador precisa entrar novamente. O limitador retorna 429 e Retry-After: 60; não protege contra ataque distribuído e não substitui proteção do provedor.

## Controles encontrados no código

- /admin/** exige papel ADMIN; endpoints de administração e solicitações não têm rotas públicas equivalentes.
- CSRF permanece ativo; alterações usam POST, inclusive exclusões e logout.
- BCrypt custo 12; não há senha universal de produção no código.
- DTOs limitam tamanhos e validam campos; entidades não são usadas diretamente para receber formulários.
- Repositories usam Spring Data e parâmetros de consultas; não foi identificado SQL montado com entrada pública.
- Thymeleaf usa saída escapada. Não foi encontrado th:utext ou inserção de dados recebidos em innerHTML.
- Upload só no CMS, com leitura real do formato PNG/JPEG, regravação, nome UUID e sem usar o nome original do usuário para construir caminhos.
- CSP, bloqueio de frames, headers padrão do Spring Security, cookies HttpOnly/SameSite e cookie Secure no profile prod.
- Actuator expõe somente health e não exibe detalhes; MySQL não publica porta no compose padrão.

Essas observações são da leitura do código e não prova de que todas as proteções estejam funcionando na implantação final.

## Antes de disponibilizar o link público

1. Configure SPRING_PROFILES_ACTIVE=prod na nuvem. O compose é de desenvolvimento e seleciona dev explicitamente; não o publique sem adaptação.
2. Use somente HTTPS e confirme o proxy encaminhando a indicação HTTPS. O app em prod usa cookie Secure, mas o redirecionamento HTTP/HTTPS deve ser garantido no ingresso da plataforma.
3. O ingresso deve substituir headers Forwarded/X-Forwarded-* externos e impedir acesso direto ao app. A identidade do IP do limitador depende dessa configuração.
4. Use senha administrativa exclusiva, guarde secrets na plataforma e nunca envie .env ao Git. Esta versão ainda não tem MFA; um segundo fator é a principal melhoria adicional para o painel.
5. Use MySQL com TLS e certificado verificado; mantenha o banco fora da rede pública sempre que o provedor permitir. Configure permissões e restrições de rede no banco.
6. Execute mvn verify e scripts/smoke.py; valide permissão ADMIN, rejeição sem CSRF, logout, expiração após trocar senha, formulário inválido, HTML escapado e uploads inválidos.
7. Rode auditoria de dependências na pipeline, por exemplo OWASP Dependency-Check, e examine os resultados com a versão efetivamente resolvida pelo Maven. Pesquisa de advisories não substitui esse processo.
8. Para testes públicos, use dados fictícios. Antes de receber dados reais, defina retenção, acesso, rotina de exclusão e backup/restauração.
9. No Render gratuito não use uploads locais para arquivos que precisam permanecer. Configure storage externo; o bucket de mídia é público e não deve receber currículos/documentos pessoais.
10. Configure limites no ingresso e monitore 429, 5xx, tentativas de login e uso de RAM. Um ataque distribuído pode superar o limitador de uma única instância.

## Validação desta atualização

- Parser oficial Java: 37 arquivos, zero erros sintáticos. Não equivale a compilação com dependências.
- E-mail inicial: execução Java isolada aceitou admin@escolhacerta.local e rejeitou e-mail com espaço.
- YAML e templates: parse realizado; JavaScript: node --check.
- Dois testes JUnit novos preparados para limite compartilhado entre URLs e orçamento separado por IP. A suíte contém agora 25 testes; não executada neste ambiente.
- Maven, Docker, MySQL e pentest HTTP não executados por indisponibilidade das ferramentas/dependências. Verifique em staging antes de concluir a publicação.

## Referências

- https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html
- https://spring.io/security/
