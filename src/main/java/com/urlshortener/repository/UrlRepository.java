package com.urlshortener.repository;

import com.urlshortener.model.UrlModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlModel, Long> {
}
