package com.ai.skills.mcp.rules.source.inventory;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evidence from the final MCP close-out artifacts.
 */
@Getter
@Accessors(fluent = true)
public final class McpCloseoutEvidenceInventory {
    private static final Pattern SKIPPED_COUNT = Pattern.compile("(?im)^\\s*Skipped\\s*[:=]\\s*(\\d+)\\b");

    private final int closeoutProfilePurity;
    private final int mcpExtentFreshness;
    private final int skippedCloseoutCount;

    private McpCloseoutEvidenceInventory(int closeoutProfilePurity,
                                         int mcpExtentFreshness,
                                         int skippedCloseoutCount) {
        this.closeoutProfilePurity = closeoutProfilePurity;
        this.mcpExtentFreshness = mcpExtentFreshness;
        this.skippedCloseoutCount = skippedCloseoutCount;
    }

    public static McpCloseoutEvidenceInventory load(McpPackageContext context, McpPackageValidationResult result) {
        return new McpCloseoutEvidenceInventory(
                closeoutProfilePurity(context, result),
                mcpExtentFreshness(context),
                skippedCloseoutCount(context, result)
        );
    }

    private static int closeoutProfilePurity(McpPackageContext context, McpPackageValidationResult result) {
        String evidence = (readPackageFile(context, result, McpArtifactPaths.MCP_EXTENT_ORIGIN)
                + "\n" + readPackageFile(context, result, McpArtifactPaths.MCP_GATE_RESULT))
                .toLowerCase(Locale.ROOT);
        return evidence.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName())
                || evidence.contains(McpExecutionProfile.PACKAGE_AUDIT.suiteFileName())
                || evidence.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.id())
                || evidence.contains(McpExecutionProfile.PACKAGE_AUDIT.id()) ? 100 : 0;
    }

    private static int mcpExtentFreshness(McpPackageContext context) {
        return Files.isRegularFile(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT))
                && Files.isRegularFile(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_SHA256))
                && Files.isRegularFile(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN))
                ? 100 : 0;
    }

    private static int skippedCloseoutCount(McpPackageContext context, McpPackageValidationResult result) {
        Integer xmlSkipped = skippedFromXml(context, result, McpArtifactPaths.MCP_TESTNG_XML);
        if (xmlSkipped == null) {
            xmlSkipped = skippedFromXml(context, result, McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML);
        }
        if (xmlSkipped != null) {
            return xmlSkipped;
        }
        Matcher matcher = SKIPPED_COUNT.matcher(readPackageFile(context, result, McpArtifactPaths.MCP_METHOD_LEDGER));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static Integer skippedFromXml(McpPackageContext context,
                                          McpPackageValidationResult result,
                                          String relativePath) {
        String xml = readPackageFile(context, result, relativePath);
        if (xml.isBlank()) {
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList methods = document.getElementsByTagName("test-method");
            int skipped = 0;
            for (int i = 0; i < methods.getLength(); i++) {
                String status = methods.item(i).getAttributes().getNamedItem("status") == null
                        ? ""
                        : methods.item(i).getAttributes().getNamedItem("status").getTextContent();
                if ("SKIP".equalsIgnoreCase(status) || "SKIPPED".equalsIgnoreCase(status)) {
                    skipped++;
                }
            }
            if (methods.getLength() > 0) {
                return skipped;
            }
            String skippedAttribute = document.getDocumentElement().getAttribute("skipped");
            return skippedAttribute.isBlank() ? null : Integer.parseInt(skippedAttribute);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String readPackageFile(McpPackageContext context,
                                          McpPackageValidationResult result,
                                          String relativePath) {
        Path path = context.packageRoot().resolve(relativePath);
        if (!Files.isRegularFile(path)) {
            return "";
        }
        return context.files().read(path, result);
    }
}
