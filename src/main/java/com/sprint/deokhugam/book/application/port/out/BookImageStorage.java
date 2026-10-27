package com.sprint.deokhugam.book.application.port.out;

public interface BookImageStorage {
    String generateImageKey(String originalFilename);

    String upload(String imageKey, byte[] value, String contentType);

    String getImageUrl(String imageKey, String contentType);

    void delete(String imageKey);
}
