package com.urlshortener.service;

import com.urlshortener.shared.exception.ResourceNotFound;
import com.urlshortener.url.dto.UrlStatsResponse;
import com.urlshortener.url.model.UrlModel;
import com.urlshortener.url.repository.UrlRepository;
import com.urlshortener.url.service.UrlService;
import com.urlshortener.user.model.UserModel;
import com.urlshortener.user.repository.UserRepository;
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
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UrlService urlService;

    @Test
    void ShouldThrowExceptionWhenUrlIsNullOrBlank() {

        assertThrows(IllegalArgumentException.class, () -> {
            urlService.createShortenUrl(null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            urlService.createShortenUrl(" ", null);
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

        var result =  urlService.createShortenUrl(longUrl, null);

        assertNotNull(result);
        assertEquals(fakeCode, result);

        verify(repository, times(1)).getNextSequenceId();
        verify(repository, times(1)).save(any(UrlModel.class));
        verifyNoInteractions(userRepository);
        var urlCaptor = org.mockito.ArgumentCaptor.forClass(UrlModel.class);
        verify(repository).save(urlCaptor.capture());
        assertNull(urlCaptor.getValue().getUser());
    }

    @Test
    void shouldAssociateShortenedUrlWithAuthenticatedUser() {
        var longUrl = "https://exemplo.com/pagina-da-conta";
        var email = "owner@example.com";
        var user = new UserModel();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(repository.getNextSequenceId()).thenReturn(2L);
        when(sqids.encode(List.of(2L))).thenReturn("k1sd5p");

        urlService.createShortenUrl(longUrl, email);

        var urlCaptor = org.mockito.ArgumentCaptor.forClass(UrlModel.class);
        verify(repository).save(urlCaptor.capture());
        assertSame(user, urlCaptor.getValue().getUser());
    }

    @Test
    void shouldReturnOriginalUrlAndIncrementClickCount() {
        var code = "k1sd5o";
        var originalUrl = "https://exemplo.com/pagina-original";
        var urlModel = new UrlModel(1L, code, originalUrl, null);

        when(repository.findByCode(code)).thenReturn(Optional.of(urlModel));

        var result = urlService.originalUrl(code);

        assertEquals(originalUrl, result);
        verify(repository).findByCode(code);
        verify(repository).incrementClickCount(code);
    }

    @Test
    void shouldThrowWhenOriginalUrlCodeIsNull() {
        assertThrows(ResourceNotFound.class, () -> urlService.originalUrl(null));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowWhenOriginalUrlDoesNotExist() {
        var code = "inexistente";
        when(repository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> urlService.originalUrl(code));

        verify(repository).findByCode(code);
        verify(repository, never()).incrementClickCount(anyString());
    }

    @Test
    void shouldReturnUrlStats() {
        var code = "k1sd5o";
        var createdAt = LocalDateTime.of(2026, 10, 19, 12, 0);
        var originalUrl = "https://exemplo.com/pagina-original";
        var clickCount = 7L;
        var user = new UserModel();
        var urlModel = new UrlModel(1L, code, originalUrl, user);
        urlModel.setCreatedAt(createdAt);
        urlModel.setClickCount(clickCount);
        when(repository.findByCodeAndUser(code, user)).thenReturn(Optional.of(urlModel));

        UrlStatsResponse result = urlService.getUrlStats(user, code);

        assertEquals(createdAt, result.date());
        assertEquals(clickCount, result.clickCount());
        assertEquals(originalUrl, result.urlOriginal());
        verify(repository).findByCodeAndUser(code, user);
        verify(repository, never()).incrementClickCount(anyString());
    }

    @Test
    void shouldThrowWhenStatsCodeDoesNotExist() {
        var code = "inexistente";
        var user = new UserModel();
        when(repository.findByCodeAndUser(code, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> urlService.getUrlStats(user, code));

        verify(repository).findByCodeAndUser(code, user);
    }
}
