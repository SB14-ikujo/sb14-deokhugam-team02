package com.sprint.deokhugam.book.application.port.out;

public interface BookImageStorage {
    String generateImageKey(String originalFilename);

    String upload(String imageKey, byte[] value);

    String getImageUrl(String imageKey);

    void delete(String imageKey);
}
