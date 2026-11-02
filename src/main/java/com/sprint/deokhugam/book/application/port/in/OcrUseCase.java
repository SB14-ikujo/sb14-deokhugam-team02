package com.sprint.deokhugam.book.application.port.in;

public interface OcrUseCase {

    String extractIsbn(byte[] image, String contentType);
}
