package br.com.escolhacerta.controller;

import br.com.escolhacerta.repository.*;
import br.com.escolhacerta.model.*;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice public class GlobalViewAdvice {
    private final SiteSettingsRepository settings;
    private final br.com.escolhacerta.service.PublicFormMenu forms;
    public GlobalViewAdvice(SiteSettingsRepository settings,br.com.escolhacerta.service.PublicFormMenu forms) {
        this.settings=settings;
        this.forms=forms;
    }
    @ModelAttribute("site") public SiteSettings site() {
        return settings.findById(1L).orElse(new SiteSettings());
    }
    @ModelAttribute("publicForms") public java.util.List<br.com.escolhacerta.dto.PublicFormLink> forms() {
        return forms.links();
    }
}
