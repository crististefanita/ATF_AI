package com.endava.ai.core.skills;

import lombok.Value;

@Value
public class AtfSkillArtifactPolicy {
    String description;

    public boolean mentions(String token) {
        return description != null && description.contains(token);
    }

    public boolean isDeclared() {
        return description != null && !description.isBlank();
    }
}
