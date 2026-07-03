package com.endava.ai.skills.mcp.rules.source.kpi;

import com.endava.ai.skills.mcp.rules.source.inventory.SourceEvolutionInventory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SourceEvolutionKpiReader {
    private static final Pattern NUMERIC_KPI = Pattern.compile(
            "(?im)(?:^|[|\\s])%s\\s*(?:[:=|]|</?td>\\s*<td>)\\s*(\\d+)");

    private SourceEvolutionKpiReader() {
    }

    static Integer readNumericValue(String content, String key) {
        Matcher matcher = Pattern.compile(String.format(NUMERIC_KPI.pattern(), Pattern.quote(key))).matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    static Map<String, Integer> declaredValues(String ledgerText, SourceEvolutionInventory inventory) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (SourceEvolutionKpiCatalog.KpiSpec spec : SourceEvolutionKpiCatalog.rootKpis()) {
            putIfPresent(values, spec.id(), ledgerText);
        }
        for (String runKey : inventory.runFileCounts().keySet()) {
            putIfPresent(values, runKey, ledgerText);
        }
        for (SourceEvolutionKpiCatalog.KpiSpec spec : SourceEvolutionKpiCatalog.diagnosticKpis()) {
            putIfPresent(values, spec.id(), ledgerText);
        }
        return values;
    }

    private static void putIfPresent(Map<String, Integer> values, String key, String ledgerText) {
        Integer value = readNumericValue(ledgerText, key);
        if (value != null) {
            values.put(key, value);
        }
    }
}
