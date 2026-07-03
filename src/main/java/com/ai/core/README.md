# ATF Core

## Purpose

`com.ai.core` is the reusable ATF runtime library. It owns generic configuration, lifecycle, listener, reporting infrastructure and shared runtime support that can be used by ATF, MCP, Atlas and future modules.

## Entry Points

- `listener`: TestNG lifecycle integration and runtime event routing.
- `reporting`: reusable reporting infrastructure; see `reporting/README.md` for its local ownership map.
- `support`: shared support kernel for typed runtime config, contract annotations, YAML contract loading and small HTML primitives consumed by Core, MCP, Atlas and future skills; see `support/README.md` for its boundary.

## Reading Path

1. Start here to decide whether the change is generic ATF runtime/library work.
2. Check `src/main/resources/atf-core/contracts/skills/skill-architecture.yml` for ownership, forbidden imports and sanity/deep suites.
3. Run `src/test/resources/atf/core/suites/core/testng-atf-core-sanity.xml` before trusting Core changes.

## Change Here When

- A behavior is generic framework infrastructure, not MCP package governance and not Atlas business understanding.
- A contract/YAML/HTML helper belongs in `core.support` because multiple skills need the same low-level primitive.
- MCP and Atlas both need the same low-level runtime/reporting capability.
- A public extension point needs to be separated from internal framework mechanics.

## Do Not

- Do not add MCP package close-out, package audit, html-ex candidate, KPI gate or protected-root policy here.
- Do not add Atlas business graph, source-to-business trace, score-truth or skill-readiness rules here.
- Do not let `core.support` become a second Core; keep it to reusable contract primitives, YAML loading and small HTML primitives.
- Do not bypass `ConsoleLogger` or the framework lifecycle/reporting gateways.

## Verification

- Run `testng-atf-core-sanity.xml` for Core health.
- Run `AtfSkillArchitectureContractTest` after changing Core/MCP/Atlas ownership, prompt ownership, or canonical skill flow contracts.
- Run `CoreMcpBoundaryContractTest` and `AtfCoreYamlContractManifestTest` after boundary or YAML ownership changes.
- Run MCP or Atlas sanity only when changing their consumers, not as a substitute for Core sanity.
