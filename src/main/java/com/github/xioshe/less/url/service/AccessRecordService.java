package com.github.xioshe.less.url.service;


import com.github.xioshe.less.url.entity.AccessRecord;
import com.github.xioshe.less.url.repository.AccessRecordRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessRecordService {

    private final AccessRecordRepository accessRecordRepository;
    private final Clock globalClock;

    public void record(String url, HttpServletRequest request) {
        AccessRecord accessRecord = new AccessRecord();
        accessRecord.setShortUrl(url);
        accessRecord.setUserAgent(request.getHeader("User-Agent"));
        accessRecord.setIp(request.getRemoteAddr());
        accessRecord.setReferer(request.getHeader("referer"));
        accessRecord.setAccessTime(LocalDateTime.now(globalClock));
        accessRecordRepository.save(accessRecord);
    }

}
