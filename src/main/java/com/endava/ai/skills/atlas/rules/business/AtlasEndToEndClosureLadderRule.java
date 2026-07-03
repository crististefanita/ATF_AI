package com.endava.ai.skills.atlas.rules.business;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasEndToEndClosureLadderRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.end-to-end-closure.ladder";
    }

    @Override
    public String expected() {
        return "Atlas explains each important flow as a closure ladder: start, visible action, durable object, reopen/relogin and unowned lifecycle.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        requireAny(context, result, "flow start / actor", html, "actor", "utilizator", "client", "shopper");
        requireAny(context, result, "visible action", html, "actiune vizibila", "visible action", "deschide", "creeaza", "trimite", "finalizeaza");
        requireAny(context, result, "durable object", html, "durabil", "durable", "owned", "obiect business", "business object");
        requireAny(context, result, "reopen or relogin pressure", html, "reopen", "redeschidere", "relogin", "dupa navigare");
        requireAny(context, result, "unowned lifecycle boundary", html, "lifecycle", "ciclu", "end-to-end", "unowned", "nu este owned", "lipseste");
    }

    private void requireAny(AtlasContext context, AtlasValidationResult result, String family, String html, String... tokens) {
        for (String token : tokens) {
            if (html.contains(token)) {
                return;
            }
        }
        result.add(id(), "Closure ladder misses " + family,
                context.atlasHtml(),
                "Each important flow should read as start -> visible action -> durable object -> reopen/relogin -> unowned lifecycle boundary.");
    }
}
