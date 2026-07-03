package com.ai.atf.core.skills.architecture;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

final class AtfSkillGitSnapshot {
    private static final String GIT_UNAVAILABLE = "git-unavailable";

    private AtfSkillGitSnapshot() {
    }

    static String commit() {
        return git("rev-parse", "--short", "HEAD");
    }

    static String dirtyState() {
        String status = git("status", "--short");
        if (GIT_UNAVAILABLE.equals(status)) {
            return status;
        }
        return status.isBlank() ? "clean" : "dirty";
    }

    private static String git(String... arguments) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command(arguments));
            builder.directory(AtfSourcePaths.workspaceRoot().toFile());
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            int exit = process.waitFor();
            return exit == 0 ? output : GIT_UNAVAILABLE;
        } catch (IOException exception) {
            return GIT_UNAVAILABLE;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return GIT_UNAVAILABLE;
        }
    }

    private static List<String> command(String... arguments) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of("git"), java.util.stream.Stream.of(arguments))
                .collect(Collectors.toList());
    }
}
