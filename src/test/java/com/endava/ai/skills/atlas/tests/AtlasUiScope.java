package com.endava.ai.skills.atlas.tests;

import lombok.Value;
import lombok.experimental.Accessors;

import java.nio.file.Path;

@Value
@Accessors(fluent = true)
class AtlasUiScope {
    String name;
    Path projectRoot;
    String sourceRoot;
    int expectedTests;
    String description;

    Path testsRoot() {
        return projectRoot.resolve(sourceRoot);
    }
}
