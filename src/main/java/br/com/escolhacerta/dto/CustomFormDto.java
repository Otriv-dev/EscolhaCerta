package br.com.escolhacerta.dto;

import jakarta.validation.constraints.*;

public class CustomFormDto {
    @NotBlank @Size(max=180) public String title;
    @Size(max=500) public String description;
    @NotBlank @Size(max=12000) public String fieldsJson;
    public boolean published;
    public String getTitle() {
        return title;
    }
    public void setTitle(String v) {
        title=v;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String v) {
        description=v;
    }
    public String getFieldsJson() {
        return fieldsJson;
    }
    public void setFieldsJson(String v) {
        fieldsJson=v;
    }
    public boolean getPublished() {
        return published;
    }
    public void setPublished(boolean v) {
        published=v;
    }
}
