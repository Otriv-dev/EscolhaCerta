# Cobertura da auditoria - Escolha Certa

Data: 30/09/2026. Auditoria estática; zero achados exploráveis verificados nas cinco categorias.

## Limitações
- O projeto entregue não contém .git; git log falhou. Histórico de commits e segredos removidos não puderam ser auditados.
- Não há JAR/bundle compilado ou source maps. Foram examinados todos os assets de frontend entregues: site.js, site.css e 25 templates.
- Maven, Docker e MySQL indisponíveis neste ambiente. Não houve execução da aplicação, suíte JUnit, pentest HTTP ou inspeção do banco em execução.
- Não foram inspecionados .env do computador do usuário, secrets de nuvem, proxy, IAM, buckets ou pipeline externa. Não há CI, Helm ou Terraform no pacote.
- Sem resolução Maven ou varredura CVE das dependências transitivas; esses controles estão fora das cinco categorias solicitadas.

## Rotas: todos os handlers explícitos

| Método / rotas | Arquivo:linha | Controle verificado |
|---|---|---|
| GET / | src/main/java/br/com/escolhacerta/controller/PublicController.java:22 | ContentService.java:17-18: consulta somente published=true. |
| GET /servicos, /blog, /depoimentos | src/main/java/br/com/escolhacerta/controller/PublicController.java:28 | ContentService.java:17-18: consulta somente published=true. |
| GET /blog/{id} | src/main/java/br/com/escolhacerta/controller/PublicController.java:39 | ContentService.java:26-29: published=true e kind=POST; caso contrário 404. |
| GET /sobre | src/main/java/br/com/escolhacerta/controller/PublicController.java:43 | Página pública; não retorna registros privados. |
| GET /privacidade | src/main/java/br/com/escolhacerta/controller/PublicController.java:46 | Página pública; não retorna registros privados. |
| GET /login | src/main/java/br/com/escolhacerta/controller/PublicController.java:49 | Página pública; não retorna registros privados. |
| GET /orcamento, /sou-cuidador, /contato | src/main/java/br/com/escolhacerta/controller/PublicController.java:60 | Página pública; não retorna registros privados. |
| POST /orcamento, /sou-cuidador, /contato | src/main/java/br/com/escolhacerta/controller/PublicController.java:68 | Entrada pública @Valid LeadForm; cria registro novo; sem ID de lead fornecido. |
| GET /formularios/{id} | src/main/java/br/com/escolhacerta/controller/PublicController.java:78 | PublicController.java:86-89: exige published=true. POST apenas cria lead; não altera formulário. |
| POST /formularios/{id} | src/main/java/br/com/escolhacerta/controller/PublicController.java:91 | PublicController.java:86-89: exige published=true. POST apenas cria lead; não altera formulário. |
| GET /admin | src/main/java/br/com/escolhacerta/controller/AdminController.java:38 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/contents/{kind} | src/main/java/br/com/escolhacerta/controller/AdminController.java:45 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/contents/{kind}/new | src/main/java/br/com/escolhacerta/controller/AdminController.java:50 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/contents/{kind}/{id}/edit | src/main/java/br/com/escolhacerta/controller/AdminController.java:55 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. Verificação de kind (Controller:57/79; Service:33). |
| POST /admin/contents/{kind}/save | src/main/java/br/com/escolhacerta/controller/AdminController.java:70 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. Verificação de kind (Controller:57/79; Service:33). |
| POST /admin/contents/{kind}/{id}/delete | src/main/java/br/com/escolhacerta/controller/AdminController.java:78 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. Verificação de kind (Controller:57/79; Service:33). |
| GET /admin/leads | src/main/java/br/com/escolhacerta/controller/AdminController.java:83 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/leads/{id} | src/main/java/br/com/escolhacerta/controller/AdminController.java:89 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/leads/{id}/status | src/main/java/br/com/escolhacerta/controller/AdminController.java:93 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/leads/{id}/delete | src/main/java/br/com/escolhacerta/controller/AdminController.java:97 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/forms | src/main/java/br/com/escolhacerta/controller/AdminController.java:101 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/forms/new | src/main/java/br/com/escolhacerta/controller/AdminController.java:105 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/forms/{id}/edit | src/main/java/br/com/escolhacerta/controller/AdminController.java:111 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/forms/save | src/main/java/br/com/escolhacerta/controller/AdminController.java:122 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/forms/{id}/delete | src/main/java/br/com/escolhacerta/controller/AdminController.java:134 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/settings | src/main/java/br/com/escolhacerta/controller/AdminController.java:138 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/settings | src/main/java/br/com/escolhacerta/controller/AdminController.java:149 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/media | src/main/java/br/com/escolhacerta/controller/AdminController.java:161 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/media | src/main/java/br/com/escolhacerta/controller/AdminController.java:164 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| GET /admin/account | src/main/java/br/com/escolhacerta/controller/AdminController.java:173 | ADMIN no servidor (SecurityConfig.java:55); escopo global da empresa. |
| POST /admin/account | src/main/java/br/com/escolhacerta/controller/AdminController.java:176 | ADMIN; conta obtida do Principal, senha atual conferida (176-178); invalida sessões (184-192). |

## Controles fora dos controllers

- POST /login e POST /logout: Spring Security, SecurityConfig.java:59-63. Login público autentica; logout mantém CSRF padrão.
- GET /actuator/health, /actuator/health/liveness e /actuator/health/readiness: health público; demais actuator negados (SecurityConfig.java:56-57).
- GET /media/**: imagens públicas, WebConfig.java:10-11; UploadService.java:13-29 restringe o conteúdo. Assets /css/**, /js/** e /images/** são públicos.
- /error: tratamento automático Spring Boot, ErrorAdvice.java:8-10, templates error/*.html; sem consulta por ID ou retorno de segredo.
- GlobalViewAdvice.java:14-18: site_settings id fixo 1 e lista de formulários publicados.

## Evidências corretas

### S01 - Autorização central de todo o CMS
src/main/java/br/com/escolhacerta/security/SecurityConfig.java:54-58
Todos os 21 handlers administrativos estão sob /admin. hasRole("ADMIN") é executado no servidor antes do controller; o menu não constitui a autorização.
```
54:             .authorizeHttpRequests(auth -> auth
55:                 .requestMatchers("/admin/**").hasRole("ADMIN")
56:                 .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
57:                 .requestMatchers("/actuator/**").denyAll()
58:                 .anyRequest().permitAll())
```

### S02 - Listagens públicas somente de conteúdos publicados
src/main/java/br/com/escolhacerta/service/ContentService.java:17-18
A consulta pública usa filtro published=true. As consultas globais sem esse filtro são usadas pelo CMS protegido.
```
17:     public java.util.List<Content> published(ContentKind kind) {
18:         return repo.findByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDesc(kind);
```

### S03 - Blog não expõe rascunhos por troca de ID
src/main/java/br/com/escolhacerta/service/ContentService.java:26-29
A leitura por ID exige publicação e tipo POST; falha retorna 404.
```
26:     public Content publicPost(long id) {
27:         Content c=get(id);
28:         if(!c.getPublished()||c.getKind()!=ContentKind.POST)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
29:         return c;
```

### S04 - Formulários públicos validam o estado em GET e POST
src/main/java/br/com/escolhacerta/controller/PublicController.java:78-92
GET e POST chamam publicForm, que rejeita rascunho; POST grava uma nova solicitação, sem dar acesso a respostas de terceiros.
```
78:     @GetMapping("/formularios/{id}") public String custom(@PathVariable long id,Model m) {
79:         CustomForm f=publicForm(id);
80:         m.addAttribute("custom",f);
81:         m.addAttribute("fields",forms.fields(f.getFieldsJson()));
82:         m.addAttribute("leadForm",new LeadForm());
83:         m.addAttribute("answers",java.util.Map.of());
84:         return "custom-form";
85:     }
86:     private CustomForm publicForm(long id) {
87:         CustomForm f=forms.get(id);
88:         if(!f.getPublished())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
89:         return f;
90:     }
91:     @PostMapping("/formularios/{id}") public String customSubmit(@PathVariable long id,@Valid @ModelAttribute("leadForm") LeadForm dto,BindingResult errors,@RequestParam java.util.Map<String,String> values,Model m,RedirectAttributes flash) {
92:         CustomForm f=publicForm(id);
```

### S05 - Senha alterada somente na própria conta autenticada
src/main/java/br/com/escolhacerta/controller/AdminController.java:176-183
Não aceita user_id ou e-mail do navegador para escolher a conta. Obtém Principal e confere a senha atual.
```
176:     @PostMapping("/account") public String password(@RequestParam String currentPassword,@RequestParam String newPassword,@RequestParam String confirmPassword,java.security.Principal principal,Model m,RedirectAttributes flash,jakarta.servlet.http.HttpServletRequest request) {
177:         AdminUser user=users.findByEmail(principal.getName()).orElseThrow();
178:         if(!encoder.matches(currentPassword,user.getPasswordHash())||newPassword.length()<12||newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72||!newPassword.equals(confirmPassword)) {
179:             m.addAttribute("error","Verifique a senha atual e use uma nova senha entre 12 e 72 caracteres, com confirmação igual.");
180:             return "admin/account";
181:         }
182:         user.setPasswordHash(encoder.encode(newPassword));
183:         users.save(user);
```

### S06 - Senha inicial de produção não é um default público
src/main/java/br/com/escolhacerta/config/AdminBootstrap.java:21-27
Bootstrap de banco sem administrador rejeita e-mail inválido e senha com menos de 12 caracteres ou mais de 72 bytes; salva apenas o hash.
```
21:     public void run(ApplicationArguments args) {
22:         if(repo.count()==0) {
23:             if(!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")||password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_PASSWORD com pelo menos 12 caracteres");
24:             AdminUser user=new AdminUser();
25:             user.setEmail(email.toLowerCase());
26:             user.setPasswordHash(encoder.encode(password));
27:             repo.save(user);
```

### S07 - Compose exige segredos externos
docker-compose.yml:7-9
As expansões :? interrompem a configuração se senhas de banco não estiverem definidas; nenhum valor secreto padrão é embutido.
```
7:       MYSQL_USER: ${DB_USER:?Execute o script de inicialização}
8:       MYSQL_PASSWORD: ${DB_PASSWORD:?Execute o script de inicialização}
9:       MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD:?Execute o script de inicialização}
```

### S08 - Senhas geradas criptograficamente
scripts/init.sh:6-12
OpenSSL gera 24 bytes aleatórios por segredo. PowerShell usa RandomNumberGenerator (init.ps1:5-11).
```
6: cp .env.example .env
7: for key in DB_PASSWORD DB_ROOT_PASSWORD ADMIN_PASSWORD; do
8:   value=$(openssl rand -hex 24)
9:   sed -i.bak "s/^${key}=.*/${key}=${value}/" .env
10:   rm -f .env.bak
11: done
12: chmod 600 .env
```

### S09 - Conteúdo e mensagens renderizados como texto
src/main/resources/templates/post.html:1-1
post.body usa th:text. lead-detail.html:1 também usa th:text para mensagem e respostas. Nenhuma saída HTML não escapada foi encontrada.
```
1: <div class="preserve article-body" th:text="${post.body}"></div>
```

### S10 - DOM do editor não interpreta HTML recebido
src/main/resources/static/js/site.js:11-15
JSON.parse não avalia código; campos usam value e textContent; elementos são criados com createElement.
```
11:  let fields;try{fields=JSON.parse(source.value);if(!Array.isArray(fields))throw Error();}catch(e){fields=[];}
12:  const types=[['text','Texto curto'],['textarea','Texto longo'],['date','Data'],['number','Número'],['select','Lista de opções']];
13:  const sync=()=>{source.value=JSON.stringify(fields);};
14:  const input=(label,value,onchange,type='text')=>{const wrap=document.createElement('label');wrap.textContent=label;const element=document.createElement('input');element.type=type;element.value=value||'';element.maxLength=type==='text'?100:2000;element.addEventListener('input',()=>onchange(element.value));wrap.appendChild(element);return wrap;};
15:  function render(){container.replaceChildren();fields.forEach((field,index)=>{const row=document.createElement('section');row.className='builder-row';const grid=document.createElement('div');grid.className='field-grid';grid.append(input('Pergunta',field.label,value=>{field.label=value;sync();}));grid.append(input('Identificador (sem espaços ou acentos)',field.name,value=>{field.name=value;sync();}));row.append(grid);const label=document.createElement('label');label.textContent='Tipo de resposta';const select=document.createElement('select');types.forEach(([value,text])=>{const option=document.createElement('option');option.value=value;option.textContent=text;select.append(option);});select.value=field.type;select.addEventListener('change',()=>{field.type=select.value;if(field.type==='select'&&!field.options)field.options=['Opção 1','Opção 2'];sync();render();});label.append(select);row.append(label);if(field.type==='select'){const options=document.createElement('label');options.textContent='Opções (uma por linha)';const area=document.createElement('textarea');area.value=(field.options||[]).join('\n');area.rows=4;area.addEventListener('input',()=>{field.options=area.value.split('\n').map(v=>v.trim()).filter(Boolean);sync();});options.append(area);row.append(options);}const required=document.createElement('label');required.className='checkbox';const check=document.createElement('input');check.type='checkbox';check.checked=field.required;check.addEventListener('change',()=>{field.required=check.checked;sync();});const text=document.createElement('span');text.textContent='Resposta obrigatória';required.append(check,text);row.append(required);const remove=document.createElement('button');remove.type='button';remove.className='danger';remove.textContent='Remover pergunta';remove.addEventListener('click',()=>{fields.splice(index,1);sync();render();});row.append(remove);container.append(row);});}
```

### S11 - URL de imagem validada no servidor
src/main/java/br/com/escolhacerta/dto/ContentForm.java:6-11
Permite HTTPS ou caminho local de imagem PNG/JPG. javascript: e data: não satisfazem a expressão. Binding recebe @Valid em AdminController:70.
```
6:     @NotBlank @Size(max=180) public String title;
7:     @Size(max=500) public String summary;
8:     @Size(max=30000) public String body;
9:     @Size(max=1000) @Pattern(regexp="^$|^https://[^\\s]+$|^/media/[a-f0-9-]+\\.(png|jpg)$",message="Use uma URL HTTPS ou uma imagem enviada pelo painel") public String imageUrl="";
10:     public boolean published;
11:     @Min(0) @Max(10000) public int sortOrder;
```

### S12 - Upload público contém somente imagens regravadas
src/main/java/br/com/escolhacerta/service/UploadService.java:13-29
Formato real PNG/JPEG, dimensões e tamanho limitados, decodificação/regravação. Não preserva HTML/SVG ou nome arbitrário do remetente.
```
13:         if(file.isEmpty()||file.getSize()>5*1024*1024)throw new IllegalArgumentException("Envie uma imagem de até 5 MB");
14:         try(var stream=javax.imageio.ImageIO.createImageInputStream(file.getInputStream())) {
15:             var readers=javax.imageio.ImageIO.getImageReaders(stream);
16:             if(!readers.hasNext())throw new IllegalArgumentException("Use uma imagem PNG ou JPEG");
17:             var reader=readers.next();
18:             try {
19:                 reader.setInput(stream);
20:                 String format=reader.getFormatName().toLowerCase();
21:                 if(!java.util.Set.of("png","jpeg","jpg").contains(format))throw new IllegalArgumentException("Use PNG ou JPEG");
22:                 int width=reader.getWidth(0),height=reader.getHeight(0);
23:                 if(width>3000||height>3000||(long)width*height>4000000)throw new IllegalArgumentException("Imagem grande demais. Use até 3000 pixels por lado e 4 megapixels");
24:                 var image=reader.read(0);
25:                 String ext=format.equals("png")?"png":"jpg";
26:                 var output=new java.io.ByteArrayOutputStream();
27:                 if(!javax.imageio.ImageIO.write(image,ext,output))throw new IllegalArgumentException("Não foi possível processar a imagem");
28:                 if(output.size()>5*1024*1024)throw new IllegalArgumentException("A imagem processada ultrapassa 5 MB");
29:                 return storage.store(output.toByteArray(),ext,"image/"+(ext.equals("jpg")?"jpeg":"png"));
```

### S13 - Autenticação e proteção adicional
src/main/java/br/com/escolhacerta/security/SecurityConfig.java:29-39
BCrypt custo 12 e papel ADMIN atribuído pelo servidor. CSRF não está desabilitado; configura CSP sem scripts inline (69-72).
```
29:     @Bean
30:     PasswordEncoder passwordEncoder() {
31:         return new BCryptPasswordEncoder(12);
32:     }
33: 
34:     @Bean
35:     UserDetailsService users(AdminUserRepository repo) {
36:         return username -> repo.findByEmail(username.toLowerCase())
37:             .map(user -> User.withUsername(user.getEmail())
38:                 .password(user.getPasswordHash()).roles("ADMIN").build())
39:             .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
```

### S14 - Navegação e campos dinâmicos têm destinos restritos
src/main/java/br/com/escolhacerta/dto/SettingsForm.java:6-10
WhatsApp somente dígitos, prefixo wa.me fixo (layout.html:3), e-mail validado com prefixo mailto: fixo. FieldDefinition.java:5 restringe tipos e nomes; FormService.java:33 reserva nomes técnicos.
```
6:     @Email @Size(max=180) public String email;
7:     @Size(max=30) public String phone;
8:     @Pattern(regexp="[0-9]{0,15}",message="Use apenas números, incluindo DDI e DDD") public String whatsapp;
9:     @Size(max=120) public String city;
10:     @Size(max=5000) public String about;
```

## Arquivos examinados (hash da cópia auditada)

| Arquivo | Linhas | SHA-256 |
|---|---:|---|
| .dockerignore | 1-6 | a9e321012d71e0f44a5663e8db9cdb96fc545feb06d3fb089e49ebedaa7d6b61 |
| .env.example | 1-21 | 8b1d498a73acd36a709dd245150fef05d65e44d585180b0326e24f6a6c4f610a |
| .gitignore | 1-9 | 36dfc7b6d70f591385eec3123fd5421876d7771efa41f401503a57928680dcee |
| Dockerfile | 1-17 | 1bf08509c190aef37a420be15f033b1950aefec584c2b42ec02371a8e213705d |
| README.md | 1-149 | 7d17b54a86385b4ea7cb503b1ff16986db4283eb1e3c236a1a9ad01353d53005 |
| docker-compose.local.yml | 1-5 | 124c6d80bd3e874192614e27fdfd6edc29c7a6f87c11a5066fc13d5b0c6014e4 |
| docker-compose.yml | 1-39 | 9973cf69b52d6e0e6261d7e134977f338810426f8207f5526a399747ae21e494 |
| docs/DEPLOY.md | 1-52 | a4d930d4e4042db69425079131ceda64565199eee5b5bb1b33d053aaaba04810 |
| docs/REFERENCES.md | 1-9 | a09a2f968bc60cca6828e07565ad2b9c504816a2f92e1d8f98260f07cb80b379 |
| docs/SECURITY_REVIEW.md | 1-59 | 256350d6787724f8a8fa96247edc14b625711e9111b51b3a9d213f7a287fc0b0 |
| docs/UPDATE.md | 1-40 | 32104640a1c5bb0250dc8e6eb1cd4a6e66d04556d215425f421581c21932267d |
| docs/VALIDATION.md | 1-53 | 4b9479af0b8157a2ae2fbd197f7ef5bfba6ece999132351ed841eea9cb7b8885 |
| pom.xml | 1-19 | 944c7f8cfc096cb8ebb4577bc0ae531707437efada9e4e67348241bbbb421dc4 |
| scripts/init.ps1 | 1-14 | 7349a2dff00b9ec351c4c9b4a03afedb56a73202c5d142c1592a436f8458297e |
| scripts/init.sh | 1-13 | e99e948b245c77f5714f2514875964db685b9c41b2221b71a84179260a2b3999 |
| scripts/run-local.ps1 | 1-7 | 7c16a536d33aabb2fa771b25e84f5d3b4dd7f102c3991c18c1c50c115896fef3 |
| scripts/run-local.sh | 1-9 | 81e56ab5e3c716293f387c40d82e3207be895849f1c3b91f0e867dd10d0743fe |
| scripts/smoke.py | 1-36 | b68ae970686936c2d33d6c877682e3cc85688c5d70a0198aac6f24c3a8bc67aa |
| scripts/start.ps1 | 1-6 | 2ce97b5d820b01a6e09ca48f1a71231b5f2eb8dbe9cc2b7c50f5d2d987f7c6b7 |
| scripts/start.sh | 1-6 | bc123ebbab0d200889564c75199860d44a065297afe79df9aa16ef5fefee74ef |
| scripts/test.sh | 1-4 | b955fdef246912eada4a784e2afb90b262637aa69be2655990298a1b64052854 |
| src/main/java/br/com/escolhacerta/EscolhaCertaApplication.java | 1-10 | bf0c3720b24abb61bdbd32de0a5971b004a3d984da87524521fe0d8ddf1d79cf |
| src/main/java/br/com/escolhacerta/config/AdminBootstrap.java | 1-30 | 3e3c7a2d85ddd1513c2c63a5e4cf577617390b27b6adfcd77d2de4fdbec780cd |
| src/main/java/br/com/escolhacerta/config/WebConfig.java | 1-13 | 9d840bc36f60dc72b33204cd3db46029e95fef89d0f384f188f317618a2ec090 |
| src/main/java/br/com/escolhacerta/controller/AdminController.java | 1-195 | c09a6298d6cf31f80ba4cd8d07cd59ae6db507d7c90215e8d4943c9100b43cc0 |
| src/main/java/br/com/escolhacerta/controller/ErrorAdvice.java | 1-12 | 132922600cd10a08f22a43976bf1be5244bda8804aa02f45c16d0d682394ecda |
| src/main/java/br/com/escolhacerta/controller/GlobalViewAdvice.java | 1-20 | 9d89c53a2f54485887e8d5f57bf6f11e66b4ce8ef50e4aabac7b7dfcf4fe9961 |
| src/main/java/br/com/escolhacerta/controller/PublicController.java | 1-108 | b006262f53f0659d5b3152434da8ca0b8448a7c733505559401ee84f8ccdd23a |
| src/main/java/br/com/escolhacerta/dto/ContentForm.java | 1-48 | 1ba559fe72289bb26efb943b0fb18de9a745466fb746b722fbf22d3542ccc75a |
| src/main/java/br/com/escolhacerta/dto/CustomFormDto.java | 1-34 | 659c3c0fc69745e64f4dda6c575503515a99e498054a27fbcd8022bc7d8fbef4 |
| src/main/java/br/com/escolhacerta/dto/FieldDefinition.java | 1-6 | 5808970e5b101e35a69f6b4b3fbfa0ec1b64d5704825c2a0f7f6ccfca1fb2ced |
| src/main/java/br/com/escolhacerta/dto/LeadForm.java | 1-55 | bdc7f496f24b54b798bdf1026d9efce871890734ef2064db1ff1c276b02b8080 |
| src/main/java/br/com/escolhacerta/dto/SettingsForm.java | 1-41 | 779f0d4b05249065aa7fc9b87021ce4c3802e81188b2f08c9fcc848f588abfdd |
| src/main/java/br/com/escolhacerta/model/AdminUser.java | 1-28 | 37247eb00de278077e90455a1771c4ddac8b1c942d6933d441486c9afe75f498 |
| src/main/java/br/com/escolhacerta/model/Content.java | 1-70 | 5a79a413227e119bd5aed90d53e5b1dc5763c8c9fe349848ae3213d67e853aba |
| src/main/java/br/com/escolhacerta/model/ContentKind.java | 1-5 | 94725551c772c5988b1c1626bc8ccedf2b824e10eaeaf89a62d33b7c7ad6d7c1 |
| src/main/java/br/com/escolhacerta/model/CustomForm.java | 1-42 | 11a7953b2e01f3d998d5a7c06188b1b0a536ca815dab5a10f6399bdd226012c9 |
| src/main/java/br/com/escolhacerta/model/Lead.java | 1-91 | 524980b57fc9148c03cdc1c9b5287fe70142007e78316cb0b72f729448e2e229 |
| src/main/java/br/com/escolhacerta/model/LeadKind.java | 1-5 | 644653770c057a7208dd5e05204d188be7b5ae85aa14c56fad5f95c586ffc9d6 |
| src/main/java/br/com/escolhacerta/model/LeadStatus.java | 1-5 | 8e3e086bfcf3b9f6453a488a5f5cc3f5f2a4c40420a6783015688c55d6b1b825 |
| src/main/java/br/com/escolhacerta/model/SiteSettings.java | 1-49 | a8d041592f71d44dc4ec6102dc04e8ddecc5d04ceb0bdcd3396cc1a9e3909565 |
| src/main/java/br/com/escolhacerta/repository/AdminUserRepository.java | 1-8 | c6156c7177ab48ffb94f53be6a2610cc9a34a3413a6caaa0ff3fbf9b6633fff6 |
| src/main/java/br/com/escolhacerta/repository/ContentRepository.java | 1-9 | b77477e1587877d432a63fc962fe77113a740ed2240e8f705cfe586fa83fbb0c |
| src/main/java/br/com/escolhacerta/repository/CustomFormRepository.java | 1-8 | 64571c8e080b1711c5cf43034baf3125b1cfae7d0e3f75ee9c87bafea55236d9 |
| src/main/java/br/com/escolhacerta/repository/LeadRepository.java | 1-9 | 3aa5681ea29a2db10b8db020bfc066854474b94a4bf5bde3b4bba9366ed1f97d |
| src/main/java/br/com/escolhacerta/repository/SiteSettingsRepository.java | 1-7 | c8871e6ea844f75b3ffeffcc1a7b255157c7ba06b6c3d517c779fb01f2326624 |
| src/main/java/br/com/escolhacerta/security/RateLimitFilter.java | 1-54 | d2331cb29597612329084014416541947b6fc7be75e5681e72fef952e910bc30 |
| src/main/java/br/com/escolhacerta/security/SecurityConfig.java | 1-75 | b6ec90f4f2d5d142762afce7bbb2fb485be62af3c1155cf10d1b8c1176e83c6c |
| src/main/java/br/com/escolhacerta/service/ContentService.java | 1-49 | c9d8e744e1e821c695f9a01e1d5703a09709981ab8c7d0dc6a9c9e907563f0b3 |
| src/main/java/br/com/escolhacerta/service/FormService.java | 1-82 | 71d12eded0c378201faa2e0ca042c9cca18fdf8d3b51152f82ddf07c2dd862bb |
| src/main/java/br/com/escolhacerta/service/LeadService.java | 1-42 | b492d96c25ad522a6893425efab14ce196457f696160d17bfd2537e44c1d0962 |
| src/main/java/br/com/escolhacerta/service/UploadService.java | 1-39 | 12605d85ad213eef620c1690ed305c75317ae4c412046439389e5966ff5d36f5 |
| src/main/java/br/com/escolhacerta/storage/LocalMediaStorage.java | 1-24 | 0ae280ce662a2ad5fb4931a8ac9593d13ab3d9eadb8e4b7c996dd2cb28f52d20 |
| src/main/java/br/com/escolhacerta/storage/MediaStorage.java | 1-5 | 32761a54fb2074d8cdb144688973f08115796308316dfeb70020d2c446d80969 |
| src/main/java/br/com/escolhacerta/storage/S3MediaStorage.java | 1-30 | 26a9657dcf394c2118f4baeb851638efe9809716b6333fb6ac474ce579dac718 |
| src/main/resources/application-dev.yml | 1-6 | 90f98e71f0f9933d091d6d68aad9a43bb5d66173d0e70e3703d3c645a0c8c67a |
| src/main/resources/application-prod.yml | 1-15 | 4fd25edcca915a1cee00e4048aa101dc5b86ad87e740be15090d50e8e85f7a65 |
| src/main/resources/application.yml | 1-70 | aa8103219ab08395545490d56fb19359272d3b3f34551dbde17b60ec55638888 |
| src/main/resources/db/migration/V1__schema.sql | 1-8 | e804d4677479df06d5482af82609dadf02e21c0eeced04d614628b07a1c7c424 |
| src/main/resources/db/migration/V2__initial_content.sql | 1-12 | 08c682aa696add3f5d0815c2be8c2c1d32eddcdd2147397111092cf6eccaa0a4 |
| src/main/resources/db/migration/V3__company_whatsapp.sql | 1-5 | 0a9f2a9fb973e38c4d56a7be7b1701f5eac5675dd60ea0343ae9de2f67227c59 |
| src/main/resources/db/migration/V4__remove_childcare_service.sql | 1-3 | 1c6e42107855908d7b5967e54ece3aee3202b03e5c0011af5f647ac213a2d0d8 |
| src/main/resources/static/css/site.css | 1-44 | 129f99b8d20706dd836a96b3ccd9442d425577b7704698c799ff4fcdff96903b |
| src/main/resources/static/js/site.js | 1-18 | 137cf66483c6701b88980735a3be3e4bd57ed4a8ccfbca7176f3ed7e86704b9b |
| src/main/resources/templates/about.html | 1-1 | be10be0f51ffd777811bf71e657caf9355806b090d347c4330dc2ab0cd1f89d7 |
| src/main/resources/templates/admin/account.html | 1-1 | 35e18e310e2b026be03fee4820c7b02a281b2904ed256d1d570a8b7880a3faf5 |
| src/main/resources/templates/admin/content-edit.html | 1-1 | 8ac1724b569f2517ecda385d79236723082336e7bfd02e4ef6a59ec873d0722b |
| src/main/resources/templates/admin/contents.html | 1-1 | aa0ef0cb0d38fda7d1552e669956751e72bd2ff3d68504088325a6ce3eb61bcb |
| src/main/resources/templates/admin/dashboard.html | 1-1 | f4e0adf8d2a1c49afdd3a78a755b67c873fb18f1f45bec2f7d38944459795181 |
| src/main/resources/templates/admin/form-edit.html | 1-1 | 8b7401eedf89d63eefc9f7d40bcb81aadb03480178b47b8a34d2f994480d6e6b |
| src/main/resources/templates/admin/forms.html | 1-1 | c7222ef4c823c2c008abca00f822e45e6684d0d29770f46dab8b45aeff68e039 |
| src/main/resources/templates/admin/lead-detail.html | 1-1 | 2e1a3db891d9f0ea3f42879530dfff20f01c3b1f4bb3fc40cd40c5f0d49ebcdc |
| src/main/resources/templates/admin/leads.html | 1-1 | b66cd1d4bed16c89ef64873e50aa8c1b0d68dd43167f0e3b3c5f1c1a8ea10121 |
| src/main/resources/templates/admin/media.html | 1-1 | d51ba029c1cce41e279d94a442ebd0a70c4f9ab6414bf80fb6c5891b6484a5cf |
| src/main/resources/templates/admin/settings.html | 1-1 | 309ba6141e0b3f2b349addece34e745afdb3c59bce46f9514455c8f204576bcf |
| src/main/resources/templates/custom-form.html | 1-1 | a5c090f3cda7a8d2ec7cbf5f8b42696890c6bfdce01b412bd4d63c7e313daae7 |
| src/main/resources/templates/error/403.html | 1-1 | 5f76cbd67c50551c0d32fb20510ae90de6c02b2b6d21d173488a9dbecea6ee1e |
| src/main/resources/templates/error/404.html | 1-1 | c4330aeba35ca23b3295bb4af035d0ce3985e98be075b99027b69331024b7042 |
| src/main/resources/templates/error/429.html | 1-1 | 5d592f35f31118d58ba7e91087805bce3806bd7e967eb4de6857c7f6de893b5e |
| src/main/resources/templates/error/500.html | 1-1 | 12f4121e92c386f0e4e9790f1ad0b1f54c2c0345348fb8214b0cd2f9fbf502ad |
| src/main/resources/templates/error.html | 1-1 | df3fc2a0b0526634f39b839fcc59663238ccd11f73af8a24e4daa0ddac778de8 |
| src/main/resources/templates/fragments/form.html | 1-1 | c658965a9c257e84f51d3989cfde64663543f7d3a361f5d50b842ce2659054b2 |
| src/main/resources/templates/fragments/layout.html | 1-6 | aae1a03cf7eea283f0060e60ba5830c3b31dd6dad976c39e2382b0d7998ba2d8 |
| src/main/resources/templates/home.html | 1-7 | 458290a6a43216550833625d7de1d65ddadf74397e67bc0c6bd962bb2c82a472 |
| src/main/resources/templates/lead-form.html | 1-1 | 2c5e557e48d5aa025cff0ff8228faae3133bdd3b0fbdfe64c12f7857773c4991 |
| src/main/resources/templates/listing.html | 1-1 | 1d4e24d2720842b3b4e1ef5bc162332064310762ee295bffb013c4b114aef2e2 |
| src/main/resources/templates/login.html | 1-1 | 7acef54c55bf6c2f06ac172b6ae3bee7465cbb359d037e446c6cbfc6f0e8acc5 |
| src/main/resources/templates/post.html | 1-1 | 60a63ef772d7fb8f6ff6cfd68dacd7471c6a193135f57534de00f5d0c187982b |
| src/main/resources/templates/privacy.html | 1-1 | f611ff96db5d0bb8be9fb03adc52c42cb215ed50affec4898f8fbe435b166327 |
| src/test/java/br/com/escolhacerta/ApplicationFlowTest.java | 1-140 | 9ee2df6243fe7e42fb617a80c2b1eb98bc910bbf14b1777a8e26c8424bca4551 |
| src/test/java/br/com/escolhacerta/FormValidationTest.java | 1-24 | 89e63672217bcbefb71d1d0fa764b93ec78d4ba6416fd4967bb4a3261782f22a |
| src/test/java/br/com/escolhacerta/RateLimitSecurityTest.java | 1-28 | c5de1e01a65a4b254ceaae9eeb9d5408419de0747cfad1357a7658d28c7efa35 |
| src/test/resources/application-test.yml | 1-15 | 3809b96a52b4c6c4e6b16e0ef8e047420ea93a460745a901134925d7bc182cac |
