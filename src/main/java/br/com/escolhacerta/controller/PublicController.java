package br.com.escolhacerta.controller;

import br.com.escolhacerta.service.*;
import br.com.escolhacerta.model.*;
import br.com.escolhacerta.dto.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller public class PublicController {
    private final ContentService content;
    private final LeadService leads;
    private final FormService forms;
    public PublicController(ContentService content,LeadService leads,FormService forms) {
        this.content=content;
        this.leads=leads;
        this.forms=forms;
    }
    @GetMapping("/") public String home(Model m) {
        m.addAttribute("services",content.published(ContentKind.SERVICE));
        m.addAttribute("posts",content.published(ContentKind.POST));
        m.addAttribute("testimonials",content.published(ContentKind.TESTIMONIAL));
        return "home";
    }
    @GetMapping( {
        "/servicos","/blog","/depoimentos"
    }
    ) public String listing(jakarta.servlet.http.HttpServletRequest r,@RequestParam(defaultValue="0") int page,Model m) {
        String path=r.getRequestURI().substring(r.getContextPath().length());
        ContentKind kind=path.equals("/servicos")?ContentKind.SERVICE:path.equals("/blog")?ContentKind.POST:ContentKind.TESTIMONIAL;
        var results = content.publicPage(kind, page);
        m.addAttribute("items",results.getContent());
        m.addAttribute("resultPage", results);
        m.addAttribute("listingPath", path);
        m.addAttribute("kind",kind);
        m.addAttribute("title",path.equals("/servicos")?"Cuidado para cada necessidade":path.equals("/blog")?"Informação que acolhe":"Histórias de cuidado");
        return "listing";
    }
    @GetMapping("/blog/{id}") public String post(@PathVariable long id,Model m) {
        m.addAttribute("post",content.publicPost(id));
        return "post";
    }
    @GetMapping("/sobre") public String about() {
        return "about";
    }
    @GetMapping("/privacidade") public String privacy() {
        return "privacy";
    }
    @GetMapping("/login") public String login() {
        return "login";
    }
    private LeadKind kind(String path) {
        return path.equals("/orcamento")?LeadKind.QUOTE:path.equals("/sou-cuidador")?LeadKind.CAREGIVER:LeadKind.CONTACT;
    }
    private void formModel(String path,Model m) {
        m.addAttribute("action",path);
        m.addAttribute("title",path.equals("/orcamento")?"Vamos encontrar o cuidado ideal?":path.equals("/sou-cuidador")?"Seu talento pode transformar vidas":"Vamos conversar");
        m.addAttribute("caregiver",path.equals("/sou-cuidador"));
    }
    @GetMapping( {
        "/orcamento","/sou-cuidador","/contato"
    }
    ) public String form(jakarta.servlet.http.HttpServletRequest r,Model m) {
        m.addAttribute("leadForm",new LeadForm());
        formModel(r.getRequestURI().substring(r.getContextPath().length()),m);
        return "lead-form";
    }
    @PostMapping( {
        "/orcamento","/sou-cuidador","/contato"
    }
    ) public String submit(jakarta.servlet.http.HttpServletRequest r,@Valid @ModelAttribute("leadForm") LeadForm f,BindingResult errors,Model m,RedirectAttributes flash) {
        formModel(r.getRequestURI().substring(r.getContextPath().length()),m);
        if(errors.hasErrors())return "lead-form";
        leads.submit(kind(r.getRequestURI().substring(r.getContextPath().length())),f,null,null);
        flash.addFlashAttribute("success","Recebemos suas informações. Nossa equipe entrará em contato.");
        return "redirect:"+r.getRequestURI().substring(r.getContextPath().length());
    }
    @GetMapping("/formularios/{id}") public String custom(@PathVariable long id,Model m) {
        CustomForm f=publicForm(id);
        m.addAttribute("custom",f);
        m.addAttribute("fields",forms.fields(f.getFieldsJson()));
        m.addAttribute("leadForm",new LeadForm());
        m.addAttribute("answers",java.util.Map.of());
        return "custom-form";
    }
    private CustomForm publicForm(long id) {
        CustomForm f=forms.get(id);
        if(!f.getPublished())throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
        return f;
    }
    @PostMapping("/formularios/{id}") public String customSubmit(@PathVariable long id,@Valid @ModelAttribute("leadForm") LeadForm dto,BindingResult errors,@RequestParam java.util.Map<String,String> values,Model m,RedirectAttributes flash) {
        CustomForm f=publicForm(id);
        m.addAttribute("custom",f);
        m.addAttribute("fields",forms.fields(f.getFieldsJson()));
        m.addAttribute("answers",values);
        String details=null;
        try {
            details=forms.answers(f,values);
        }
        catch(IllegalArgumentException ex) {
            errors.reject("fields",ex.getMessage());
        }
        if(errors.hasErrors())return "custom-form";
        leads.submit(LeadKind.CUSTOM,dto,id,details);
        flash.addFlashAttribute("success","Formulário enviado com sucesso.");
        return "redirect:/formularios/"+id;
    }
}
