package com.ai.skills.mcp.tests.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class McpTestDigests {
    private static final String SHA_256 = "SHA-256";

    private McpTestDigests() {
    }

    public static String sha256(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] bytes = digest.digest(Files.readAllBytes(path));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                hex.append(String.format("%02x", value & 0xff));
            }
            return hex.toString();
        } catch (IOException exception) {
            throw new AssertionError("Cannot read artifact for hashing: " + path, exception);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(SHA_256 + " is not available", exception);
        }
    }
}
