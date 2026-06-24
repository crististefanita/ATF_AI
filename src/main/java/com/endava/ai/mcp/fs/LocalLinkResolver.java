package com.endava.ai.mcp.fs;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;

public final class LocalLinkResolver {
    public Path resolve(Path reportDirectory, String href) {
        try {
            URI uri = URI.create(href.replace("\\", "/"));
            if (uri.getScheme() != null && uri.getScheme().length() == 1) {
                return Path.of(href).toAbsolutePath().normalize();
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to filesystem resolution.
        }

        Path path = Path.of(href);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return reportDirectory.resolve(path).normalize();
    }

    public boolean isExternalOrInline(String href) {
        String lower = href.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("mailto:")
                || lower.startsWith("data:")
                || lower.startsWith("javascript:")
                || lower.startsWith("#");
    }
}
