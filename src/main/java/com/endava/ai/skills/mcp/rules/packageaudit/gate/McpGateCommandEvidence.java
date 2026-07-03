package com.endava.ai.skills.mcp.rules.packageaudit.gate;

import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class McpGateCommandEvidence {
    private McpGateCommandEvidence() {
    }

    public static String originCommand(McpPackageContext context) {
        Path origin = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN);
        if (!Files.isRegularFile(origin)) {
            return "";
        }
        try {
            String command = jsonString(Files.readString(origin), "command");
            return command == null ? "" : command;
        } catch (IOException exception) {
            return "";
        }
    }

    public static String gateCommand(String gateText) {
        java.util.regex.Matcher matcher = Pattern.compile("(?im)^\\s*-?\\s*(?:full\\s+mcp\\s+suite\\s+command|command|mcp\\s+contract\\s+suite)\\s*[:=]\\s*(.+)$")
                .matcher(gateText == null ? "" : gateText);
        while (matcher.find()) {
            String value = matcher.group(1).trim();
            if (value.toLowerCase(Locale.ROOT).contains("mvn")) {
                return value;
            }
        }
        return "";
    }

    public static boolean declaresPackageRoot(String command) {
        return command != null && command.toLowerCase(Locale.ROOT).contains("-dmcp.package.root");
    }

    public static boolean saysPackageRootWasMissing(String evidenceText) {
        String lower = evidenceText.toLowerCase(Locale.ROOT);
        return lower.contains("missing -dmcp.package.root")
                || lower.contains("package-audit skipped: missing")
                || lower.contains("missing package root");
    }

    public static String skippedOnlyGateEvidence(String gateText) {
        StringBuilder evidence = new StringBuilder();
        for (String line : (gateText == null ? "" : gateText).split("\\R")) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("skipped") || lower.contains("missing") || lower.contains("reason")) {
                evidence.append(line).append('\n');
            }
        }
        return evidence.toString();
    }

    public static Integer publishedCap(McpPackageContext context) {
        Path packageState = context.packageRoot().resolve(McpArtifactPaths.PACKAGE_STATE);
        if (!Files.isRegularFile(packageState)) {
            return null;
        }
        try {
            String state = Files.readString(packageState);
            Integer cap = firstInt(state, "cap");
            Integer overall = firstInt(state, "overall");
            return cap == null ? overall : cap;
        } catch (IOException exception) {
            return null;
        }
    }

    public static List<String> skippedTestNames(String html) {
        ArrayList<String> names = new ArrayList<>();
        java.util.regex.Matcher matcher = Pattern.compile("(?is)<li\\s+class=[\"'][^\"']*\\btest-item\\b[^\"']*[\"'][^>]*\\bstatus=[\"'](?:skip|skipped)[\"'][\\s\\S]*?<p\\s+class=[\"']name[\"']>(.*?)</p>")
                .matcher(html == null ? "" : html);
        while (matcher.find()) {
            String name = matcher.group(1).replaceAll("(?is)<[^>]+>", "").trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    public static boolean isPackageAuditProfileName(String name) {
        return name.startsWith("McpLatestPackage")
                || name.startsWith("McpRuntimeAndPackage")
                || name.startsWith("McpBusinessEvidence")
                || name.startsWith("McpCandidateContract");
    }

    private static String jsonString(String json, String field) {
        java.util.regex.Matcher matcher = Pattern.compile("(?is)\"" + Pattern.quote(field)
                + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static Integer firstInt(String text, String label) {
        java.util.regex.Matcher matcher = Pattern.compile("(?is)(?:\"|\\b)" + Pattern.quote(label)
                + "(?:\"|\\b)\\s*[:=]\\s*\"?(\\d+)\"?").matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
