package com.ai.skills.atlas;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class AtlasValidationResult {
    private final List<AtlasViolation> violations = new ArrayList<>();

    public void add(String code, String message, Path path, String repairHint) {
        violations.add(AtlasViolation.of(code, message, path, repairHint));
    }

    public boolean passed() {
        return violations.isEmpty();
    }

    public List<AtlasViolation> violations() {
        return Collections.unmodifiableList(violations);
    }

    public List<AtlasViolation> byCode(String code) {
        return violations.stream()
                .filter(violation -> violation.code().equals(code))
                .collect(Collectors.toList());
    }
}
