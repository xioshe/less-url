package com.github.xioshe.less.url.api;


import com.github.xioshe.less.url.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("url")
public class UrlController {

    private final UrlService urlService;

    @Value("${less.url.host:http://localhost:8080/}")
    private String baseUrl;

    @PostMapping
    public String shorten(@RequestBody CreateUrlCommand command) {
        return baseUrl + urlService.shorten(command.getOriginalUrl(), 1L);
    }

    @DeleteMapping("/{shortUrl}")
    public void delete(@PathVariable String shortUrl) {
        // todo token 获取 apiDevKey
    }

}
