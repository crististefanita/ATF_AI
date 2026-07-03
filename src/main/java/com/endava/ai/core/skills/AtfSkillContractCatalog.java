package com.endava.ai.core.skills;

import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AtfSkillContractCatalog {
    public static final String DEFAULT_RESOURCE = "atf-core/contracts/skills/skill-architecture.yml";

    private final String resource;
    private final List<Map<String, String>> entries;
    private final List<AtfSkill> skills;
    private final Map<String, AtfSkill> skillsById;

    private AtfSkillContractCatalog(String resource, List<Map<String, String>> entries) {
        this.resource = resource;
        this.entries = List.copyOf(entries);
        this.skills = entries.stream()
                .filter(entry -> "skill".equals(required(entry, "kind")))
                .map(this::toSkill)
                .collect(Collectors.toUnmodifiableList());
        this.skillsById = Collections.unmodifiableMap(this.skills.stream()
                .collect(Collectors.toMap(
                        AtfSkill::getId,
                        skill -> skill,
                        (left, right) -> left,
                        LinkedHashMap::new
                )));
    }

    public static AtfSkillContractCatalog fromDefaultResource() {
        return fromResource(DEFAULT_RESOURCE);
    }

    public static AtfSkillContractCatalog fromResource(String resource) {
        return new AtfSkillContractCatalog(resource, SimpleYamlContractLoader.load(resource));
    }

    public String resource() {
        return resource;
    }

    public List<AtfSkill> skills() {
        return skills;
    }

    public AtfSkill requireSkill(String skillId) {
        AtfSkill skill = skillsById.get(skillId);
        if (skill == null) {
            throw new IllegalArgumentException("Missing skill in " + resource + ": " + skillId);
        }
        return skill;
    }

    public List<Map<String, String>> flows() {
        return entriesByKind("flow");
    }

    public List<Map<String, String>> useCases() {
        return entriesByKind("use-case");
    }

    public List<Map<String, String>> entriesByKind(String kind) {
        return entries.stream()
                .filter(entry -> kind.equals(required(entry, "kind")))
                .collect(Collectors.toUnmodifiableList());
    }

    private AtfSkill toSkill(Map<String, String> entry) {
        return AtfSkill.builder()
                .id(required(entry, "id"))
                .kind(required(entry, "kind"))
                .owner(required(entry, "owner"))
                .role(required(entry, "role"))
                .sourceRoot(required(entry, "sourceRoot"))
                .implementationRoot(entry.getOrDefault("implementationRoot", ""))
                .testRoot(required(entry, "testRoot"))
                .readme(required(entry, "readme"))
                .sanitySuite(required(entry, "sanitySuite"))
                .deepSuite(required(entry, "deepSuite"))
                .promptRoot(required(entry, "promptRoot"))
                .collaboratesThrough(required(entry, "collaboratesThrough"))
                .artifactPolicy(required(entry, "artifactPolicy"))
                .consumes(values(entry, "consumes"))
                .readingPath(values(entry, "readingPath"))
                .forbiddenMainImports(values(entry, "forbiddenMainImports"))
                .forbiddenTestImports(values(entry, "forbiddenTestImports"))
                .mustMention(values(entry, "mustMention"))
                .build();
    }

    private String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

    public static List<String> values(Map<String, String> entry, String key) {
        String raw = entry.getOrDefault(key, "").trim();
        if (raw.isBlank()) {
            return List.of();
        }
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableList());
    }
}
