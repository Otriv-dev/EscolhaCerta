package br.com.escolhacerta.controller;

import br.com.escolhacerta.service.*;
import br.com.escolhacerta.repository.*;
import br.com.escolhacerta.model.*;
import br.com.escolhacerta.dto.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;

@Controller @org.springframework.transaction.annotation.Transactional @RequestMapping("/admin") public class AdminController {
    private final org.springframework.security.core.session.SessionRegistry sessions;
    private final AuditService audit;
    private final PublicFormMenu menu;
    private final ContentService content;
    private final LeadService leads;
    private final LeadRepository inbox;
    private final FormService forms;
    private final CustomFormRepository formRepo;
    private final SiteSettingsRepository settings;
    private final UploadService uploads;
    private final AdminUserRepository users;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;
    public AdminController(ContentService content,LeadService leads,LeadRepository inbox,FormService forms,CustomFormRepository formRepo,SiteSettingsRepository settings,UploadService uploads,AdminUserRepository users,org.springframework.security.crypto.password.PasswordEncoder encoder,org.springframework.security.core.session.SessionRegistry sessions,AuditService audit,PublicFormMenu menu) {
        this.content=content;
        this.leads=leads;
        this.inbox=inbox;
        this.forms=forms;
        this.formRepo=formRepo;
        this.settings=settings;
        this.uploads=uploads;
        this.users=users;
        this.encoder=encoder;
        this.sessions=sessions;
        this.audit=audit;
        this.menu=menu;
    }
    @GetMapping public String dashboard(Model m) {
        m.addAttribute("total",inbox.count());
        m.addAttribute("newCount",inbox.countByStatus(LeadStatus.NEW));
        m.addAttribute("contentCount",content.all(ContentKind.POST).size());
        m.addAttribute("recent",inbox.findAll(PageRequest.of(0,8,org.springframework.data.domain.Sort.by("createdAt").descending())).getContent());
        return "admin/dashboard";
    }
    @GetMapping("/contents/{kind}") public String contents(@PathVariable ContentKind kind,Model m) {
        m.addAttribute("items",content.all(kind));
        m.addAttribute("kind",kind);
        return "admin/contents";
    }
    @GetMapping("/contents/{kind}/new") public String contentNew(@PathVariable ContentKind kind,Model m) {
        m.addAttribute("contentForm",new ContentForm());
        m.addAttribute("kind",kind);
        return "admin/content-edit";
    }
    @GetMapping("/contents/{kind}/{id}/edit") public String contentEdit(@PathVariable ContentKind kind,@PathVariable long id,Model m) {
        Content c=content.get(id);
        if(c.getKind()!=kind)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        ContentForm f=new ContentForm();
        f.title=c.getTitle();
        f.summary=c.getSummary();
        f.body=c.getBody();
        f.imageUrl=c.getImageUrl();
        f.published=c.getPublished();
        f.sortOrder=c.getSortOrder();
        m.addAttribute("contentForm",f);
        m.addAttribute("kind",kind);
        m.addAttribute("id",id);
        return "admin/content-edit";
    }
    @PostMapping("/contents/{kind}/save") public String contentSave(@PathVariable ContentKind kind,@RequestParam(required=false) Long id,@Valid @ModelAttribute("contentForm") ContentForm f,BindingResult errors,Model m,RedirectAttributes flash) {
        m.addAttribute("kind",kind);
        m.addAttribute("id",id);
        if(errors.hasErrors())return "admin/content-edit";
        content.save(kind,id,f);
        flash.addFlashAttribute("success","Conteúdo salvo.");
        return "redirect:/admin/contents/"+kind;
    }
    @PostMapping("/contents/{kind}/{id}/delete") public String contentDelete(@PathVariable ContentKind kind,@PathVariable long id) {
        if(content.get(id).getKind()!=kind)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
        content.delete(id);
        return "redirect:/admin/contents/"+kind;
    }
    @GetMapping("/leads") public String inbox(@RequestParam(required=false) LeadKind kind,@RequestParam(defaultValue="0") int page,Model m) {
        var pageable=PageRequest.of(Math.max(0,page),20,org.springframework.data.domain.Sort.by("createdAt").descending());
        m.addAttribute("page",kind==null?inbox.findAll(pageable):inbox.findByKind(kind,pageable));
        m.addAttribute("kind",kind);
        return "admin/leads";
    }
    @GetMapping("/leads/{id}") public String lead(@PathVariable long id,Model m) {
        m.addAttribute("lead",leads.get(id));
        return "admin/lead-detail";
    }
    @PostMapping("/leads/{id}/status") public String status(@PathVariable long id,@RequestParam LeadStatus status) {
        leads.status(id,status);
        return "redirect:/admin/leads/"+id;
    }
    @PostMapping("/leads/{id}/delete") public String deleteLead(@PathVariable long id) {
        leads.delete(id);
        return "redirect:/admin/leads";
    }
    @GetMapping("/forms") public String forms(Model m) {
        m.addAttribute("items",formRepo.findAll());
        return "admin/forms";
    }
    @GetMapping("/forms/new") public String formNew(Model m) {
        CustomFormDto f=new CustomFormDto();
        f.fieldsJson="[{\"name\":\"necessidade\",\"label\":\"Qual sua necessidade?\",\"type\":\"text\",\"required\":true}]";
        m.addAttribute("formDto",f);
        return "admin/form-edit";
    }
    @GetMapping("/forms/{id}/edit") public String formEdit(@PathVariable long id,Model m) {
        CustomForm f=forms.get(id);
        CustomFormDto dto=new CustomFormDto();
        dto.title=f.getTitle();
        dto.description=f.getDescription();
        dto.fieldsJson=f.getFieldsJson();
        dto.published=f.getPublished();
        m.addAttribute("formDto",dto);
        m.addAttribute("id",id);
        return "admin/form-edit";
    }
    @PostMapping("/forms/save") public String formSave(@RequestParam(required=false) Long id,@Valid @ModelAttribute("formDto") CustomFormDto dto,BindingResult errors,Model m,RedirectAttributes flash) {
        m.addAttribute("id",id);
        if(!errors.hasErrors())try {
            forms.save(id,dto);
        }
        catch(IllegalArgumentException ex) {
            errors.rejectValue("fieldsJson","invalid",ex.getMessage());
        }
        if(errors.hasErrors())return "admin/form-edit";
        flash.addFlashAttribute("success","Formulário salvo.");
        return "redirect:/admin/forms";
    }
    @PostMapping("/forms/{id}/delete") public String formDelete(@PathVariable long id) {
        formRepo.delete(forms.get(id));
        menu.invalidate();
        audit.record("FORM_DELETED", Long.toString(id));
        return "redirect:/admin/forms";
    }
    @GetMapping("/settings") public String settings(Model m) {
        SiteSettings s=settings.findById(1L).orElseThrow();
        SettingsForm f=new SettingsForm();
        f.email=s.getEmail();
        f.phone=s.getPhone();
        f.whatsapp=s.getWhatsapp();
        f.city=s.getCity();
        f.about=s.getAbout();
        m.addAttribute("settingsForm",f);
        return "admin/settings";
    }
    @PostMapping("/settings") public String settingsSave(@Valid @ModelAttribute("settingsForm") SettingsForm f,BindingResult errors,RedirectAttributes flash) {
        if(errors.hasErrors())return "admin/settings";
        SiteSettings s=settings.findById(1L).orElseThrow();
        s.setEmail(f.email);
        s.setPhone(f.phone);
        s.setWhatsapp(f.whatsapp);
        s.setCity(f.city);
        s.setAbout(f.about);
        settings.save(s);
        audit.record("SETTINGS_CHANGED", "1");
        flash.addFlashAttribute("success","Dados atualizados.");
        return "redirect:/admin/settings";
    }
    @GetMapping("/media") public String media() {
        return "admin/media";
    }
    @PostMapping("/media") public String upload(@RequestParam org.springframework.web.multipart.MultipartFile file,Model m) {
        try {
            String uploaded = uploads.upload(file);
            m.addAttribute("uploadedUrl",uploaded);
            audit.record("MEDIA_UPLOADED", uploaded.substring(uploaded.lastIndexOf('/') + 1));
        }
        catch(IllegalArgumentException ex) {
            m.addAttribute("error",ex.getMessage());
        }
        return "admin/media";
    }
    @GetMapping("/account") public String account() {
        return "admin/account";
    }
    @PostMapping("/account") public String password(@RequestParam String currentPassword,@RequestParam String newPassword,@RequestParam String confirmPassword,java.security.Principal principal,Model m,RedirectAttributes flash,jakarta.servlet.http.HttpServletRequest request) {
        AdminUser user=users.findByEmail(principal.getName()).orElseThrow();
        if(!encoder.matches(currentPassword,user.getPasswordHash())||!newPassword.equals(confirmPassword)) {
            audit.record("PASSWORD_CHANGE_DENIED", user.getId().toString());
            m.addAttribute("error","Verifique a senha atual e a confirmação da nova senha.");
            return "admin/account";
        }
        try { br.com.escolhacerta.security.PasswordPolicy.validate(newPassword); }
        catch (IllegalArgumentException ex) {
            audit.record("PASSWORD_CHANGE_DENIED", user.getId().toString());
            m.addAttribute("error", ex.getMessage());
            return "admin/account";
        }
        user.setPasswordHash(encoder.encode(newPassword));
        users.save(user);
        audit.record("PASSWORD_CHANGED", user.getId().toString());
        for (Object authenticated : sessions.getAllPrincipals()) {
            if (authenticated instanceof org.springframework.security.core.userdetails.UserDetails details
                && details.getUsername().equals(user.getEmail())) {
                sessions.getAllSessions(authenticated, false).forEach(info -> info.expireNow());
            }
        }
        var currentSession = request.getSession(false);
        if (currentSession != null) currentSession.invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return "redirect:/login?passwordChanged";
    }
}
