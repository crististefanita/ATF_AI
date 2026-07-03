package com.ai.core.support.contract.capability;

import com.ai.core.support.contract.AtfContractArea;
import com.ai.core.support.contract.AtfContractProfile;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.core.support.contract.yaml.YamlEnums;
import com.ai.core.support.contract.yaml.YamlScalars;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Canonical catalog of framework capabilities proven by the ATF contract suite.
 *
 * <p>The catalog is deliberately declarative: each row ties one capability id to
 * a profile, area, severity, expected invariant, suite XML, primary evidence,
 * report expectation, and repair hint. Contract tests use it as the stable
 * index for report matrices and coverage checks.</p>
 */
public final class AtfCapabilityCatalog {
    public static final String RESOURCE = "atf/core/contracts/capabilities.yml";

    private static final List<AtfCapabilitySpec> CAPABILITIES = Collections.unmodifiableList(capabilities());
    private static final Map<String, AtfCapabilitySpec> BY_ID = Collections.unmodifiableMap(byId(CAPABILITIES));

    private AtfCapabilityCatalog() {
    }

    public static List<AtfCapabilitySpec> all() {
        return CAPABILITIES;
    }

    public static AtfCapabilitySpec require(String id) {
        AtfCapabilitySpec spec = BY_ID.get(id);
        if (spec == null) {
            throw new IllegalArgumentException("Unknown ATF capability: " + id);
        }
        return spec;
    }

    public static List<AtfCapabilitySpec> byProfile(AtfContractProfile profile) {
        Objects.requireNonNull(profile, "profile");
        return CAPABILITIES.stream()
                .filter(spec -> spec.profile() == profile)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public static Map<AtfContractProfile, List<AtfCapabilitySpec>> groupedByProfile() {
        Map<AtfContractProfile, List<AtfCapabilitySpec>> grouped = new EnumMap<>(AtfContractProfile.class);
        for (AtfContractProfile profile : AtfContractProfile.values()) {
            grouped.put(profile, byProfile(profile));
        }
        return Collections.unmodifiableMap(grouped);
    }

    private static List<AtfCapabilitySpec> capabilities() {
        return SimpleYamlContractLoader.load(RESOURCE).stream()
                .map(AtfCapabilityCatalog::spec)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private static AtfCapabilitySpec spec(Map<String, String> entry) {
        return AtfCapabilitySpec.builder(required(entry, "id"))
                .profile(enumValue(AtfContractProfile.class, entry, "profile"))
                .area(enumValue(AtfContractArea.class, entry, "area"))
                .severity(enumValue(AtfCapabilitySeverity.class, entry, "severity"))
                .owner(required(entry, "owner"))
                .expected(required(entry, "expected"))
                .suiteXml(required(entry, "suiteXml"))
                .primaryEvidence(required(entry, "primaryEvidence"))
                .reportExpectation(required(entry, "reportExpectation"))
                .repairHint(required(entry, "repairHint"))
                .build();
    }

    private static String required(Map<String, String> entry, String key) {
        return YamlScalars.requiredString(entry, key, RESOURCE, "ATF capability");
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, Map<String, String> entry, String key) {
        return YamlEnums.enumValue(type, required(entry, key), key, RESOURCE, entry);
    }

    private static Map<String, AtfCapabilitySpec> byId(List<AtfCapabilitySpec> specs) {
        Map<String, AtfCapabilitySpec> result = new LinkedHashMap<>();
        for (AtfCapabilitySpec spec : specs) {
            AtfCapabilitySpec previous = result.put(spec.id(), spec);
            if (previous != null) {
                throw new IllegalStateException("Duplicate ATF capability id: " + spec.id());
            }
        }
        return result;
    }
}
