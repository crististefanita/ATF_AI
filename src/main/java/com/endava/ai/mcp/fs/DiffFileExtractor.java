package com.endava.ai.mcp.fs;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiffFileExtractor {
    public Set<String> referencedFiles(String diff) {
        Set<String> referencedFiles = new HashSet<>();
        Matcher marker = Pattern.compile("(?im)^---\\s+(?:NEW|UPDATE)\\s+FILE:\\s+([^\\r\\n]+)$").matcher(diff);
        while (marker.find()) {
            referencedFiles.add(marker.group(1).trim());
        }
        Matcher git = Pattern.compile("(?im)^\\+\\+\\+\\s+b/([^\\r\\n]+)$").matcher(diff);
        while (git.find()) {
            referencedFiles.add(git.group(1).trim());
        }
        return referencedFiles;
    }
}
