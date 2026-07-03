package com.endava.ai.skills.mcp.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HashService {
    private static final String SHA_256 = "SHA-256";
    private static final Pattern SHA_256_HEX = Pattern.compile("(?i)\\b[a-f0-9]{64}\\b");

    public String sha256(Path path) {
        return sha256OrPath(path);
    }

    public static String sha256OrPath(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(Files.readAllBytes(path));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value & 0xff));
            }
            return builder.toString();
        } catch (IOException | NoSuchAlgorithmException exception) {
            return path.toAbsolutePath().normalize().toString();
        }
    }

    public static String firstSha256(String content) {
        Matcher matcher = SHA_256_HEX.matcher(content);
        return matcher.find() ? matcher.group().toLowerCase(Locale.ROOT) : null;
    }
}
