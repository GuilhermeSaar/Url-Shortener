package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UrlModel(Long nextId, String code, String longUrl) {
        this.id = nextId;
        this.code = code;
        this.urlOriginal = longUrl;
        this.clickCount = 0L;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
