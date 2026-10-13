package com.urlshortener.service;

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
    private final UrlRepository urlRepository;


    @Transactional
    public String createShortenUrl(String longUrl) {

        var urlModel = new UrlModel();
        urlModel.setUrl(longUrl);
        urlModel.setClickCount(0L);

        urlRepository.save(urlModel);

        String code = sqids.encode(List.of(urlModel.getId()));

        urlModel.setCode(code);
        urlRepository.save(urlModel);

        return code;
    }
}
