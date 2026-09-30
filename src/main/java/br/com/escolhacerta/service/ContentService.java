package br.com.escolhacerta.service;

import br.com.escolhacerta.model.*;
import br.com.escolhacerta.dto.*;
import br.com.escolhacerta.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service @Transactional public class ContentService {
    private final AuditService audit;
    private final ContentRepository repo;
    public ContentService(ContentRepository repo,AuditService audit) {
        this.repo=repo;
        this.audit=audit;
    }
    public java.util.List<br.com.escolhacerta.dto.PublicContent> published(ContentKind kind) {
        return repo.findByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDescIdDesc(kind, org.springframework.data.domain.PageRequest.of(0, kind == ContentKind.POST ? 3 : 12)).getContent();
    }
    public org.springframework.data.domain.Slice<? extends br.com.escolhacerta.dto.PublicContent> publicPage(ContentKind kind, int page) {
        var pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, Math.min(page, 10000)), 12);
        if (kind == ContentKind.SERVICE) return repo.findServiceCardsByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDescIdDesc(kind, pageable);
        return repo.findByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDescIdDesc(kind, pageable);
    }
    public java.util.List<Content> all(ContentKind kind) {
        return repo.findByKindOrderBySortOrderAscCreatedAtDesc(kind);
    }
    public Content get(long id) {
        return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    public Content publicPost(long id) {
        Content c=get(id);
        if(!c.getPublished()||c.getKind()!=ContentKind.POST)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return c;
    }
    public Content save(ContentKind kind,Long id,ContentForm f) {
        Content c=id==null?new Content():get(id);
        if(id!=null&&c.getKind()!=kind)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        c.setKind(kind);
        c.setTitle(f.title);
        c.setSummary(f.summary);
        c.setBody(f.body);
        c.setImageUrl(f.imageUrl);
        c.setPublished(f.published);
        c.setSortOrder(f.sortOrder);
        Content saved=repo.save(c);
        audit.record("CONTENT_SAVED", saved.getId().toString());
        return saved;
    }
    public void delete(long id) {
        repo.delete(get(id));
        audit.record("CONTENT_DELETED", Long.toString(id));
    }
}
