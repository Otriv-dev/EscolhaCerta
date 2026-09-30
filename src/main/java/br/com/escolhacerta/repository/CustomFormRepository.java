package br.com.escolhacerta.repository;

import br.com.escolhacerta.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFormRepository extends JpaRepository<CustomForm,Long> {
    java.util.List<br.com.escolhacerta.dto.PublicFormLink> findTop20ByPublishedTrueOrderByIdDesc();
}
