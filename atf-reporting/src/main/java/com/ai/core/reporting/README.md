<a id="reporting-implementation-packages"></a>
# Pachetele implementării de raportare

<a id="overview"></a>
## Prezentare generală

Aceste pachete aparțin [atf-reporting](../../../../../../../README.md).
Implementează publicarea rapoartelor și integrarea TestNG peste contractele
de raportare Core.

Core gestionează ReportLogger, TestStatus, StepLogger și ReportingBackend. Acest modul
furnizează implementarea prin ServiceLoader și gestionează tipurile furnizorilor și
publicarea ciclului de viață Extent.

<a id="entry-points"></a>
## Puncte de intrare

- [Adaptoare](adapters/README.md): conversie către Extent.
- [Ascultător](listener/README.md): ciclu de viață TestNG, sesiuni de raportare și rezultate.
- [Raportare agregată](aggregate/README.md): publicarea combinată a cazurilor executate.

Utilitarele de atașare și audit păstrează identitatea testului, starea și
starea observată în browser. Execuția obișnuită nu necesită rapoarte istorice.

<a id="verification"></a>
## Verificare

Suita menținută de raportare verifică local contractele de ciclu de viață și
prezentare. Publicarea agregată Extent și atașamentele din browsere reale
au selecții explicite de acceptanță în
[Testare](../../../../../../../../docs/Testing.md).
