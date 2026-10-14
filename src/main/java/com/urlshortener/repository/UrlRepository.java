package com.urlshortener.repository;

import com.urlshortener.model.UrlModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlModel, Long> {

    Optional<UrlModel> findByCode(String code);
}
