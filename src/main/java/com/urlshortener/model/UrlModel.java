package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "urls")
public class UrlModel implements Persistable<Long> {

    @Id
    private Long id;

    @Column(nullable = false, length = 6, unique = true)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String urlOriginal;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long clickCount = 0L;

    @Column(nullable = false)
    private Instant expiresAt;

    @Transient
    private boolean newEntity = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UrlModel(Long nextId, String code, String longUrl) {
        this.id = nextId;
        this.code = code;
        this.urlOriginal = longUrl;
        this.clickCount = 0L;
    }

    @PostPersist
    @PostLoad
    private void markNotNew() {
        this.newEntity = false;
    }

    @PrePersist
    protected void prePersist() {
        this.expiresAt = Instant.now().plus(90, ChronoUnit.DAYS);
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
