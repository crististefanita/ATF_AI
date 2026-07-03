package com.endava.ai.skills.atlas.rules.scoring.cards;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.skills.atlas.rules.scoring.AtlasScoreContract;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class AtlasScoreCardsDocumentedRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.score-cards.documented";
    }

    @Override
    public String expected() {
        return "Atlas score cards are governed by business-test-atlas-reference.html, not invented ad hoc in the report.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String atlas = AtlasScoreContract.normalized(context.lowerAtlasText());
        String requirements;
        try {
            requirements = AtlasScoreContract.normalized(Files.readString(context.requirementsHtml(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            result.add(id(), "Cannot read atlas requirements HTML: " + exception.getMessage(),
                    context.requirementsHtml(), "Keep business-test-atlas-reference.html present and readable.");
            return;
        }

        if (!requirements.contains("3.3.1 score cards canonice")) {
            result.add(id(), "Requirements do not expose the canonical score-card subsection",
                    context.requirementsHtml(),
                    "Document the score cards under 3.3.1 Score cards canonice so the atlas skill is governed.");
        }
        for (String card : AtlasScoreContract.ALL_CARDS) {
            if (!requirements.contains(card)) {
                result.add(id(), "Requirements do not document score card: " + card,
                        context.requirementsHtml(),
                        "Add the card to business-test-atlas-reference.html before allowing it in generated atlas output.");
            }
            if (!atlas.contains(card)) {
                result.add(id(), "Atlas output does not use documented score card: " + card,
                        context.atlasHtml(), AtlasScoreContract.REPAIR_CANONICAL_CARDS);
            }
        }
    }
}
