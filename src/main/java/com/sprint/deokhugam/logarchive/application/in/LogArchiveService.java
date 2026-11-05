package com.sprint.deokhugam.logarchive.application.in;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.sprint.deokhugam.logarchive.application.out.LogStoragePort;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "deokhugam.storage.type", havingValue = "s3")
public class LogArchiveService implements LogArchiveUseCase {
    private final LogStoragePort logStoragePort;

    @Override
    public int archive(LocalDate today) {
        Path logDir = Path.of("logs");
        int uploaded = 0;

        for (int i = 1; i <= 7; i++) {
            LocalDate date = today.minusDays(i);

            try (DirectoryStream<Path> files = Files.newDirectoryStream(logDir, "app-" + date + ".*.log")) {

                for (Path file : files) {
                    String key = date + "/" + file.getFileName(); // logs/2026-11-03/app-2026-11-03.0.log

                    logStoragePort.upload(key, file);
                    uploaded++;
                }
            } catch (IOException e) {
                log.error("로그 업로드 실패: {}", date, e);
            }
        }
        return uploaded;
    }
}
