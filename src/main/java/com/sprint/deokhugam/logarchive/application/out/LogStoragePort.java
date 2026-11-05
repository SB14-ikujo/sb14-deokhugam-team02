package com.sprint.deokhugam.logarchive.application.out;

import java.nio.file.Path;

public interface LogStoragePort {
    void upload(String key, Path file);
}
