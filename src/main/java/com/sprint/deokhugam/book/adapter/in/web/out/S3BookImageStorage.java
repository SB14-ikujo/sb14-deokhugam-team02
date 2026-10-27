package com.sprint.deokhugam.book.adapter.in.web.out;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.book.application.port.out.BookImageStorage;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class S3BookImageStorage implements BookImageStorage {

    @Override
    public String generateImageKey(String originalFilename) {
        return "";
    }

    @Override
    public String upload(UUID imageKey, byte[] value) {
        return "";
    }

    @Override
    public String getImageUrl(String imageKey) {
        return "";
    }

    @Override
    public void delete(String imageKey) {

    }
}
