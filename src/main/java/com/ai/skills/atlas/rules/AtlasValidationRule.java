package com.ai.skills.atlas.rules;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

public interface AtlasValidationRule {
    String id();

    String expected();

    void validate(AtlasContext context, AtlasValidationResult result);
}
