package com.sprint.deokhugam.logarchive.application.in;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;

import com.sprint.deokhugam.logarchive.application.out.LogStoragePort;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class LogArchiveService implements LogArchiveUseCase {
    private static final Path LOG_DIR = Path.of("logs");
    private static final int ARCHIVE_DAYS = 7;

    private final LogStoragePort logStoragePort;

    @Override
    public int archive(LocalDate today) {
        int uploaded = 0;

        // 디렉터리는 1번만 조회하고, 가져온 파일 목록을 돌면서 업로드한다.
        for (Path file : findLogFiles(today)) {
            if (upload(file)) {
                uploaded++;
            }
        }
        return uploaded;
    }

    // 어제부터 7일치 롤링 로그(app-yyyy-MM-dd.N.log) 목록. 조회에 실패하면 로그만 남기고 빈 목록을 반환한다.
    private List<Path> findLogFiles(LocalDate today) {
        List<String> prefixes = IntStream.rangeClosed(1, ARCHIVE_DAYS)
            .mapToObj(i -> "app-" + today.minusDays(i) + ".")   // 예: app-2026-11-03.
            .toList();

        try (Stream<Path> files = Files.list(LOG_DIR)) {
            return files
                .filter(file -> prefixes.stream().anyMatch(file.getFileName().toString()::startsWith))
                .toList();
        } catch (IOException | UncheckedIOException e) {
            log.error("[로그 아카이브] 로그 디렉터리 조회 실패: dir={}", LOG_DIR, e);
            return List.of();
        }
    }

    // 파일 하나 업로드. 실패해도 예외를 던지지 않고 로그만 남겨서, 나머지 파일은 계속 업로드한다.
    private boolean upload(Path file) {
        String key = file.getFileName().toString(); // app-2026-11-03.0.log

        try {
            logStoragePort.upload(key, file);
            return true;
        } catch (SdkException | UncheckedIOException e) {
            log.error("[로그 아카이브] 업로드 실패: key={}", key, e);
            return false;
        }
    }
}
