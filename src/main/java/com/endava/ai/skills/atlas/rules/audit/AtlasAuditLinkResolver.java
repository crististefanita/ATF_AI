package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.AtlasContext;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasAuditLinkResolver {
    private static final String VALIDATION_LINK_ROOT = "validation/";
    private static final String GENERIC_EXTENT_LINK = VALIDATION_LINK_ROOT + AtlasContext.ATLAS_EXTENT_REPORT_FILE;
    private static final Pattern EXTENT_LINK_PATTERN = Pattern.compile(
            "href=\"(validation/[^\"]*atlas-tests-extent-report\\.html)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern README_LINK_PATTERN = Pattern.compile(
            "href=\"(validation/[^\"]+\\.README\\.md)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VALIDATION_EXTENT_LINK_PATTERN = Pattern.compile(
            "href=\"(validation/[^\"]+\\.html)\"",
            Pattern.CASE_INSENSITIVE);

    private AtlasAuditLinkResolver() {
    }

    static String expectedExtentLinkFor(AtlasContext context) {
        String fileName = context.atlasHtml().getFileName().toString();
        String businessLink = scopedLink(fileName, AtlasContext.BUSINESS_ATLAS_SUFFIX,
                AtlasContext.ATLAS_EXTENT_REPORT_SUFFIX);
        if (!businessLink.isBlank()) {
            return businessLink;
        }
        String frameworkLink = scopedLink(fileName, AtlasContext.FRAMEWORK_ATLAS_SUFFIX,
                AtlasContext.FRAMEWORK_ATLAS_EXTENT_REPORT_SUFFIX);
        return frameworkLink.isBlank() ? GENERIC_EXTENT_LINK : frameworkLink;
    }

    static String actualExtentLink(String atlasHtml) {
        Matcher matcher = EXTENT_LINK_PATTERN.matcher(atlasHtml);
        return matcher.find() ? matcher.group(1) : "";
    }

    static String actualReadmeManifestLink(String atlasHtml) {
        Matcher matcher = README_LINK_PATTERN.matcher(atlasHtml);
        return matcher.find() ? matcher.group(1) : "";
    }

    static boolean linksValidationArtifact(String atlasHtml, String artifactName) {
        Matcher matcher = VALIDATION_EXTENT_LINK_PATTERN.matcher(atlasHtml);
        while (matcher.find()) {
            if (matcher.group(1).endsWith("/" + artifactName) || matcher.group(1).endsWith(artifactName)) {
                return true;
            }
        }
        return false;
    }

    static Path extentPath(AtlasContext context, String extentLink) {
        return context.projectRoot().resolve(AtlasContext.ATLAS_DIRECTORY).resolve(extentLink).normalize();
    }

    static Path atlasValidationPath(AtlasContext context, String link) {
        return context.projectRoot().resolve(AtlasContext.ATLAS_DIRECTORY).resolve(link).normalize();
    }

    private static String scopedLink(String fileName, String atlasSuffix, String reportSuffix) {
        if (!fileName.endsWith(atlasSuffix)) {
            return "";
        }
        String prefix = fileName.substring(0, fileName.length() - atlasSuffix.length());
        return prefix.isBlank() ? "" : VALIDATION_LINK_ROOT + prefix + "/" + prefix + reportSuffix;
    }
}
