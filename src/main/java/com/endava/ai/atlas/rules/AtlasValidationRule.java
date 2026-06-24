package com.endava.ai.atlas.rules;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

public interface AtlasValidationRule {
    String id();

    String expected();

    void validate(AtlasContext context, AtlasValidationResult result);
}
