package com.ai.skills.atlas.rules.audit;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasTestAuditManifestCommandSyncRule implements AtlasValidationRule {
    private static final Pattern MANIFEST_FIELD = Pattern.compile(
            "(?im)^\\|\\s*([^|]+?)\\s*\\|\\s*`?([^|`]+?)`?\\s*\\|\\s*$");
    private static final String FIELD_TOTAL = "total invocations";
    private static final String FIELD_PASSED = "passed";
    private static final String FIELD_FAILED = "failed";
    private static final String FIELD_SKIPPED = "skipped";
    private static final String FIELD_EXTENT_ARTIFACT = "extent validation artifact";

    @Override
    public String id() {
        return AtlasRuleIds.Candidate.TEST_AUDIT_MANIFEST_COMMAND_SYNC;
    }

    @Override
    public String expected() {
        return "If the atlas publishes aggregate/runtime evidence, its HTML, linked Extent report, README manifest, and Maven command stay synchronized.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String atlasHtml = context.atlasText();
        String lowerAtlas = context.lowerAtlasText();
        String manifestLink = AtlasTestAuditSupport.actualReadmeManifestLink(atlasHtml);
        if (manifestLink.isBlank() && !declaresAggregateManifest(lowerAtlas)) {
            return;
        }
        if (manifestLink.isBlank()) {
            result.add(id(), "Atlas mentions aggregate/runtime manifest evidence but does not link a README manifest",
                    context.atlasHtml(),
                    "Link the scoped validation/*" + AtlasContext.README_SUFFIX
                            + " manifest next to the published aggregate Extent artifact.");
            return;
        }

        Path manifest = AtlasTestAuditSupport.atlasValidationPath(context, manifestLink);
        if (!Files.exists(manifest)) {
            result.add(id(), "Linked aggregate README manifest is missing: " + manifestLink,
                    manifest,
                    "Copy the generated README manifest into " + AtlasContext.VALIDATION_DIRECTORY
                            + " and keep the atlas link scoped.");
            return;
        }

        try {
            validateManifest(context, result, manifest, atlasHtml, lowerAtlas);
        } catch (IOException e) {
            result.add(id(), "Cannot read linked aggregate README manifest: " + manifestLink,
                    manifest,
                    "Ensure the linked manifest is readable before publishing aggregate audit evidence.");
        }
    }

    private void validateManifest(AtlasContext context,
                                  AtlasValidationResult result,
                                  Path manifest,
                                  String atlasHtml,
                                  String lowerAtlas) throws IOException {
        String manifestText = Files.readString(manifest);
        String lowerManifest = manifestText.toLowerCase(Locale.ROOT);
        String extentArtifact = field(manifestText, FIELD_EXTENT_ARTIFACT);
        if (extentArtifact.isBlank()) {
            result.add(id(), "README manifest does not declare the Extent validation artifact",
                    manifest,
                    "Add an 'Extent validation artifact' field that names the published aggregate Extent HTML.");
        } else {
            Path extent = manifest.getParent().resolve(extentArtifact).normalize();
            if (!Files.exists(extent)) {
                result.add(id(), "README manifest points to a missing Extent validation artifact: " + extentArtifact,
                        extent,
                        "Copy the aggregate Extent HTML beside the manifest or update the manifest artifact field.");
            }
            if (!AtlasTestAuditSupport.linksValidationArtifact(atlasHtml, extentArtifact)) {
                result.add(id(), "Atlas HTML does not link the Extent artifact declared by the README manifest: " + extentArtifact,
                        context.atlasHtml(),
                        "Link the same aggregate Extent artifact that the README manifest declares.");
            }
        }

        validateNumber(result, manifest, lowerAtlas, manifestText, FIELD_TOTAL);
        validateNumber(result, manifest, lowerAtlas, manifestText, FIELD_PASSED);
        validateNumber(result, manifest, lowerAtlas, manifestText, FIELD_FAILED);
        validateNumber(result, manifest, lowerAtlas, manifestText, FIELD_SKIPPED);
        if (!lowerManifest.contains("mvn ") || !lowerManifest.contains("reporting.engine=extent")) {
            result.add(id(), "README manifest does not contain an audit-ready Maven Extent command",
                    manifest,
                    "Publish the exact mvn command used to regenerate the aggregate Extent artifact.");
        }
        if (!lowerAtlas.contains("mvn ") || !lowerAtlas.contains("reporting.engine=extent")) {
            result.add(id(), "Atlas Test Audit does not publish the Maven Extent command",
                    context.atlasHtml(),
                    "Add the exact mvn command that regenerates the aggregate Extent artifact.");
        }
    }

    private static boolean declaresAggregateManifest(String lowerAtlas) {
        return lowerAtlas.contains("aggregate")
                && lowerAtlas.contains("manifest")
                && lowerAtlas.contains("extent");
    }

    private void validateNumber(AtlasValidationResult result,
                                Path manifest,
                                String lowerAtlas,
                                String manifestText,
                                String fieldName) {
        String value = field(manifestText, fieldName);
        if (value.isBlank()) {
            result.add(id(), "README manifest does not declare '" + fieldName + "'",
                    manifest,
                    "Keep total/passed/failed/skipped fields explicit in the aggregate manifest.");
            return;
        }
        if (!lowerAtlas.contains(value.toLowerCase(Locale.ROOT))) {
            result.add(id(), "Atlas HTML does not echo manifest field '" + fieldName + "' value " + value,
                    manifest,
                    "Synchronize the atlas audit table with the README manifest before publishing.");
        }
    }

    private static String field(String manifestText, String expectedName) {
        Matcher matcher = MANIFEST_FIELD.matcher(manifestText);
        while (matcher.find()) {
            String name = matcher.group(1).trim().toLowerCase(Locale.ROOT);
            if (name.equals(expectedName)) {
                return matcher.group(2).trim();
            }
        }
        return "";
    }
}
