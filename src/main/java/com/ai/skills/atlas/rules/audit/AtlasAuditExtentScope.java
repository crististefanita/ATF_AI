package com.ai.skills.atlas.rules.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class AtlasAuditExtentScope {
    private AtlasAuditExtentScope() {
    }

    static boolean looksLikeFrameworkAtlasReport(Path extent) throws IOException {
        return Files.readString(extent).contains("FrameworkAtlasOutputContractTest");
    }

    static boolean mentionsCurrentAtlas(Path extent, Path atlasHtml) throws IOException {
        String html = Files.readString(extent);
        return html.contains(atlasHtml.toString())
                || html.contains(atlasHtml.getFileName().toString());
    }
}
