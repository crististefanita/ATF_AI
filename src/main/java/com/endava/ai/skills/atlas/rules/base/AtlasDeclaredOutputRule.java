package com.endava.ai.skills.atlas.rules.base;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.report.AtlasReportModel;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.endava.ai.skills.atlas.contract.AtlasRuleIds.Base.DECLARED_OUTPUT;

public final class AtlasDeclaredOutputRule implements AtlasValidationRule {
    @Override
    public String id() {
        return DECLARED_OUTPUT;
    }

    @Override
    public String expected() {
        return "The atlas output exists as " + AtlasContext.BUSINESS_ATLAS_FILE
                + " or as a scoped <ATF_NAME>" + AtlasContext.BUSINESS_ATLAS_SUFFIX + " artifact.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Path atlasDir = context.projectRoot().resolve(AtlasContext.ATLAS_DIRECTORY).normalize();
        List<Path> declaredOutputs = declaredAtlasOutputs(atlasDir);

        if (declaredOutputs.isEmpty()) {
            result.add(id(),
                    "No declared business atlas output found",
                    atlasDir,
                    "Produce " + AtlasContext.BUSINESS_ATLAS_FILE
                            + " for a generic run or <ATF_NAME>" + AtlasContext.BUSINESS_ATLAS_SUFFIX
                            + " for a scoped analysis.");
            return;
        }

        for (Path output : declaredOutputs) {
            requireUsefulSize(result, output);
        }
    }

    private List<Path> declaredAtlasOutputs(Path atlasDir) {
        if (!Files.isDirectory(atlasDir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(atlasDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> isDeclaredAtlasOutput(path.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    private boolean isDeclaredAtlasOutput(String fileName) {
        return AtlasReportModel.isDeclaredAtlasArtifactName(fileName);
    }

    private void requireUsefulSize(AtlasValidationResult result, Path output) {
        try {
            if (Files.size(output) < 8_000) {
                result.add(id(),
                        "Declared atlas output is too small to be useful: " + output.getFileName(),
                        output,
                        "Regenerate the scoped atlas with diagrams, score cards, trace rows and comprehension limits.");
            }
        } catch (IOException e) {
            result.add(id(),
                    "Cannot inspect declared atlas output: " + output.getFileName(),
                    output,
                    "Make the declared atlas output readable.");
        }
    }
}
