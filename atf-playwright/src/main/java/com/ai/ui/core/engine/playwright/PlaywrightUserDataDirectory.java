package com.ai.ui.core.engine.playwright;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

final class PlaywrightUserDataDirectory {
    private static final String TEMP_USER_DATA_DIR_PREFIX = "playwright-zoom-profile-";

    private PlaywrightUserDataDirectory() {
    }

    static Path createTemporary() {
        try {
            return Files.createTempDirectory(TEMP_USER_DATA_DIR_PREFIX);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create Playwright user data directory", e);
        }
    }

    static void deleteQuietly(Path root) {
        if (root == null) {
            return;
        }

        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}
