package com.urlshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "urls",
    indexes = {
        @Index(name = "index_url_code", columnList = "code")
    })
public class UrlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @SequenceGenerator(name = "seq", sequenceName = "seq_url", allocationSize = 1)
    private Long id;

    @Column(length = 7, unique = true)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String urlOriginal;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long clickCount = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
