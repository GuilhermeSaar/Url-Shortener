package com.urlshortener.repository;

import com.urlshortener.model.UrlModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlModel, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE UrlModel u SET u.clickCount = u.clickCount + 1 WHERE u.code = :code")
    void incrementClickCount(@Param("code") String code);

    Optional<UrlModel> findByCode(String code);

}
