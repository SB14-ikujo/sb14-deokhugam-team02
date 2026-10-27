package com.sprint.deokhugam.book.adapter.out;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sprint.deokhugam.book.adapter.out.exception.StorageErrorCode;
import com.sprint.deokhugam.book.adapter.out.exception.StorageException;

class LocalStorageTest {

    @TempDir
    Path tempDir;

    private LocalStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalStorage(tempDir.resolve("uploads").toString());
    }

    @Test
    @DisplayName("저장 디렉터리가 없으면 생성 시 만들어진다")
    void createsUploadDir() {
        assertThat(tempDir.resolve("uploads")).isDirectory();
    }

    @Test
    @DisplayName("이미지 키는 UUID와 원본 파일명으로 만들어진다")
    void generateImageKey() {
        String key = storage.generateImageKey("book.png");

        assertThat(key).endsWith("_book.png");
        assertThat(key).isNotEqualTo(storage.generateImageKey("book.png"));
    }

    @Test
    @DisplayName("업로드하면 파일이 저장되고 이미지 키를 반환한다")
    void upload() throws Exception {
        byte[] content = {1, 2, 3};

        String key = storage.upload("a.png", content, "image/png");

        assertThat(key).isEqualTo("a.png");
        assertThat(Files.readAllBytes(tempDir.resolve("uploads/a.png"))).isEqualTo(content);
    }

    @Test
    @DisplayName("이미지 URL은 prefix와 인코딩된 키로 만들어진다")
    void getImageUrl() {
        assertThat(storage.getImageUrl("a.png", "image/png")).isEqualTo("/api/images/a.png");
        assertThat(storage.getImageUrl("uuid_한글 책.png", "image/png"))
            .isEqualTo("/api/images/uuid_%ED%95%9C%EA%B8%80%20%EC%B1%85.png");
    }

    @Test
    @DisplayName("삭제하면 파일이 지워지고, 없는 파일을 삭제해도 예외가 없다")
    void delete() {
        storage.upload("a.png", new byte[] {1}, "image/png");

        storage.delete("a.png");

        assertThat(tempDir.resolve("uploads/a.png")).doesNotExist();
        assertThatCode(() -> storage.delete("a.png")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("저장 디렉터리를 벗어나는 키는 업로드, 삭제 모두 거부한다")
    void rejectsPathTraversal() {
        assertThatThrownBy(() -> storage.upload("../evil.png", new byte[] {1}, "image/png"))
            .isInstanceOfSatisfying(StorageException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(StorageErrorCode.LOCAL_UPLOAD_FAILED));
        assertThat(tempDir.resolve("evil.png")).doesNotExist();

        assertThatThrownBy(() -> storage.delete("../evil.png"))
            .isInstanceOf(StorageException.class);
    }
}
