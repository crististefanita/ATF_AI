package com.ai.core.support.contract.capability;

import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.core.support.contract.yaml.YamlScalars;
import lombok.Value;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Readability map for the ATF core reporting system.
 *
 * <p>This stays data-driven because it is an agent-facing repair map, not
 * executable framework logic. The YAML resource keeps reporting ownership lanes
 * readable without turning this class into another hardcoded catalog.</p>
 */
public final class AtfCoreReportingArchitectureMap {
    public static final String RESOURCE = "atf/core/contracts/reporting-architecture-zones.yml";

    private static final List<Zone> ZONES = Collections.unmodifiableList(zonesFromYaml());

    private AtfCoreReportingArchitectureMap() {
    }

    public static List<Zone> zones() {
        return ZONES;
    }

    private static List<Zone> zonesFromYaml() {
        return SimpleYamlContractLoader.load(RESOURCE).stream()
                .map(AtfCoreReportingArchitectureMap::zone)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private static Zone zone(Map<String, String> entry) {
        return new Zone(
                required(entry, "id"),
                required(entry, "capabilityId"),
                required(entry, "owner"),
                Path.of(required(entry, "sourceRoot")),
                required(entry, "input"),
                required(entry, "output"),
                required(entry, "contractTest"),
                required(entry, "suiteXml"),
                required(entry, "responsibility")
        );
    }

    private static String required(Map<String, String> entry, String key) {
        return YamlScalars.requiredString(entry, key, RESOURCE, "ATF reporting architecture zone");
    }

    @Value
    @Accessors(fluent = true)
    public static class Zone {
        String id;
        String capabilityId;
        String owner;
        Path sourceRoot;
        String input;
        String output;
        String contractTest;
        String suiteXml;
        String responsibility;
    }
}
