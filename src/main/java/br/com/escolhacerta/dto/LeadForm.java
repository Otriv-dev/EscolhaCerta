package br.com.escolhacerta.dto;

import jakarta.validation.constraints.*;

public class LeadForm {
    @NotBlank @Size(max=120) public String name;
    @NotBlank @Email @Size(max=180) public String email;
    @NotBlank @Pattern(regexp="[+()0-9 .-]{8,30}",message="Informe um telefone válido") public String phone;
    @Size(max=120) public String city;
    @NotBlank @Size(max=5000) public String message;
    @AssertTrue(message="É necessário autorizar o tratamento dos dados") public boolean consent;
    @Size(max=150) public String website;
    public String getName() {
        return name;
    }
    public void setName(String v) {
        name=v;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String v) {
        email=v;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String v) {
        phone=v;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city=v;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String v) {
        message=v;
    }
    public boolean getConsent() {
        return consent;
    }
    public void setConsent(boolean v) {
        consent=v;
    }
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String v) {
        website=v;
    }
}
