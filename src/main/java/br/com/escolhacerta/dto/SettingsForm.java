package br.com.escolhacerta.dto;

import jakarta.validation.constraints.*;

public class SettingsForm {
    @Email @Size(max=180) public String email;
    @Size(max=30) public String phone;
    @Pattern(regexp="[0-9]{0,15}",message="Use apenas números, incluindo DDI e DDD") public String whatsapp;
    @Size(max=120) public String city;
    @Size(max=5000) public String about;
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
    public String getWhatsapp() {
        return whatsapp;
    }
    public void setWhatsapp(String v) {
        whatsapp=v;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String v) {
        city=v;
    }
    public String getAbout() {
        return about;
    }
    public void setAbout(String v) {
        about=v;
    }
}
