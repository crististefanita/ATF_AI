# Framework documentation

Choose a guide by the decision you need to make. The root explains the framework;
owner guides keep the detailed contract, and feedback keeps reproducible gaps.

## Find the owner

| Reader's question | Start here |
|---|---|
| What is ATF, and which library do I need? | [Root overview](../README.md) |
| Who owns an API, provider or dependency boundary? | [Architecture](Architecture.md) |
| Where are runnable recipes without obscuring strategy? | [Collapsed command cookbook](Testing_Commands.md) |
| Which existing checks establish the behavior I changed? | [Testing](Testing.md) and [Atlas coverage map](../atf-atlas/Coverage_Map.md) |
| Where should edits, validation copies, caches and evidence live? | [Development](Development.md) |
| How should code, tests, READMEs and diagrams be reviewed? | [Governance policies](governance/README.md) |
| How do UI/API lifecycle, steps and reporting contracts work? | Living architecture in [Markdown](Living_Architecture_UI_API_doc_v1_0.md) and [HTML](Living_Architecture_UI_API_doc_v1_0.html) |
| Which acceptance criteria remain open? | [Feedback and TODOs](feedback/README.md) |
| How are reports preserved and another project mapped? | [Artifacts](../artifacts/README.md) and [reusable Atlas intake](../artifacts/atlas/README.md) |

## Module owners

| Module | Owner document |
|---|---|
| Neutral contracts, configuration and UI lifecycle | [atf-core](../atf-core/README.md) |
| Reusable REST execution and response assertions | [atf-api](../atf-api/README.md) |
| Playwright implementation | [atf-playwright](../atf-playwright/README.md) |
| Selenium implementation | [atf-selenium](../atf-selenium/README.md) |
| Extent reporting lifecycle | [atf-reporting](../atf-reporting/README.md) |
| Code and documentation governance | [atf-governance](../atf-governance/README.md) |
| Framework/product helpers and annotated checks | [atf-tests](../atf-tests/README.md) |

Owner documents describe current contracts. Feedback records retain source
checkpoints, commands and external evidence; historical results keep their
original execution scope.

## Documentation boundaries

The [code and documentation policy](governance/Code_And_Documentation_Policy.md) owns review rules.

- Module READMEs describe use and verification; package guides explain local responsibilities.
- The separate [Atlas](../atf-atlas/README.md) connects capabilities to existing source/tests and their proof limits.
- Link to these owners rather than copying their contracts into another guide.
