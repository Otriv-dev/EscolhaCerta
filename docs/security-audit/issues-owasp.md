# ISSUES PARA O GITHUB - revisão ampliada

--- ISSUE 1 ---
# [Segurança] Login limita por IP, sem orçamento por conta compartilhado
Labels sugeridas: security, média

## Problema
A única chave de quota contém getRemoteAddr e categoria. O username não participa do contador, e o HashMap é privado de cada instância. Trocar de IP ou de instância renova a quota para a mesma conta. Não foi identificado controle adicional de tentativas por conta.

## Condições de exploração
Exige login acessível e múltiplos IPs, múltiplas instâncias ou reinício para exceder a quota local. Comprometer a conta ainda depende de acertar sua senha. Não demonstra login sem credenciais e não presume ataque já ocorrido.

## Evidência
src/main/java/br/com/escolhacerta/security/RateLimitFilter.java:17-17
```java
17:     private final Map<String, Window> windows = new HashMap<>();
```
src/main/java/br/com/escolhacerta/security/RateLimitFilter.java:40-47
```java
40:         if ("POST".equals(req.getMethod())) {
41:             if (path.equals("/login")) category = "login";
42:             else if (path.equals("/orcamento") || path.equals("/sou-cuidador")
43:                      || path.equals("/contato") || path.startsWith("/formularios/")) category = "public-forms";
44:             else if (path.equals("/admin/account")) category = "account";
45:             else if (path.equals("/admin/media")) { category = "upload"; limit = 5; }
46:         }
47:         if (category != null && !allowed(req.getRemoteAddr() + ":" + category, limit)) {
```
src/main/java/br/com/escolhacerta/security/SecurityConfig.java:59-62
```java
59:             .formLogin(login -> login
60:                 .loginPage("/login")
61:                 .defaultSuccessUrl("/admin", true)
62:                 .permitAll())
```

## Impacto
Permite ampliar tentativas contra um administrador além das 10/minuto por IP; cada senha existente avaliada consome BCrypt custo 12. Facilita guessing e credential stuffing.

## Sugestão de correção
Manter quota por IP e adicionar controle por conta normalizada, compartilhado entre instâncias, com janela e backoff. Evitar bloqueio permanente que permita DoS da conta. Acrescentar MFA no CMS como defesa adicional.

## Critérios de aceite
- [ ] A mesma conta continua limitada ao alternar IPs e instâncias.
- [ ] A quota por IP permanece e o e-mail não é armazenado sem necessidade em logs.
- [ ] O bloqueio tem recuperação e TTL definidos; não permite bloqueio permanente malicioso.
- [ ] Teste de integração valida limites usando login real com CSRF.

--- FIM ISSUE 1 ---

--- ISSUE 2 ---
# [Segurança] Política aceita senhas administrativas previsíveis
Labels sugeridas: security, média

## Problema
Bootstrap e troca verificam comprimento mínimo 12 e limite 72 bytes, sem bloqueio de senhas comuns/comprometidas. A configuração contém somente autenticação por senha. As condições aceitam aaaaaaaaaaaa e 123456789012.

## Condições de exploração
Só afeta uma conta se o operador escolher senha fraca; o script init gera senha aleatória forte. A troca exige administrador autenticado, senha atual correta e confirmação. Não há prova de que a senha real seja fraca.

## Evidência
src/main/java/br/com/escolhacerta/config/AdminBootstrap.java:21-27
```java
21:     public void run(ApplicationArguments args) {
22:         if(repo.count()==0) {
23:             if(!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")||password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_PASSWORD com pelo menos 12 caracteres");
24:             AdminUser user=new AdminUser();
25:             user.setEmail(email.toLowerCase());
26:             user.setPasswordHash(encoder.encode(password));
27:             repo.save(user);
```
src/main/java/br/com/escolhacerta/controller/AdminController.java:176-183
```java
176:     @PostMapping("/account") public String password(@RequestParam String currentPassword,@RequestParam String newPassword,@RequestParam String confirmPassword,java.security.Principal principal,Model m,RedirectAttributes flash,jakarta.servlet.http.HttpServletRequest request) {
177:         AdminUser user=users.findByEmail(principal.getName()).orElseThrow();
178:         if(!encoder.matches(currentPassword,user.getPasswordHash())||newPassword.length()<12||newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72||!newPassword.equals(confirmPassword)) {
179:             m.addAttribute("error","Verifique a senha atual e use uma nova senha entre 12 e 72 caracteres, com confirmação igual.");
180:             return "admin/account";
181:         }
182:         user.setPasswordHash(encoder.encode(newPassword));
183:         users.save(user);
```

## Impacto
A política permite que uma credencial administrativa seja facilmente adivinhável, reduzindo a proteção contra tentativas automatizadas.

## Sugestão de correção
Usar serviço único de política em bootstrap/troca, mínimo de 15 caracteres para login sem MFA e lista de senhas comuns/comprometidas. Preservar limite de 72 bytes do BCrypt, aceitar passphrases e Unicode sem exigir composição artificial. Não enviar senhas em texto a serviços de verificação.

## Critérios de aceite
- [ ] Bootstrap e troca rejeitam os dois exemplos e senhas da blocklist.
- [ ] A mesma política é aplicada nos dois fluxos, com mensagens coerentes.
- [ ] Senhas fortes e passphrases dentro do limite de bytes são aceitas.
- [ ] Nenhuma senha ou hash é registrado em logs.

--- FIM ISSUE 2 ---

--- ISSUE 3 ---
# [Segurança] Leituras públicas carregam coleções sem limite e não têm quota de GET
Labels sugeridas: security, média

## Problema
A consulta published retorna List com todas as entidades, incluindo body. Na home, limit(3) é aplicado em stream depois da consulta, portanto não limita o SQL. O advice consulta os formulários publicados integralmente em cada handler MVC. O filtro só classifica POST e deixa os GET públicos sem quota. Não há cache de dados/resultado no código. Cache de templates não é cache dessas queries.

## Condições de exploração
Visitantes podem repetir GET sem autenticação ou CSRF. A amplificação cresce conforme o acervo publicado e a concorrência. Saturação real, RPS suportado e indisponibilidade não foram medidos. Cache/WAF externo pode mitigar, mas não foi auditado.

## Evidência
src/main/java/br/com/escolhacerta/controller/PublicController.java:22-25
```java
22:     @GetMapping("/") public String home(Model m) {
23:         m.addAttribute("services",content.published(ContentKind.SERVICE));
24:         m.addAttribute("posts",content.published(ContentKind.POST).stream().limit(3).toList());
25:         m.addAttribute("testimonials",content.published(ContentKind.TESTIMONIAL));
```
src/main/java/br/com/escolhacerta/service/ContentService.java:17-18
```java
17:     public java.util.List<Content> published(ContentKind kind) {
18:         return repo.findByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDesc(kind);
```
src/main/java/br/com/escolhacerta/repository/ContentRepository.java:6-8
```java
6: public interface ContentRepository extends JpaRepository<Content,Long> {
7:     java.util.List<Content> findByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDesc(ContentKind kind);
8:     java.util.List<Content> findByKindOrderBySortOrderAscCreatedAtDesc(ContentKind kind);
```
src/main/java/br/com/escolhacerta/controller/GlobalViewAdvice.java:14-18
```java
14:     @ModelAttribute("site") public SiteSettings site() {
15:         return settings.findById(1L).orElse(new SiteSettings());
16:     }
17:     @ModelAttribute("publicForms") public java.util.List<CustomForm> forms() {
18:         return forms.findByPublishedTrueOrderByIdDesc();
```
src/main/java/br/com/escolhacerta/security/RateLimitFilter.java:40-52
```java
40:         if ("POST".equals(req.getMethod())) {
41:             if (path.equals("/login")) category = "login";
42:             else if (path.equals("/orcamento") || path.equals("/sou-cuidador")
43:                      || path.equals("/contato") || path.startsWith("/formularios/")) category = "public-forms";
44:             else if (path.equals("/admin/account")) category = "account";
45:             else if (path.equals("/admin/media")) { category = "upload"; limit = 5; }
46:         }
47:         if (category != null && !allowed(req.getRemoteAddr() + ":" + category, limit)) {
48:             res.setHeader("Retry-After", "60");
49:             res.sendError(429);
50:             return;
51:         }
52:         chain.doFilter(req, res);
```

## Impacto
Trabalho de banco e alocação por requisição aumentam com o acervo; tráfego abusivo pode degradar CPU/RAM/latência em hospedagem pequena. Este é risco de DoS na camada de aplicação, não prova de proteção ou vulnerabilidade a DDoS volumétrico.

## Sugestão de correção
Paginar listas públicas; buscar apenas os três posts da home no banco, usando projeção sem body; limitar e cachear o menu de formulários com invalidação no CMS. Definir orçamento de GET no ingresso e na aplicação, preservando health e assets.

## Critérios de aceite
- [ ] A consulta da home tem limite no banco e não carrega o corpo de todos os posts.
- [ ] Listagens possuem tamanho máximo por página e não retornam todo o acervo.
- [ ] Advice usa lista limitada/cacheada com invalidação ao publicar ou editar.
- [ ] Teste em staging mede latência/RAM com acervo representativo e limites de GET.

--- FIM ISSUE 3 ---

--- ISSUE 4 ---
# [Segurança] Logs de mutações não identificam o administrador
Labels sugeridas: security, baixa

## Problema
Eventos content_saved/content_deleted, lead_status_changed/lead_deleted e form_saved registram IDs e atributos, mas não o principal responsável. Troca de senha salva e invalida sessões sem evento explícito da aplicação. Não foi encontrado contexto MDC ou mecanismo de auditoria que adicione o autor.

## Condições de exploração
Uma ação administrativa ocorre; a investigação usa os logs entregues pelo aplicativo. Logs adicionais de proxy, IAM ou SIEM podem existir na implantação e não foram inspecionados. O achado não afirma que não exista nenhum log na plataforma.

## Evidência
src/main/java/br/com/escolhacerta/service/ContentService.java:41-47
```java
41:         Content saved=repo.save(c);
42:         log.info("content_saved id={} kind={} published={}",saved.getId(),kind,saved.getPublished());
43:         return saved;
44:     }
45:     public void delete(long id) {
46:         repo.delete(get(id));
47:         log.info("content_deleted id={}",id);
```
src/main/java/br/com/escolhacerta/service/LeadService.java:34-40
```java
34:     public void status(long id,LeadStatus status) {
35:         get(id).setStatus(status);
36:         log.info("lead_status_changed id={} status={}",id,status);
37:     }
38:     public void delete(long id) {
39:         repo.delete(get(id));
40:         log.info("lead_deleted id={}",id);
```
src/main/java/br/com/escolhacerta/service/FormService.java:49-50
```java
49:         CustomForm saved=repo.save(f);
50:         log.info("form_saved id={} published={}",saved.getId(),saved.getPublished());
```
src/main/java/br/com/escolhacerta/controller/AdminController.java:182-193
```java
182:         user.setPasswordHash(encoder.encode(newPassword));
183:         users.save(user);
184:         for (Object authenticated : sessions.getAllPrincipals()) {
185:             if (authenticated instanceof org.springframework.security.core.userdetails.UserDetails details
186:                 && details.getUsername().equals(user.getEmail())) {
187:                 sessions.getAllSessions(authenticated, false).forEach(info -> info.expireNow());
188:             }
189:         }
190:         var currentSession = request.getSession(false);
191:         if (currentSession != null) currentSession.invalidate();
192:         org.springframework.security.core.context.SecurityContextHolder.clearContext();
193:         return "redirect:/login?passwordChanged";
```

## Impacto
A trilha da aplicação não permite atribuir com segurança mutações à conta responsável; dificulta investigar uso indevido de uma sessão ou mudança de senha.

## Sugestão de correção
Adicionar auditoria estruturada com ID da conta, ação, objeto, horário, resultado e correlation ID; registrar mudança de senha sem valor da senha/hash. Monitorar falhas de autenticação e eventos relevantes com proteção contra log flooding e dados pessoais desnecessários.

## Critérios de aceite
- [ ] Mutações registram ator autenticado e objeto, com correlation ID.
- [ ] Alteração de senha emite evento sem senha, hash ou token.
- [ ] Os eventos ficam disponíveis para revisão com retenção e acesso definidos.
- [ ] Teste confirma autor correto e ausência de segredos nos logs.

--- FIM ISSUE 4 ---
