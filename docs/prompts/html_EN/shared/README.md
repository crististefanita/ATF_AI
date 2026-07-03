# Shared Reporting Primitives

## Purpose

This folder is the English source for the shared primitives of the Reporting skill. MCP and Atlas consume them, but they do not own them.

## Ownership

- Reporting skill owns the shared shell, shared CSS, score-card patterns, artifact-linking rules and reusable graph primitives.
- MCP owns package close-out meaning, final evidence, Extent/XML/ledger/KPI and validation of `analysis/langgraph-business-understanding.html`.
- Atlas owns business understanding, source trace, graph maturity and score truth.

## Documents

- `report-shell.html`: shared report shell.
- `score-card-patterns.html`: score primitives.
- `graph-style-primitives.html`: visual graph primitives.
- `artifact-linking.html`: shared artifact-linking rules.

## Rule

Do not move MCP or Atlas rules here. If a requirement is about generic report mechanics, it stays here. If it is about package publishability, it belongs to MCP. If it is about business understanding quality, it belongs to Atlas.
