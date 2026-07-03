package com.endava.ai.core.skills;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
public class AtfSkillBoundary {
    List<String> consumes;
    List<String> forbiddenMainImports;
    List<String> forbiddenTestImports;

    @Builder
    public AtfSkillBoundary(
            List<String> consumes,
            List<String> forbiddenMainImports,
            List<String> forbiddenTestImports
    ) {
        this.consumes = safeList(consumes);
        this.forbiddenMainImports = safeList(forbiddenMainImports);
        this.forbiddenTestImports = safeList(forbiddenTestImports);
    }

    public boolean consumes(String skillId) {
        return consumes.contains(skillId);
    }

    public boolean hasForbiddenMainImports() {
        return forbiddenMainImports.stream().anyMatch(AtfSkillBoundary::isActiveToken);
    }

    public boolean hasForbiddenTestImports() {
        return forbiddenTestImports.stream().anyMatch(AtfSkillBoundary::isActiveToken);
    }

    private static boolean isActiveToken(String value) {
        return value != null && !value.isBlank() && !"none".equalsIgnoreCase(value.trim());
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
