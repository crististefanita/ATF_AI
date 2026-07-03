package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.AtlasContext;

import java.io.IOException;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

final class AtlasTestAuditSupport {
    private AtlasTestAuditSupport() {
    }

    static String expectedExtentLinkFor(AtlasContext context) {
        return AtlasAuditLinkResolver.expectedExtentLinkFor(context);
    }

    static String actualExtentLink(String atlasHtml) {
        return AtlasAuditLinkResolver.actualExtentLink(atlasHtml);
    }

    static Path extentPath(AtlasContext context, String extentLink) {
        return AtlasAuditLinkResolver.extentPath(context, extentLink);
    }

    static Path atlasValidationPath(AtlasContext context, String link) {
        return AtlasAuditLinkResolver.atlasValidationPath(context, link);
    }

    static String actualReadmeManifestLink(String atlasHtml) {
        return AtlasAuditLinkResolver.actualReadmeManifestLink(atlasHtml);
    }

    static boolean linksValidationArtifact(String atlasHtml, String artifactName) {
        return AtlasAuditLinkResolver.linksValidationArtifact(atlasHtml, artifactName);
    }

    static AuditTotals declaredTotals(String atlasHtml) {
        return AtlasAuditTotalsParser.declaredTotals(atlasHtml);
    }

    static AuditTotals extentTotals(Path extent) throws IOException {
        return AtlasAuditTotalsParser.extentTotals(extent);
    }

    static boolean looksLikeFrameworkAtlasReport(Path extent) throws IOException {
        return AtlasAuditExtentScope.looksLikeFrameworkAtlasReport(extent);
    }

    static boolean mentionsCurrentAtlas(Path extent, Path atlasHtml) throws IOException {
        return AtlasAuditExtentScope.mentionsCurrentAtlas(extent, atlasHtml);
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class AuditTotals {
        private final int testCount;
        private final int failureCount;
        private final boolean present;

        static AuditTotals present(int testCount, int failureCount) {
            return new AuditTotals(testCount, failureCount, true);
        }

        static AuditTotals missing() {
            return new AuditTotals(0, 0, false);
        }
    }
}
