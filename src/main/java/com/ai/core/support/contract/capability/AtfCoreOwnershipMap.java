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
 * Agent-facing ownership map for ATF core surfaces.
 *
 * <p>Atlas and MCP use rule catalogs because they validate generated output.
 * ATF core is different: it is reusable runtime infrastructure, so this map
 * names ownership lanes, repair targets and the contract that protects each
 * lane. The rows live in YAML so future refactors update a readable repair map
 * instead of editing Java control flow.</p>
 */
public final class AtfCoreOwnershipMap {
    public static final String RESOURCE = "atf/core/contracts/ownership-zones.yml";

    private static final List<Zone> ZONES = Collections.unmodifiableList(zonesFromYaml());

    private AtfCoreOwnershipMap() {
    }

    public static List<Zone> zones() {
        return ZONES;
    }

    public static String toMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# ATF Core Ownership Matrix").append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("Generated from ").append(RESOURCE)
                .append(". Use this as the first repair map for ATF core, ui.core, api.core and reporting changes.")
                .append(System.lineSeparator()).append(System.lineSeparator());
        markdown.append("| Zone | Capability | Owner | Source root | Trigger/Input | Output | Contract | Suite | Repair target | Responsibility |")
                .append(System.lineSeparator());
        markdown.append("|---|---|---|---|---|---|---|---|---|---|").append(System.lineSeparator());
        for (Zone zone : ZONES) {
            markdown.append("| ")
                    .append(escape(zone.id())).append(" | ")
                    .append(escape(zone.capabilityId())).append(" | ")
                    .append(escape(zone.owner())).append(" | `")
                    .append(escape(zone.sourceRoot().toString())).append("` | ")
                    .append(escape(zone.input())).append(" | ")
                    .append(escape(zone.output())).append(" | `")
                    .append(escape(zone.contractTest())).append("` | `")
                    .append(escape(zone.suiteXml())).append("` | `")
                    .append(escape(zone.repairTarget().toString())).append("` | ")
                    .append(escape(zone.responsibility())).append(" |")
                    .append(System.lineSeparator());
        }
        return markdown.toString();
    }

    private static List<Zone> zonesFromYaml() {
        return SimpleYamlContractLoader.load(RESOURCE).stream()
                .map(AtfCoreOwnershipMap::zone)
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
                Path.of(required(entry, "repairTarget")),
                required(entry, "responsibility")
        );
    }

    private static String required(Map<String, String> entry, String key) {
        return YamlScalars.requiredString(entry, key, RESOURCE, "ATF core ownership zone");
    }

    private static String escape(String value) {
        return value.replace("|", "\\|").replace(System.lineSeparator(), " ");
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
        Path repairTarget;
        String responsibility;
    }
}
