# atf-tests

## Overview

This module owns governance, cross-module regressions, product support and
explicit application journeys. Its coordinates are `com.ai:atf-tests:1.0.0`.
Its framework dependencies and support code are test scoped; this module is
not a production runtime library.

## Ownership

| Area | Location |
|---|---|
| Framework and governance checks | [com.ai.atf](src/test/java/com/ai/atf) |
| UI product pages, workflows and assertions | [com.ai.ui.support](src/test/java/com/ai/ui/support) |
| API product endpoints, models and factories | [com.ai.api.support](src/test/java/com/ai/api/support) |
| Maintained TestNG selections | [suites](src/test/resources/suites) |
| Fixture data and report settings | [test resources](src/test/resources) |

Governance protects the five-module dependency graph, vendor boundaries,
maintained documentation and source ownership. Native library checks remain
with their owning modules.

The module uses [atf-core](../atf-core/README.md),
[atf-playwright](../atf-playwright/README.md),
[atf-selenium](../atf-selenium/README.md) and
[atf-reporting](../atf-reporting/README.md) as test dependencies.

## Execution

For a failed source/documentation guard, use the
[diagnostic workflow](../docs/governance/Failure_Diagnostics.md): preserve the
observed finding, inspect its owning contract and verify the proposed correction
with the maintained selection. A repair hint is not proof of a root cause.

From an isolated reactor copy:

```powershell
mvn -B -ntp test
mvn -B -ntp -pl atf-tests -am test
mvn -B -ntp -Pbrowser test
mvn -B -ntp -Paggregate test
```

The default selects framework-local, including governance. The browser profile
selects framework-browser only in this module. The aggregate profile executes
the maintained aggregate suite and publishes both report formats.
[Testing](../docs/Testing.md) owns browser preparation, filtered commands,
application configuration and artifact acceptance.

Application journeys remain explicit class selections. They need configured
URLs/accounts and can create data. Local framework results do not establish
public-site acceptance.

The [retained UI selection](../docs/Testing.md#retained-product-ui-audit) runs
the same registration, account and profile methods once through each browser
provider. Product test methods compose services/components and validations;
actions record technical completion, while validations record expected/actual
business evidence and a terminal verdict. A negative application scenario passes
only when its rejection oracle succeeds. See the
[living principles](../docs/Living_Architecture_UI_API_doc_v1_0.md#retained-principles)
for the maintained responsibilities and explicit data-cleanup limit.

## Evidence and maintenance

The existing governance checks enforce the maintained suite catalog, explicit
live/failure-fixture exclusions, owner documentation and feedback lifecycle.
The living UI/API guide has a canonical Markdown source and a checked HTML
projection; the documentation Atlas stays outside the runtime reactor.

Results belong to this module's target directories in the disposable validation
copy. Preserve each run's source checkpoint, exact selection, counts and
artifacts outside the development and live source trees.
[Feedback](../docs/feedback/README.md) retains active TODOs and historical proof;
new results must identify their own execution scope.

Keep focused regressions next to the behavior they protect. Update
[reporting test ownership](src/test/java/com/ai/atf/core/reporting/README.md)
when changing that test area.
