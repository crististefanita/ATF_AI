package com.ai.skills.atlas.contract.execution;

import com.ai.core.support.contract.execution.AtfExecutionProfile;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public enum AtlasExecutionProfile implements AtfExecutionProfile {
    SANITY("atlas-sanity", "src/test/resources/skills/atlas/testng-atlas-sanity.xml"),
    SMOKE("atlas-smoke", "src/test/resources/skills/atlas/testng-atlas-smoke.xml"),
    BUSINESS_CLOSEOUT("atlas-business-closeout", "src/test/resources/skills/atlas/testng-atlas-business-closeout.xml"),
    FULL("atlas-full", "src/test/resources/skills/atlas/testng-atlas.xml");

    private final String id;
    private final String suiteXml;

    AtlasExecutionProfile(String id, String suiteXml) {
        this.id = id;
        this.suiteXml = suiteXml;
    }

    public static AtlasExecutionProfile fromId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(profile -> profile.id.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Atlas execution profile: " + id));
    }

    public static List<String> ids() {
        return Arrays.stream(values())
                .map(AtlasExecutionProfile::id)
                .collect(Collectors.toUnmodifiableList());
    }
}
