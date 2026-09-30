package br.com.escolhacerta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="admin_users") public class AdminUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long v) {
        id=v;
    }
    @Column(nullable=false,unique=true,length=180) private String email;
    public String getEmail() {
        return email;
    }
    public void setEmail(String v) {
        email=v;
    }
    @Column(nullable=false,length=100) private String passwordHash;
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String v) {
        passwordHash=v;
    }
}
