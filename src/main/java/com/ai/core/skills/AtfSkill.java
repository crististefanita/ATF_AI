package com.ai.core.skills;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
public class AtfSkill {
    String id;
    String kind;
    String owner;
    String role;
    String sourceRoot;
    String implementationRoot;
    String testRoot;
    String readme;
    String sanitySuite;
    String deepSuite;
    String promptRoot;
    String collaboratesThrough;
    String artifactPolicy;
    List<String> consumes;
    List<String> readingPath;
    List<String> forbiddenMainImports;
    List<String> forbiddenTestImports;
    List<String> mustMention;

    @Builder
    public AtfSkill(
            String id,
            String kind,
            String owner,
            String role,
            String sourceRoot,
            String implementationRoot,
            String testRoot,
            String readme,
            String sanitySuite,
            String deepSuite,
            String promptRoot,
            String collaboratesThrough,
            String artifactPolicy,
            List<String> consumes,
            List<String> readingPath,
            List<String> forbiddenMainImports,
            List<String> forbiddenTestImports,
            List<String> mustMention
    ) {
        this.id = id;
        this.kind = kind;
        this.owner = owner;
        this.role = role;
        this.sourceRoot = sourceRoot;
        this.implementationRoot = implementationRoot;
        this.testRoot = testRoot;
        this.readme = readme;
        this.sanitySuite = sanitySuite;
        this.deepSuite = deepSuite;
        this.promptRoot = promptRoot;
        this.collaboratesThrough = collaboratesThrough;
        this.artifactPolicy = artifactPolicy;
        this.consumes = safeList(consumes);
        this.readingPath = safeList(readingPath);
        this.forbiddenMainImports = safeList(forbiddenMainImports);
        this.forbiddenTestImports = safeList(forbiddenTestImports);
        this.mustMention = safeList(mustMention);
    }

    public boolean consumes(String skillId) {
        return boundary().consumes(skillId);
    }

    public boolean hasForbiddenMainImports() {
        return boundary().hasForbiddenMainImports();
    }

    public boolean hasForbiddenTestImports() {
        return boundary().hasForbiddenTestImports();
    }

    public boolean hasImplementationRoot() {
        return implementationRoot != null && !implementationRoot.isBlank();
    }

    public AtfSkillSuite suite() {
        return AtfSkillSuite.builder()
                .sanitySuite(sanitySuite)
                .deepSuite(deepSuite)
                .promptRoot(promptRoot)
                .readingPath(readingPath)
                .build();
    }

    public AtfSkillBoundary boundary() {
        return AtfSkillBoundary.builder()
                .consumes(consumes)
                .forbiddenMainImports(forbiddenMainImports)
                .forbiddenTestImports(forbiddenTestImports)
                .build();
    }

    public AtfSkillArtifactPolicy artifactPolicyModel() {
        return new AtfSkillArtifactPolicy(artifactPolicy);
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

}
