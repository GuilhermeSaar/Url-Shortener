package com.urlshortener.service;

import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.UrlModel;
import com.urlshortener.repository.UrlRepository;
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

        var urlModel = new UrlModel();
        urlModel.setUrlOriginal(longUrl);
        repository.save(urlModel);
        String code = sqids.encode(List.of(urlModel.getId()));
        urlModel.setCode(code);

        return code;
    }

    @Transactional
    public String findUrlOriginalByCode(String code) {

        if (code == null) throw new ResourceNotFoundException("O código da url nao pode ser nulo");

        var urlLong = repository.findByCode(code).orElseThrow(
                () -> new ResourceNotFoundException("URL não encontrada")
        );

        repository.incrementClickCount(code);
        return urlLong.getUrlOriginal();
    }

    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String code) {

        var urlModel = repository.findByCode(code).orElseThrow(
                () -> new ResourceNotFoundException("Recurso nao encontrado")
        );

        return new UrlStatsResponse(urlModel.getCreatedAt(), urlModel.getClickCount(), urlModel.getUrlOriginal());
    }
}
