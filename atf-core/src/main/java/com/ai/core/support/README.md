<a id="core-support"></a>
# Suport Core

<a id="overview"></a>
## Prezentare generală

Suportul comun aparține [atf-core](../../../../../../../README.md).
Oferă configurație, metadate de contract, încărcare YAML și primitive HTML
compacte folosite de codul de execuție reutilizabil.

<a id="entry-points"></a>
## Puncte de intrare

| Pachet | Responsabilitate |
|---|---|
| [config](config) | Configurație din classpath sau fișier explicit și opțiuni tipizate de execuție |
| [contract](contract) | Identificatori de contract, metadate și încărcare YAML |
| [html](html) | Structuri HTML mici și reutilizabile |

Resursele de execuție împachetate se rezolvă din classpath. Un fișier explicit
de configurație este selectat prin setarea framework.properties. Căutarea surselor
în dezvoltare identifică reactorul și modulul responsabil; nu este o alternativă
pentru rezolvarea resurselor de execuție.

<a id="verification"></a>
## Verificare

[Regresiile de configurație](../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java)
și [regresiile YAML](../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/support/contracts/yaml)
protejează aceste contracte în suita locală menținută.
Folosește [Testare](../../../../../../../../docs/Testing.md) pentru execuția izolată
a verificărilor native și între module.
