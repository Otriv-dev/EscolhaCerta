package br.com.escolhacerta.repository;

import br.com.escolhacerta.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead,Long> {
    org.springframework.data.domain.Page<Lead> findByKind(LeadKind kind, org.springframework.data.domain.Pageable pageable);
    long countByStatus(LeadStatus status);
}
