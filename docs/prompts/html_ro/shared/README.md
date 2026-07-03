# Primitive Comune De Raportare

## Scop

Acest folder este sursa romana pentru primitivele comune ale Reporting skill-ului. MCP si Atlas le consuma, dar nu le detin.

## Ownership

- Reporting skill detine shell-ul comun, CSS-ul partajat, score-card patterns, reguli de linkuire si primitive grafice reutilizabile.
- MCP detine sensul de package close-out, dovezi finale, Extent/XML/ledger/KPI si validarea raportului `analysis/langgraph-business-understanding.html`.
- Atlas detine business understanding, source trace, graph maturity si score truth.

## Documente

- `report-shell.html`: shell comun de raport, ownership, profile si checklist pentru documente noi.
- `score-card-patterns.html`: primitive pentru score wall, score-card, cap rationale si evidence row.
- `graph-style-primitives.html`: primitive vizuale pentru grafuri business, noduri si proof strip local.
- `artifact-linking.html`: reguli comune de linkuire a artefactelor, scope, freshness si interpretare.

## Stil HTML

Toate documentele HTML din acest folder trebuie sa urmeze acelasi stil ca documentele MCP:

- `<!DOCTYPE html>` si `<html lang="ro">`.
- CSS relativizat prin `../../assets/shared-report-reference.css`.
- `wrap`, `header`, `badge`, `card`, `details`, `summary-title`, `sumhint`.
- toolbar cu `Deschide sectiunile` si `Inchide sectiunile`.
- tabele pentru contracte si ownership.
- diagrame SVG simple cand ajuta intelegerea.
- script minim `toggleAll(openState)` la final.

## Ce Are Nevoie Fiecare Consumer

| Consumer | Ia din shared | Pastreaza la el |
|---|---|---|
| Reporting | shell, CSS, score-card shape, graph primitive, artifact link primitive | implementarea Java/Extent si validarea generica de shell |
| MCP | shell, score wall, graph readability, link primitives | close-out, gate, Extent/XML/ledger, package truth, langgraph final report |
| Atlas | shell, graph readability, proof strip shape, score-card shape, link primitives | business understanding, source trace, graph maturity, Atlas Test Audit |
| Core | shell si raportare generica | runtime, lifecycle, diagnostic si contracte core fara sens MCP/Atlas |

## Regula

Nu muta aici reguli MCP sau Atlas. Daca o cerinta este despre mecanica generica a raportului, ramane aici. Daca este despre publicabilitatea pachetului, merge in MCP. Daca este despre calitatea intelegerii business, merge in Atlas.
