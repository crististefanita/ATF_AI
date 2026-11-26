<a id="reporting-listener"></a>
# Ascultătorul de raportare

<a id="overview"></a>
## Prezentare generală

Ascultătorul din [atf-reporting](../../../../../../../../README.md)
direcționează evenimentele ciclului de viață TestNG către sesiunile de raportare și
publică rezultatele și atașamentele de execuție.

<a id="entry-points"></a>
## Puncte de intrare

[ReportingTestListener](ReportingTestListener.java) coordonează colaboratorii ciclului de viață și ai execuției.

- Fațada de compatibilitate [TestListener](../../../../../../../../../atf-core/src/main/java/com/ai/core/reporting/listener/TestListener.java) din Core transmite evenimentele TestNG prin SPI-ul de raportare.
- Modulul de raportare gestionează ascultătorul concret și publicarea Extent.
- Clasele de bază Core nu declară un ascultător al unui furnizor.

Ascultătorul trebuie să păstreze identitatea testului responsabil pe parcursul inițializării,
eșecului testului, închiderii și capturii finale. Eșecurile de diagnostic nu trebuie
să înlocuiască rezultatul original al testului.

<a id="verification"></a>
## Verificare

[Verificările ciclului de viață la eșec](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/lifecycle/TestListenerFailureContractTest.java)
și [verificările sesiunilor](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/regression/ReportingManagerSessionContractTest.java)
rulează în suita locală menținută. Verificările atașamentelor din browser necesită
profilul browser explicit. Consultă [Testare](../../../../../../../../../docs/Testing.md).
