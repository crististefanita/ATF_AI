package com.ai.core.skills;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
public class AtfSkillSuite {
    String sanitySuite;
    String deepSuite;
    String promptRoot;
    List<String> readingPath;

    @Builder
    public AtfSkillSuite(String sanitySuite, String deepSuite, String promptRoot, List<String> readingPath) {
        this.sanitySuite = sanitySuite;
        this.deepSuite = deepSuite;
        this.promptRoot = promptRoot;
        this.readingPath = readingPath == null ? List.of() : List.copyOf(readingPath);
    }

    public boolean usesSuite(String suiteXml) {
        return sanitySuite.equals(suiteXml) || deepSuite.equals(suiteXml);
    }

    public boolean hasPromptRoot() {
        return promptRoot != null && !promptRoot.isBlank() && !"none".equalsIgnoreCase(promptRoot.trim());
    }
}
