<a id="reporting-contract-tests"></a>
# Testele contractelor de raportare

<a id="overview"></a>
## Prezentare generală

Aceste verificări din [atf-tests](../../../../../../../../README.md) protejează contractele
de raportare Core și [implementarea de raportare](../../../../../../../../../atf-reporting/README.md):
ciclu de viață, pași, redare Extent, rezultate Allure, atașamente, audit de execuție
și publicare agregată.

<a id="test-ownership"></a>
## Responsabilitățile testelor

| Zonă | Exemplu |
|---|---|
| Delegarea pașilor și starea | [StepLoggerContractTest](regression/StepLoggerContractTest.java) |
| Izolarea suitei și a sesiunii | [ReportingManagerSessionContractTest](regression/ReportingManagerSessionContractTest.java) |
| Păstrarea eșecului din ciclul de viață | [TestListenerFailureContractTest](lifecycle/TestListenerFailureContractTest.java) |
| Publicarea prin furnizori | [adaptoare](adapters) |
| Numărători, temporizare și publicare agregată | [agregare](aggregate) |
| Atașamente din browsere reale | [capturi](screenshot) |

Păstrează explicite fixture-urile de eșec așteptat și rezultatul original al testului
când diagnosticele raportării eșuează. O regresie focalizată trebuie să protejeze
comportamentul observabil, fără a dubla tokenurile implementării.

<a id="execution"></a>
## Execuție

Rulează dintr-o copie izolată a reactorului:

<details>
<summary>Rulează comenzile: Execuție</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-reporting.xml" test
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-allure.xml" "-Dreporting.engine=allure" test
mvn -B -ntp -Paggregate test
```

</details>

Verificările redării în browser și ale capturilor folosesc profilul browser explicit,
după pregătire. [Testare](../../../../../../../../../docs/Testing.md) definește pregătirea și
acceptanța artefactelor. Inspectează rapoartele generate împreună cu rezultatele TestNG
și păstrează fiecare rulare separat.
