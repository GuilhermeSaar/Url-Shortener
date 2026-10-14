package com.urlshortener.service;

import com.urlshortener.exception.UrlNotFoundException;
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
        urlModel.setClickCount(0L);

        repository.save(urlModel);

        String code = sqids.encode(List.of(urlModel.getId()));

        urlModel.setCode(code);
        repository.save(urlModel);

        return code;
    }

    @Transactional(readOnly = true)
    public String findUrlOriginalByCode(String code) {

        var urlLong = repository.findByCode(code).orElseThrow(
                () -> new UrlNotFoundException("URL não encontrada")
        );

        return urlLong.getUrlOriginal();
    }
}
