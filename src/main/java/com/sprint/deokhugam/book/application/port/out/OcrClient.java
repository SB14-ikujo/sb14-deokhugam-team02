package com.sprint.deokhugam.book.application.port.out;

public interface OcrClient {

    String readText(byte[] image, String contentType);
}
