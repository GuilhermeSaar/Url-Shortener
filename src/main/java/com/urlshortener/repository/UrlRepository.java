package com.urlshortener.repository;

import com.urlshortener.model.UrlModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlModel, Long> {

    @Modifying
    @Query("UPDATE UrlModel u SET u.clickCount = u.clickCount + 1 WHERE u.code = :code")
    void incrementClickCount(@Param("code") String code);

    Optional<UrlModel> findByCode(String code);

    @Query(value = "SELECT nextval('seq_url')", nativeQuery = true)
    Long getNextSequenceId();

    void deleteByExpiresAtBefore(Instant now);
}
