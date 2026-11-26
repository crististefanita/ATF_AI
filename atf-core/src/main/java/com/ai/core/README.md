<a id="core-runtime-packages"></a>
# Pachetele de execuție Core

<a id="overview"></a>
## Prezentare generală

Aceste pachete aparțin [atf-core](../../../../../../README.md).
Gestionează configurația și suportul comun, contractul neutru de raportare,
pașii din consolă și delegarea sesiunilor.

<a id="entry-points"></a>
## Puncte de intrare

- [Suport Core](support/README.md): primitive de configurație, metadate, YAML și HTML.
- [ReportLogger](reporting/ReportLogger.java): operații de raportare independente de Extent.
- [StepLogger](reporting/StepLogger.java): domenii ale pașilor, ieșire în consolă și delegare către rapoarte.

Implementările de raportare, ascultătorii TestNG și publicarea se află în
[atf-reporting](../../../../../../../atf-reporting/README.md).
Semnăturile publice Core rămân independente de SDK-urile furnizorilor de browser și raportare.

<a id="verification"></a>
## Verificare

Rulează verificările native Core și regresiile între module afectate dintr-o
copie izolată a reactorului. [Testare](../../../../../../../docs/Testing.md) definește
comenzile menținute și regulile pentru dovezi.
