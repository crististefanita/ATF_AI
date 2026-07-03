package com.ai.atf.core.skills.architecture;

import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entry;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entriesByKind;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.path;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.required;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.values;

@Listeners(TestListener.class)
public class AtfSkillPromptRoutingContractTest {

    @Test
    public void coreSupportKernelPublishesLocalOwnershipReadme() throws IOException {
        Path supportReadme = path("src/main/java/com/ai/core/support/README.md");
        Assert.assertTrue(Files.isRegularFile(supportReadme),
                "Core support kernel must publish a local README because it is shared by Core, Reporting, MCP and Atlas.");
        String readme = Files.readString(supportReadme);
        for (String token : List.of(
                "Core Support Kernel",
                "domain-neutral",
                "YAML contract loading",
                "Do not add MCP",
                "Do not add Atlas",
                "core.reporting",
                "testng-atf-core-sanity.xml")) {
            Assert.assertTrue(readme.contains(token),
                    "Core support README must document `" + token + "`");
        }

        String coreReadme = Files.readString(path("src/main/java/com/ai/core/README.md"));
        Assert.assertTrue(coreReadme.contains("support/README.md"),
                "Core README must route maintainers to the local core/support ownership README.");
    }

    @Test
    public void skillArchitectureUsesRomanianPromptRootsForSkillExecution() {
        Assert.assertEquals(required(entry("skill.core"), "promptRoot"), "docs/prompts/html_ro/shared",
                "Core should route shared prompt primitives through the Romanian source prompt family.");
        Assert.assertEquals(required(entry("skill.reporting"), "promptRoot"), "docs/prompts/html_ro/shared",
                "Reporting should route shared report primitives through the Romanian source prompt family.");
        Assert.assertEquals(required(entry("skill.business-understanding"), "promptRoot"), "docs/prompts/html_ro/shared",
                "Business Understanding should route shared semantic prompt primitives through the Romanian source prompt family.");
        Assert.assertEquals(required(entry("skill.mcp"), "promptRoot"), "docs/prompts/html_ro/mcp",
                "MCP skill execution should start from the Romanian MCP prompt source family.");
        Assert.assertEquals(required(entry("skill.atlas"), "promptRoot"), "docs/prompts/html_ro/atlas",
                "Atlas skill execution should start from the Romanian Atlas prompt source family.");
        Assert.assertEquals(required(entry("prompt.assets"), "promptRoot"), "docs/prompts",
                "Prompt assets may own the whole prompt tree because they govern html_ro/html_EN mirrors and shared assets.");
    }

    @Test
    public void skillArchitecturePublishesShortReadingPathsForAgents() throws IOException {
        List<Map<String, String>> routedEntries = new ArrayList<>(entriesByKind("skill"));
        routedEntries.add(entry("prompt.assets"));

        for (Map<String, String> routedEntry : routedEntries) {
            String id = required(routedEntry, "id");
            List<String> readingPath = values(routedEntry, "readingPath");
            Assert.assertFalse(readingPath.isEmpty(), id + " must declare a short agent readingPath.");
            Assert.assertEquals(readingPath.get(0), required(routedEntry, "readme"),
                    id + " readingPath must start from the declared owner README.");
            String readme = Files.readString(path(required(routedEntry, "readme")));
            Assert.assertTrue(readme.contains("## Reading Path"),
                    id + " README must expose a local Reading Path section.");
            for (String step : readingPath) {
                Assert.assertTrue(Files.exists(path(step)),
                        id + " readingPath references missing file or directory: " + step);
            }
        }
    }
}
