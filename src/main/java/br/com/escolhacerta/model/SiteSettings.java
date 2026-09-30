package br.com.escolhacerta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="site_settings") public class SiteSettings {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long v) {
        id=v;
    }
    @Column(length=180) private String email;
    public String getEmail() {
        return email;
    }
    public void setEmail(String v) {
        email=v;
    }
    @Column(length=30) private String phone;
    public String getPhone() {
        return phone;
    }
    public void setPhone(String v) {
        phone=v;
    }
    @Column(length=20) private String whatsapp;
    public String getWhatsapp() {
        return whatsapp;
    }
    public void setWhatsapp(String v) {
        whatsapp=v;
    }
    @Column(length=120) private String city;
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city=v;
    }
    @Column(columnDefinition="LONGTEXT") private String about;
    public String getAbout() {
        return about;
    }
    public void setAbout(String v) {
        about=v;
    }
}
