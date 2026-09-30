package br.com.escolhacerta;

import br.com.escolhacerta.repository.*;
import br.com.escolhacerta.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") class ApplicationFlowTest {
    @Autowired MockMvc mvc;
    @Autowired LeadRepository leads;
    @Autowired ContentRepository contents;
    @Autowired CustomFormRepository forms;
    @Autowired AdminUserRepository users;
    @Test void publicPagesRender() throws Exception {
        for(String path:new String[] {
            "/","/sobre","/servicos","/blog","/depoimentos","/privacidade","/contato","/orcamento","/sou-cuidador","/login"
        }
        )mvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Escolha Certa")));
    }
    @Test void healthUsesDatabase()throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void protectedPagesRequireLogin()throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/actuator/env")).andExpect(status().is3xxRedirection());
    }
    @Test void submissionRateLimited()throws Exception {
        for(int i=0;i<11;i++) {
            var request=post("/contato").with(csrf()).with(r-> {
                r.setRemoteAddr("192.0.2.42");return r;
            }
            );
            mvc.perform(request).andExpect(i<10?status().isOk():status().isTooManyRequests());
        }
    }
    @Test void missingCsrfIsRejected()throws Exception {
        mvc.perform(post("/orcamento").param("name","Nome")).andExpect(status().isForbidden());
    }
    @Test void realPasswordLoginWorks()throws Exception {
        mvc.perform(formLogin().user("admin@test.local").password("TesteSeguro!2026Validacao")).andExpect(authenticated().withRoles("ADMIN")).andExpect(redirectedUrl("/admin"));
        assertThat(users.findByEmail("admin@test.local").orElseThrow().getPasswordHash()).startsWith("$2").doesNotContain("TesteSeguro");
    }
    @Test void badPasswordIsRejected()throws Exception {
        mvc.perform(formLogin().user("admin@test.local").password("errada")).andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }
    @Test void validPublicFormsPersist()throws Exception {
        long initial=leads.count();
        for(String path:new String[] {
            "/orcamento","/sou-cuidador","/contato"
        }
        )mvc.perform(post(path).with(csrf()).param("name","Cliente Teste").param("email","cliente@example.com").param("phone","34999999999").param("city","Uberaba").param("message","Preciso de apoio durante o dia").param("consent","true")).andExpect(status().is3xxRedirection());
        assertThat(leads.count()).isEqualTo(initial+3);
        assertThat(leads.findAll().stream().map(Lead::getKind)).contains(LeadKind.QUOTE,LeadKind.CAREGIVER,LeadKind.CONTACT);
    }
    @Test void invalidLeadDoesNotPersist()throws Exception {
        long before=leads.count();
        mvc.perform(post("/orcamento").with(csrf()).param("name","").param("email","invalido").param("phone","123").param("message","Teste")).andExpect(status().isOk()).andExpect(model().attributeHasErrors("leadForm"));
        assertThat(leads.count()).isEqualTo(before);
    }
    @Test void botTrapDoesNotPersist()throws Exception {
        long before=leads.count();
        mvc.perform(post("/contato").with(csrf()).param("name","Bot").param("email","bot@example.com").param("phone","34999999999").param("message","Teste").param("consent","true").param("website","spam.example")).andExpect(status().is3xxRedirection());
        assertThat(leads.count()).isEqualTo(before);
    }
    @Test @WithMockUser(roles="USER") void wrongRoleDenied()throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(roles="ADMIN") void adminPagesRender()throws Exception {
        for(String path:new String[] {
            "/admin","/admin/leads","/admin/contents/SERVICE","/admin/contents/POST","/admin/contents/TESTIMONIAL","/admin/contents/POST/new","/admin/forms","/admin/forms/new","/admin/media","/admin/settings","/admin/account"
        }
        )mvc.perform(get(path)).andExpect(status().isOk());
    }
    @Test @WithMockUser(roles="ADMIN") void contentLifecycleAndXssEscaping()throws Exception {
        mvc.perform(post("/admin/contents/POST/save").with(csrf()).param("title","Publicação CRUD").param("summary","Resumo").param("body","<script>alert(1)</script>").param("imageUrl","").param("sortOrder","1").param("published","true")).andExpect(status().is3xxRedirection());
        Content c=contents.findByKindOrderBySortOrderAscCreatedAtDesc(ContentKind.POST).stream().filter(x->x.getTitle().equals("Publicação CRUD")).findFirst().orElseThrow();
        mvc.perform(get("/blog/"+c.getId())).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")));
        mvc.perform(get("/admin/contents/POST/"+c.getId()+"/edit")).andExpect(status().isOk());
        mvc.perform(post("/admin/contents/POST/save").with(csrf()).param("id",c.getId().toString()).param("title","Rascunho").param("imageUrl","").param("sortOrder","1")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/blog/"+c.getId())).andExpect(status().isNotFound());
        mvc.perform(post("/admin/contents/POST/"+c.getId()+"/delete").with(csrf())).andExpect(status().is3xxRedirection());
        assertThat(contents.findById(c.getId())).isEmpty();
    }
    @Test @WithMockUser(roles="ADMIN") void unsafeImageUrlRejected()throws Exception {
        mvc.perform(post("/admin/contents/POST/save").with(csrf()).param("title","Teste").param("imageUrl","javascript:alert(1)")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("contentForm","imageUrl"));
    }
    @Test @WithMockUser(roles="ADMIN") void customFormEndToEnd()throws Exception {
        String json="[{\"name\":\"horario\",\"label\":\"Horário\",\"type\":\"select\",\"required\":true,\"options\":[\"Manhã\",\"Noite\"]}]";
        mvc.perform(post("/admin/forms/save").with(csrf()).param("title","Formulário CRUD").param("fieldsJson",json).param("published","true")).andExpect(status().is3xxRedirection());
        CustomForm f=forms.findAll().stream().filter(x->x.getTitle().equals("Formulário CRUD")).findFirst().orElseThrow();
        mvc.perform(get("/formularios/"+f.getId())).andExpect(status().isOk());
        long before=leads.count();
        mvc.perform(post("/formularios/"+f.getId()).with(csrf()).param("name","Cliente").param("email","cliente@example.com").param("phone","34999999999").param("message","Teste").param("consent","true").param("horario","Outra opção")).andExpect(status().isOk()).andExpect(model().attributeHasErrors("leadForm"));
        assertThat(leads.count()).isEqualTo(before);
        mvc.perform(post("/formularios/"+f.getId()).with(csrf()).param("name","Cliente").param("email","cliente@example.com").param("phone","34999999999").param("message","Teste").param("consent","true").param("horario","Manhã")).andExpect(status().is3xxRedirection());
        assertThat(leads.count()).isEqualTo(before+1);
        mvc.perform(get("/admin/forms/"+f.getId()+"/edit")).andExpect(status().isOk());
        mvc.perform(post("/admin/forms/"+f.getId()+"/delete").with(csrf())).andExpect(status().is3xxRedirection());
    }
    @Test @WithMockUser(roles="ADMIN") void leadStatusAndDeletion()throws Exception {
        Lead l=new Lead();
        l.setKind(LeadKind.CONTACT);
        l.setName("Teste status");
        l.setEmail("status@example.com");
        l.setPhone("34999999999");
        l.setConsent(true);
        l=leads.save(l);
        mvc.perform(get("/admin/leads/"+l.getId())).andExpect(status().isOk());
        mvc.perform(post("/admin/leads/"+l.getId()+"/status").with(csrf()).param("status","IN_PROGRESS")).andExpect(status().is3xxRedirection());
        assertThat(leads.findById(l.getId()).orElseThrow().getStatus()).isEqualTo(LeadStatus.IN_PROGRESS);
        mvc.perform(post("/admin/leads/"+l.getId()+"/delete").with(csrf())).andExpect(status().is3xxRedirection());
        assertThat(leads.findById(l.getId())).isEmpty();
    }
    @Test @WithMockUser(roles="ADMIN") void settingsPersist()throws Exception {
        mvc.perform(post("/admin/settings").with(csrf()).param("email","contato@example.com").param("phone","34999999999").param("whatsapp","5534999999999").param("city","Uberaba").param("about","Sobre a empresa")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/sobre")).andExpect(content().string(org.hamcrest.Matchers.containsString("Sobre a empresa")));
    }
    @Test @WithMockUser(roles="ADMIN") void invalidUploadRejected()throws Exception {
        var file=new org.springframework.mock.web.MockMultipartFile("file","arquivo.txt","text/plain","hello".getBytes());
        mvc.perform(multipart("/admin/media").file(file).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
    }
    @Test @WithMockUser(roles="ADMIN") void validImageUpload()throws Exception {
        var image=new java.awt.image.BufferedImage(10,10,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var buffer=new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image,"png",buffer);
        var file=new org.springframework.mock.web.MockMultipartFile("file","foto.png","image/png",buffer.toByteArray());
        mvc.perform(multipart("/admin/media").file(file).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeExists("uploadedUrl"));
    }
}
