package br.com.escolhacerta.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="contents") public class Content {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long v) {
        id=v;
    }
    @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(nullable=false,length=32) private ContentKind kind;
    public ContentKind getKind() {
        return kind;
    }
    public void setKind(ContentKind v) {
        kind=v;
    }
    @Column(nullable=false,length=180) private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String v) {
        title=v;
    }
    @Column(length=500) private String summary;
    public String getSummary() {
        return summary;
    }
    public void setSummary(String v) {
        summary=v;
    }
    @Column(columnDefinition="LONGTEXT") private String body;
    public String getBody() {
        return body;
    }
    public void setBody(String v) {
        body=v;
    }
    @Column(length=1000) private String imageUrl;
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String v) {
        imageUrl=v;
    }
    @Column(nullable=false) private boolean published = false;
    public boolean getPublished() {
        return published;
    }
    public void setPublished(boolean v) {
        published=v;
    }
    private int sortOrder = 0;
    public int getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(int v) {
        sortOrder=v;
    }
    @Column(nullable=false) private Instant createdAt = Instant.now();
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant v) {
        createdAt=v;
    }
}
