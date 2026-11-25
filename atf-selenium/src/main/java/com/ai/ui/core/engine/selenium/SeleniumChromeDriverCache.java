package com.ai.ui.core.engine.selenium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SeleniumChromeDriverCache {
    static final String ATF_SELENIUM_CACHE_ROOT_PROPERTY = "atf.selenium.cache.root";
    private static final String SELENIUM_CACHE_DIRECTORY = ".cache";
    private static final String SELENIUM_DIRECTORY = "selenium";
    private static final String CHROME_DRIVER_DIRECTORY = "chromedriver";
    private static final String WINDOWS_CHROME_DRIVER_EXECUTABLE = "chromedriver.exe";
    private static final String UNIX_CHROME_DRIVER_EXECUTABLE = "chromedriver";
    private static final String VERSION_SEPARATOR = "\\.";
    private static final int VERSION_PARTS_TO_COMPARE = 4;

    Optional<Path> latestChromeDriverExecutable() {
        Path chromeDriverRoot = cacheRoot().resolve(CHROME_DRIVER_DIRECTORY);
        if (!Files.isDirectory(chromeDriverRoot)) {
            return Optional.empty();
        }

        try (Stream<Path> candidates = Files.walk(chromeDriverRoot)) {
            return candidates
                    .filter(Files::isRegularFile)
                    .filter(this::isChromeDriverExecutable)
                    .max(this::compareDriverCandidates);
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private static Path cacheRoot() {
        String explicitCacheRoot = System.getProperty(ATF_SELENIUM_CACHE_ROOT_PROPERTY);
        if (explicitCacheRoot != null && !explicitCacheRoot.isBlank()) {
            return Path.of(explicitCacheRoot);
        }
        return Path.of(System.getProperty("user.home"), SELENIUM_CACHE_DIRECTORY, SELENIUM_DIRECTORY);
    }

    private boolean isChromeDriverExecutable(Path path) {
        String fileName = path.getFileName().toString();
        return WINDOWS_CHROME_DRIVER_EXECUTABLE.equalsIgnoreCase(fileName)
                || UNIX_CHROME_DRIVER_EXECUTABLE.equals(fileName);
    }

    private int compareDriverCandidates(Path left, Path right) {
        int versionComparison = compareVersionDirectories(left.getParent().getFileName().toString(),
                right.getParent().getFileName().toString());
        if (versionComparison != 0) {
            return versionComparison;
        }
        return Comparator.comparing(this::lastModifiedMillis).compare(left, right);
    }

    private int compareVersionDirectories(String left, String right) {
        List<Integer> leftParts = versionParts(left);
        List<Integer> rightParts = versionParts(right);
        for (int index = 0; index < VERSION_PARTS_TO_COMPARE; index++) {
            int comparison = Integer.compare(leftParts.get(index), rightParts.get(index));
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private List<Integer> versionParts(String versionDirectory) {
        String[] tokens = versionDirectory.split(VERSION_SEPARATOR);
        return Stream.iterate(0, index -> index + 1)
                .limit(VERSION_PARTS_TO_COMPARE)
                .map(index -> index < tokens.length ? parseVersionToken(tokens[index]) : 0)
                .collect(Collectors.toList());
    }

    private int parseVersionToken(String token) {
        try {
            return Integer.parseInt(token.replaceAll("\\D+", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private long lastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }
}
