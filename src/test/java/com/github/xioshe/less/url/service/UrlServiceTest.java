package com.github.xioshe.less.url.service;

import com.github.xioshe.less.url.repository.UrlRepository;
import com.github.xioshe.less.url.shorter.UrlShorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UrlServiceTest {

    private UrlRepository urlRepository;

    private UrlShorter urlShorter;

    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlRepository = mock(UrlRepository.class);
        urlShorter = mock(UrlShorter.class);
        urlService = new UrlService(urlRepository, urlShorter);
    }

    @Test
    void Shorten_invalid_url() {
        String originalUrl = "invalid-url";

        var e = assertThrows(IllegalArgumentException.class, () -> urlService.shorten(originalUrl, 1L));
        assertEquals("Invalid URL", e.getMessage());
    }

    @Test
    void Shorten_existing_url() {
        String originalUrl = "http://existing.com";
        String decodedUrl = URLDecoder.decode(originalUrl, StandardCharsets.UTF_8);
        String shortUrl = "http://existing-short.com";

        when(urlRepository.getShortUrl(decodedUrl, 1L)).thenReturn(shortUrl);

        String result = urlService.shorten(originalUrl, 1L);

        assertEquals(shortUrl, result);
        verify(urlRepository, never()).save(anyString(), anyString(), anyLong());
    }

    @Test
    void Shorten_url() {
        String originalUrl = "http://example.com";
        String decodedUrl = URLDecoder.decode(originalUrl, StandardCharsets.UTF_8);
        String shortUrl = "http://short.com";

        when(urlRepository.getShortUrl(decodedUrl, 1L)).thenReturn(null);
        when(urlShorter.shorten(decodedUrl)).thenReturn(shortUrl);
        when(urlRepository.existShortUrl(shortUrl)).thenReturn(false);

        String result = urlService.shorten(originalUrl, 1L);

        assertEquals(shortUrl, result);
        verify(urlRepository).save(decodedUrl, shortUrl, 1L);
    }

    @Test
    void Shorten_exceeding_then_fail() {
        String originalUrl = "http://fail.com";
        String decodedUrl = URLDecoder.decode(originalUrl, StandardCharsets.UTF_8);
        String shortUrl = "http://fail-short.com";

        when(urlRepository.getShortUrl(decodedUrl, 1L)).thenReturn(null);
        when(urlShorter.shorten(anyString())).thenReturn(shortUrl);
        when(urlRepository.existShortUrl(shortUrl)).thenReturn(true);

        var e = assertThrows(RuntimeException.class, () -> urlService.shorten(originalUrl, 1L));
        assertEquals("Failed to shorten url", e.getMessage());
    }

    @Test
    void Shorten_conflict_once() {
        String originalUrl = "http://conflict.com";
        String decodedUrl = URLDecoder.decode(originalUrl, StandardCharsets.UTF_8);
        String shortUrl = "http://conflict-short.com";

        when(urlRepository.getShortUrl(decodedUrl, 1L)).thenReturn(null);
        when(urlShorter.shorten(anyString())).thenReturn(shortUrl);
        when(urlRepository.existShortUrl(shortUrl)).thenReturn(true).thenReturn(false);

        String result = urlService.shorten(originalUrl, 1L);

        assertEquals(shortUrl, result);
        verify(urlRepository).save(decodedUrl, shortUrl, 1L);
    }
}
