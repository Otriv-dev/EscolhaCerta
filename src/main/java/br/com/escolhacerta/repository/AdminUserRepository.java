package br.com.escolhacerta.repository;

import br.com.escolhacerta.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser,Long> {
    java.util.Optional<AdminUser> findByEmail(String email);
}
