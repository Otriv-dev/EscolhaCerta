package br.com.escolhacerta.dto;

import jakarta.validation.constraints.*;

public class ContentForm {
    @NotBlank @Size(max=180) public String title;
    @Size(max=500) public String summary;
    @Size(max=30000) public String body;
    @Size(max=1000) @Pattern(regexp="^$|^https://[^\\s]+$|^/media/[a-f0-9-]+\\.(png|jpg)$",message="Use uma URL HTTPS ou uma imagem enviada pelo painel") public String imageUrl="";
    public boolean published;
    @Min(0) @Max(10000) public int sortOrder;
    public String getTitle() {
        return title;
    }
    public void setTitle(String v) {
        title=v;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String v) {
        summary=v;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String v) {
        body=v;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String v) {
        imageUrl=v;
    }
    public boolean getPublished() {
        return published;
    }
    public void setPublished(boolean v) {
        published=v;
    }
    public int getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(int v) {
        sortOrder=v;
    }
}
