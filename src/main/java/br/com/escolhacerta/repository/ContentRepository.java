package br.com.escolhacerta.repository;

import br.com.escolhacerta.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content,Long> {
    org.springframework.data.domain.Slice<br.com.escolhacerta.dto.PublicContent> findByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDescIdDesc(ContentKind kind, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Slice<br.com.escolhacerta.dto.PublicServiceContent> findServiceCardsByKindAndPublishedTrueOrderBySortOrderAscCreatedAtDescIdDesc(ContentKind kind, org.springframework.data.domain.Pageable pageable);
    java.util.List<Content> findByKindOrderBySortOrderAscCreatedAtDesc(ContentKind kind);
}
