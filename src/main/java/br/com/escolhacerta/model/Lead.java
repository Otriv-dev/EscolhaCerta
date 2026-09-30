package br.com.escolhacerta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="leads") public class Lead {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long v) {
        id=v;
    }
    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(nullable=false,length=32) private LeadKind kind;
    public LeadKind getKind() {
        return kind;
    }
    public void setKind(LeadKind v) {
        kind=v;
    }
    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(nullable=false,length=32) private LeadStatus status = LeadStatus.NEW;
    public LeadStatus getStatus() {
        return status;
    }
    public void setStatus(LeadStatus v) {
        status=v;
    }
    @Column(nullable=false,length=120) private String name;
    public String getName() {
        return name;
    }
    public void setName(String v) {
        name=v;
    }
    @Column(nullable=false,length=180) private String email;
    public String getEmail() {
        return email;
    }
    public void setEmail(String v) {
        email=v;
    }
    @Column(nullable=false,length=30) private String phone;
    public String getPhone() {
        return phone;
    }
    public void setPhone(String v) {
        phone=v;
    }
    @Column(length=120) private String city;
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city=v;
    }
    @Column(columnDefinition="LONGTEXT") private String message;
    public String getMessage() {
        return message;
    }
    public void setMessage(String v) {
        message=v;
    }
    @Column(columnDefinition="LONGTEXT") private String details;
    public String getDetails() {
        return details;
    }
    public void setDetails(String v) {
        details=v;
    }
    private Long formId;
    public Long getFormId() {
        return formId;
    }
    public void setFormId(Long v) {
        formId=v;
    }
    @Column(nullable=false) private boolean consent = false;
    public boolean getConsent() {
        return consent;
    }
    public void setConsent(boolean v) {
        consent=v;
    }
    @Column(nullable=false) private Instant createdAt = Instant.now();
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant v) {
        createdAt=v;
    }
}
