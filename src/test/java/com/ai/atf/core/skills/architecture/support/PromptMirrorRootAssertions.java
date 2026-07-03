package com.ai.atf.core.skills.architecture.support;

import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class PromptMirrorRootAssertions {
    private PromptMirrorRootAssertions() {
    }

    public static void assertDeclaredRootsAreIndexed(Map<String, String> entry,
                                                     Path projectRoot,
                                                     String readmePath,
                                                     String owner) {
        if (!entry.containsKey("sourceRoot") && !entry.containsKey("mirrorRoot")) {
            return;
        }
        String id = entry.getOrDefault("id", "<missing-id>");
        String sourceRoot = required(entry, "sourceRoot", owner, id);
        String mirrorRoot = required(entry, "mirrorRoot", owner, id);
        Assert.assertTrue(Files.isDirectory(projectRoot.resolve(sourceRoot)),
                owner + " prompt source root must exist for " + id + ": " + sourceRoot);
        Assert.assertTrue(Files.isDirectory(projectRoot.resolve(mirrorRoot)),
                owner + " prompt mirror root must exist for " + id + ": " + mirrorRoot);
        String readme = read(projectRoot.resolve(readmePath), owner);
        Assert.assertTrue(readme.contains(sourceRoot) && readme.contains(mirrorRoot),
                owner + " prompt ownership README must name source and mirror roots for " + id);
    }

    private static String required(Map<String, String> entry, String key, String owner, String id) {
        String value = entry.get(key);
        Assert.assertTrue(value != null && !value.isBlank(),
                owner + " prompt ownership policy must declare `" + key + "` for " + id);
        return value;
    }

    private static String read(Path path, String owner) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + owner + " prompt ownership README: " + path, exception);
        }
    }
}
