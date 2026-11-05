package com.urlshortener.url.service;

import com.urlshortener.shared.exception.ResourceNotFound;
import com.urlshortener.url.dto.UrlStatsResponse;
import com.urlshortener.url.model.UrlModel;
import com.urlshortener.url.repository.UrlRepository;
import com.urlshortener.user.model.UserModel;
import com.urlshortener.user.repository.UserRepository;
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
    private final UserRepository  userRepository;

    @Transactional
    public String createShortenUrl(String longUrl, String userAuth) {

        UserModel user = null;
        if (userAuth != null) {
            user = userRepository.findByEmail(userAuth).orElseThrow(
                    () -> new ResourceNotFound("User not found")
            );
        }

        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("Invalid URL");
        }

        var nextId = urlRepository.getNextSequenceId();
        var code = sqids.encode(List.of(nextId));

        var urlModel = new UrlModel(nextId, code, longUrl, user);
        urlRepository.save(urlModel);
        return code;
    }

    @Transactional
    public String originalUrl(String code) {

        if (code == null) throw new ResourceNotFound("The code cannot be null");

        var urlLong = urlRepository.findByCode(code).orElseThrow(
                () -> new ResourceNotFound("URL not found")
        );

        urlRepository.incrementClickCount(code);
        return urlLong.getUrlOriginal();
    }

    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String code) {

        var urlModel = urlRepository.findByCode(code).orElseThrow(
                () -> new ResourceNotFound("Url not found")
        );

        return new UrlStatsResponse(urlModel.getCreatedAt(), urlModel.getClickCount(), urlModel.getUrlOriginal());
    }
}
