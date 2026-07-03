package com.endava.ai.skills.atlas.rules;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;

public interface AtlasValidationRule {
    String id();

    String expected();

    void validate(AtlasContext context, AtlasValidationResult result);
}
