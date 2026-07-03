package com.ai.skills.atlas.rules;

import java.util.Arrays;
import java.util.List;

public final class AtlasSemanticVocabulary {
    public static final List<String> MATURITY_MARKERS = List.of(
            "owned",
            "detinut",
            "durabil",
            "reopen",
            "relogin",
            "partial",
            "diagnostic",
            "surface",
            "surface-only",
            "suprafata");

    private static final List<List<String>> MATURITY_FAMILIES = List.of(
            List.of("owned", "detinut", "durabil", "reopen", "relogin"),
            List.of("partial", "incomplet"),
            List.of("diagnostic"),
            List.of("surface", "surface-only", "suprafata"));

    public static final List<String> LIMIT_MARKERS = List.of(
            "limit",
            "limita",
            "limite",
            "partial",
            "diagnostic",
            "surface",
            "surface-only",
            "suprafata",
            "missing",
            "lipseste",
            "incomplet",
            "neexplicat",
            "open",
            "boundary");

    public static final List<String> CAP_MARKERS = List.of(
            "cap",
            "capped",
            "plafon",
            "plafonat",
            "sub 95",
            "below 95",
            "95+");

    public static final List<String> BUSINESS_UNDERSTANDING_MARKERS = List.of(
            "intelegere business",
            "business understanding",
            "business comprehension",
            "comprehension",
            "calitatea atlasului",
            "atlas quality");

    public static final List<String> COMPLETENESS_BOUNDARY_MARKERS = List.of(
            "completitudine",
            "completeness",
            "coverage",
            "acoperire",
            "end-to-end",
            "limit",
            "limita",
            "boundary",
            "plafon",
            "cap");

    public static final List<String> UNIFORM_SCORE_MARKERS = List.of(
            "uniform",
            "scoruri uniforme",
            "aceeasi nota",
            "same score");

    public static final List<String> EVIDENCE_SURFACE_MARKERS = List.of(
            "tabel",
            "table",
            "trace",
            "sursa",
            "source",
            "layer",
            "obiect",
            "object",
            "flux",
            "flow",
            "dovada",
            "evidence",
            "proof");

    public static final List<List<String>> FLOW_STORY_FAMILIES = List.of(
            List.of("actor"),
            List.of("intent", "intentie"),
            List.of("visible action", "actiune vizibila", "actiune"),
            List.of("business object", "obiect business", "obiect"),
            List.of("observable result", "rezultat observabil", "rezultat"));

    private AtlasSemanticVocabulary() {
    }

    public static boolean containsAny(String text, List<String> markers) {
        return markers.stream().anyMatch(text::contains);
    }

    public static boolean containsAny(String text, String... markers) {
        return Arrays.stream(markers).anyMatch(text::contains);
    }

    public static int countAny(String text, List<String> markers) {
        return markers.stream().mapToInt(marker -> count(text, marker)).sum();
    }

    public static int count(String text, String token) {
        return AtlasText.count(text, token);
    }

    public static int maturityFamilyCoverage(String text) {
        return (int) MATURITY_FAMILIES.stream()
                .filter(family -> containsAny(text, family))
                .count();
    }

    public static int familyCoverage(String text, List<List<String>> families) {
        return (int) families.stream()
                .filter(family -> containsAny(text, family))
                .count();
    }
}
