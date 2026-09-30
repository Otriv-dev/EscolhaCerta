package br.com.escolhacerta.repository;

import br.com.escolhacerta.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings,Long> {
}
