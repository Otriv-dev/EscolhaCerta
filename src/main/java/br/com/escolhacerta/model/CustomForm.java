package br.com.escolhacerta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="custom_forms") public class CustomForm {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long v) {
        id=v;
    }
    @Column(nullable=false,length=180) private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String v) {
        title=v;
    }
    @Column(length=500) private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String v) {
        description=v;
    }
    @Column(columnDefinition="LONGTEXT") private String fieldsJson;
    public String getFieldsJson() {
        return fieldsJson;
    }
    public void setFieldsJson(String v) {
        fieldsJson=v;
    }
    @Column(nullable=false) private boolean published = false;
    public boolean getPublished() {
        return published;
    }
    public void setPublished(boolean v) {
        published=v;
    }
}
