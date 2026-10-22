package com.urlshortener.url.service;

import com.urlshortener.url.dto.UrlStatsResponse;
import com.urlshortener.url.exception.UrlNotFoundException;
import com.urlshortener.url.model.UrlModel;
import com.urlshortener.url.repository.UrlRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sqids.Sqids;

import java.util.List;

@Service
@AllArgsConstructor
public class UrlService {

    private final Sqids sqids;
    private final UrlRepository repository;

    @Transactional
    public String createShortenUrl(String longUrl) {

        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("Url inválida");
        }

        var nextId = repository.getNextSequenceId();
        var code = sqids.encode(List.of(nextId));

        var urlModel = new UrlModel(nextId, code, longUrl);
        repository.save(urlModel);
        return code;
    }

    @Transactional
    public String originalUrl(String code) {

        if (code == null) throw new UrlNotFoundException("O código da url nao pode ser nulo");

        var urlLong = repository.findByCode(code).orElseThrow(
                () -> new UrlNotFoundException("URL não encontrada")
        );

        repository.incrementClickCount(code);
        return urlLong.getUrlOriginal();
    }

    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String code) {

        var urlModel = repository.findByCode(code).orElseThrow(
                () -> new UrlNotFoundException("Recurso nao encontrado")
        );

        return new UrlStatsResponse(urlModel.getCreatedAt(), urlModel.getClickCount(), urlModel.getUrlOriginal());
    }
}
