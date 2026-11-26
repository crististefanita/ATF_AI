<a id="reporting-adapters"></a>
# Adaptoare de raportare

<a id="overview"></a>
## Prezentare generală

Adaptoarele din [atf-reporting](../../../../../../../../README.md)
transformă operațiile neutre de raportare în Extent, păstrând stările,
ierarhia pașilor și atașamentele.

<a id="entry-points"></a>
## Puncte de intrare

| Adaptor | Responsabilitate |
|---|---|
| [ExtentAdapter](ExtentAdapter.java) | Ciclul de viață și redarea raportului HTML Extent |

Tipurile furnizorilor rămân în modulul de raportare. Operațiile comune folosesc
ReportLogger și TestStatus din Core. Configurația de execuție selectează motorul
obișnuit de raportare; publicarea agregată are o selecție explicită proprie.

<a id="verification"></a>
## Verificare

[Regresiile adaptoarelor](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/adapters)
acoperă conversia stărilor, directoarele, compoziția HTML și comportamentul Extent.
[Testare](../../../../../../../../../docs/Testing.md) definește subseturile de raportare,
verificările rapoartelor în browsere reale și inspecția artefactelor.
