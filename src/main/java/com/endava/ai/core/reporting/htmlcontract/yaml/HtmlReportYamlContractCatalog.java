package com.endava.ai.core.reporting.htmlcontract.yaml;

import com.endava.ai.core.reporting.htmlcontract.model.*;

import com.endava.ai.core.support.contract.yaml.ContractResource;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HtmlReportYamlContractCatalog {
    private static final String SHARED_RESOURCE = "skills/reporting/contracts/html-report-shared.yml";
    private static final String MCP_RESOURCE = "skills/mcp/contracts/report-sections.yml";
    private static final String ATLAS_RESOURCE = "skills/atlas/contracts/report-sections.yml";

    private static final List<HtmlReportYamlContractSpec> SPECS = Stream.of(
                    load(SHARED_RESOURCE),
                    load(MCP_RESOURCE),
                    load(ATLAS_RESOURCE))
            .flatMap(List::stream)
            .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
    private static final Map<HtmlReportShellProfile, HtmlReportYamlContractSpec> BY_PROFILE = indexByProfile();

    private HtmlReportYamlContractCatalog() {
    }

    public static HtmlReportYamlContractSpec require(HtmlReportShellProfile profile) {
        HtmlReportYamlContractSpec spec = BY_PROFILE.get(profile);
        if (spec == null) {
            throw new IllegalArgumentException("Missing HTML report YAML contract for profile: " + profile);
        }
        return spec;
    }

    public static List<HtmlReportYamlContractSpec> specs() {
        return SPECS;
    }

    private static List<HtmlReportYamlContractSpec> load(String resourcePath) {
        return ContractResource.of(resourcePath, entry -> HtmlReportYamlContractSpec.from(entry, resourcePath)).load();
    }

    private static Map<HtmlReportShellProfile, HtmlReportYamlContractSpec> indexByProfile() {
        Map<HtmlReportShellProfile, HtmlReportYamlContractSpec> indexed =
                new EnumMap<>(HtmlReportShellProfile.class);
        for (HtmlReportYamlContractSpec spec : SPECS) {
            HtmlReportYamlContractSpec previous = indexed.put(spec.profile(), spec);
            if (previous != null) {
                throw new IllegalStateException("Duplicate HTML report profile YAML contract: " + spec.profile());
            }
        }
        return Map.copyOf(indexed);
    }
}
