package com.endava.ai.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AtlasValidationResult {
    private final List<AtlasViolation> violations = new ArrayList<>();

    public void add(String code, String message, java.nio.file.Path path, String repairHint) {
        violations.add(new AtlasViolation(code, message, path, repairHint));
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
                .collect(java.util.stream.Collectors.toList());
    }
}
