package br.com.escolhacerta.service;

import br.com.escolhacerta.model.*;
import br.com.escolhacerta.dto.*;
import br.com.escolhacerta.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional public class LeadService {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(LeadService.class);
    private final AuditService audit;
    private final LeadRepository repo;
    public LeadService(LeadRepository repo,AuditService audit) {
        this.repo=repo;
        this.audit=audit;
    }
    public Lead submit(LeadKind kind,LeadForm f,Long formId,String details) {
        if(f.website!=null&&!f.website.isBlank())return null;
        Lead l=new Lead();
        l.setKind(kind);
        l.setName(f.name.trim());
        l.setEmail(f.email.trim().toLowerCase());
        l.setPhone(f.phone);
        l.setCity(f.city);
        l.setMessage(f.message);
        l.setConsent(f.consent);
        l.setFormId(formId);
        l.setDetails(details);
        Lead saved=repo.save(l);
        log.info("lead_received id={} kind={}",saved.getId(),kind);
        return saved;
    }
    public Lead get(long id) {
        return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
    }
    public void status(long id,LeadStatus status) {
        get(id).setStatus(status);
        audit.record("LEAD_STATUS_CHANGED", Long.toString(id));
    }
    public void delete(long id) {
        repo.delete(get(id));
        audit.record("LEAD_DELETED", Long.toString(id));
    }
}
