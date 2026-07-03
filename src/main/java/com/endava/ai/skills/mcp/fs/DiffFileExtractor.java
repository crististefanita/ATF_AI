package com.endava.ai.skills.mcp.fs;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiffFileExtractor {
    public Set<String> referencedFiles(String diff) {
        Set<String> referencedFiles = new HashSet<>();
        Matcher marker = Pattern.compile("(?im)^---\\s+(?:NEW|UPDATE|DELETE)\\s+FILE:\\s+([^\\r\\n]+)$").matcher(diff);
        while (marker.find()) {
            addPath(referencedFiles, marker.group(1));
        }
        Matcher git = Pattern.compile("(?im)^(?:---|\\+\\+\\+)\\s+\"?(?:a|b)/([^\"\\r\\n]+)\"?$").matcher(diff);
        while (git.find()) {
            addPath(referencedFiles, git.group(1));
        }
        Matcher gitHeader = Pattern.compile("(?im)^diff --git\\s+\"?a/(.+?)\"?\\s+\"?b/(.+?)\"?$").matcher(diff);
        while (gitHeader.find()) {
            addPath(referencedFiles, gitHeader.group(1));
            addPath(referencedFiles, gitHeader.group(2));
        }
        Matcher rename = Pattern.compile("(?im)^rename\\s+(?:from|to)\\s+([^\\r\\n]+)$").matcher(diff);
        while (rename.find()) {
            addPath(referencedFiles, rename.group(1));
        }
        return referencedFiles;
    }

    private void addPath(Set<String> referencedFiles, String rawPath) {
        String normalized = rawPath.trim()
                .replace("\"", "")
                .replace('\\', '/')
                .replaceAll("/+", "/");
        if (normalized.isBlank() || "/dev/null".equals(normalized)) {
            return;
        }
        referencedFiles.add(normalized);
    }
}
