package com.urlshortener.service;

import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.UrlModel;
import com.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sqids.Sqids;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {

    @Mock
    private UrlRepository repository;
    @Mock
    private Sqids sqids;
    @InjectMocks
    private UrlService urlService;

    @Test
    void ShouldThrowExceptionWhenUrlIsNullOrBlank() {

        assertThrows(IllegalArgumentException.class, () -> {
            urlService.createShortenUrl(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            urlService.createShortenUrl(" ");
        });

        verifyNoInteractions(repository);
    }

    @Test
    void ShouldCreateShortenedUrl() {

        var longUrl = "https://exemplo.com/pagina-muito-longa";
        var fakeId = 1L;
        var fakeCode = "k1sd5o";

        when(repository.getNextSequenceId()).thenReturn(fakeId);
        when(sqids.encode(List.of(fakeId))).thenReturn(fakeCode);

        var result =  urlService.createShortenUrl(longUrl);

        assertNotNull(result);
        assertEquals(fakeCode, result);

        verify(repository, times(1)).getNextSequenceId();
        verify(repository, times(1)).save(any(UrlModel.class));
    }

    @Test
    void shouldReturnOriginalUrlAndIncrementClickCount() {
        var code = "k1sd5o";
        var originalUrl = "https://exemplo.com/pagina-original";
        var urlModel = new UrlModel(1L, code, originalUrl);

        when(repository.findByCode(code)).thenReturn(Optional.of(urlModel));

        var result = urlService.originalUrl(code);

        assertEquals(originalUrl, result);
        verify(repository).findByCode(code);
        verify(repository).incrementClickCount(code);
    }

    @Test
    void shouldThrowWhenOriginalUrlCodeIsNull() {
        assertThrows(ResourceNotFoundException.class, () -> urlService.originalUrl(null));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowWhenOriginalUrlDoesNotExist() {
        var code = "inexistente";
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> urlService.originalUrl(code));

        verify(repository).findByCode(code);
        verify(repository, never()).incrementClickCount(anyString());
    }

    @Test
    void shouldReturnUrlStats() {
        var code = "k1sd5o";
        var createdAt = LocalDateTime.of(2026, 10, 19, 12, 0);
        var originalUrl = "https://exemplo.com/pagina-original";
        var clickCount = 7L;
        var urlModel = new UrlModel(1L, code, originalUrl);
        urlModel.setCreatedAt(createdAt);
        urlModel.setClickCount(clickCount);
        when(repository.findByCode(code)).thenReturn(Optional.of(urlModel));

        UrlStatsResponse result = urlService.getUrlStats(code);

        assertEquals(createdAt, result.date());
        assertEquals(clickCount, result.clickCount());
        assertEquals(originalUrl, result.urlOriginal());
        verify(repository).findByCode(code);
        verify(repository, never()).incrementClickCount(anyString());
    }

    @Test
    void shouldThrowWhenStatsCodeDoesNotExist() {
        var code = "inexistente";
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> urlService.getUrlStats(code));

        verify(repository).findByCode(code);
    }
}
