# Core support

## Overview

Shared support belongs to [atf-core](../../../../../../../README.md).
It provides configuration, contract metadata, YAML loading and compact HTML
primitives used by reusable runtime code.

## Entry points

| Package | Responsibility |
|---|---|
| [config](config) | Classpath or explicit-file configuration and typed runtime options |
| [contract](contract) | Contract identifiers, metadata and YAML loading |
| [html](html) | Small reusable HTML structures |

Packaged runtime resources resolve from the classpath. An explicit configuration
file is selected with the framework.properties setting. Development source
lookup resolves the reactor and owning module; it is not a runtime resource
fallback.

## Verification

[Configuration regressions](../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java)
and [YAML regressions](../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/support/contracts/yaml)
protect these contracts in the maintained local suite.
Use [Testing](../../../../../../../../docs/Testing.md) for isolated native and
cross-module execution.
