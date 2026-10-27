package com.sprint.deokhugam.book.application.port.out;

import java.util.UUID;

public interface BookImageStorage {
    String generateImageKey(String originalFilename);

    String upload(UUID imageKey, byte[] value);

    String getImageUrl(String imageKey);

    void delete(String imageKey);
}
