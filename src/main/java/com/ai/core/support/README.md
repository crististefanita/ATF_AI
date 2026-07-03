# Core Support Kernel

## Purpose

`com.ai.core.support` is the shared kernel for low-level reusable primitives used by Core, Reporting, MCP, Atlas and future skills.

It owns stable support building blocks only:

- runtime/config keys and small typed runtime options;
- ATF contract annotations, narrative ids, source-path helpers and capability metadata;
- shared YAML contract loading and scalar parsing;
- small diagnostic HTML primitives.

## Change Here When

- More than one skill needs the same low-level primitive.
- A YAML loader, scalar parser, contract annotation or source-path helper would otherwise be duplicated in MCP, Atlas, Reporting or Core.
- A runtime/config key is generic framework infrastructure, not business/package/report meaning.

## Do Not

- Do not add MCP package audit, close-out, candidate/html-ex or protected-root policy here.
- Do not add Atlas graph, source-to-business trace, score-truth or generated Atlas artifact rules here.
- Do not add Extent/Spark shell rendering, runtime audit sections or visual report layout here; those stay in `core.reporting`.
- Do not add TestNG assertion helpers here; test-only helpers stay under `src/test/java`.
- Do not let this package become a second Core. Keep it small, typed and domain-neutral.

## Ownership

| Area | Package | Rule |
|---|---|---|
| Config/runtime support | `core.support.config` | Generic runtime values only. |
| Contract metadata | `core.support.contract` | Shared annotations and framework contract vocabulary. |
| YAML loading | `core.support.contract.yaml` | The only shared YAML/scalar parsing surface. |
| Diagnostic HTML primitives | `core.support.html` | Compact reusable HTML fragments, not report shells. |

## Verification

- Run `src/test/resources/atf/core/suites/core/testng-atf-core-sanity.xml`.
- Run `AtfSkillArchitectureContractTest` after changing skill boundaries or ownership.
- Run `AtfCoreYamlContractManifestTest` after changing shared YAML contract loading.
- Run MCP/Atlas sanity only when their consumers are affected; do not use them as a substitute for Core sanity.
