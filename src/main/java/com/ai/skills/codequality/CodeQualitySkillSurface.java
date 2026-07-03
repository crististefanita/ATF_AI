package com.ai.skills.codequality;

/**
 * Marker facade for ATF code-quality guardrails.
 */
public final class CodeQualitySkillSurface {
    public static final String SKILL_ID = "skill.code-quality";
    public static final String PRIMARY_SUITE =
            "src/test/resources/skills/codequality/testng-code-quality-sanity.xml";
    public static final int DEFAULT_MAX_DIRECT_CLASSES_PER_PACKAGE = 8;
    public static final int DEFAULT_WARNING_LINES_PER_CLASS = 220;
    public static final int DEFAULT_MAX_LINES_PER_CLASS = 320;
    public static final int DEFAULT_WARNING_BYTES_PER_YAML = 10000;
    public static final int DEFAULT_MAX_BYTES_PER_YAML = 20000;
    public static final int DEFAULT_MAX_TESTNG_SUITES = 39;
    public static final int DECLARED_DEPENDENCY_BOUNDARIES = 10;
    public static final int DECLARED_JAVA_COMPATIBILITY_RULES = 1;

    private CodeQualitySkillSurface() {
    }
}
