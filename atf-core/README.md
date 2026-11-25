# atf-core

<a id="overview"></a>
## Prezentare generală

Core oferă contracte reutilizabile de execuție UI și API fără dependențe de SDK-urile
Selenium, Playwright, Extent sau Allure. Coordonatele sunt
`com.ai:atf-core:1.0.0`; compilarea Java 11 este moștenită de la
[atf-parent](../pom.xml).

<a id="ownership-and-entry-points"></a>
## Responsabilități și puncte de intrare

| Zonă | Punct de intrare și responsabilitate |
|---|---|
| Configurație | [ConfigManager](src/main/java/com/ai/core/support/config/ConfigManager.java), opțiuni tipizate și valori implicite din classpath |
| Contracte UI | [UIEngine](src/main/java/com/ai/ui/core/engine/UIEngine.java), capabilități, locatori și valori de cerere/răspuns |
| Compoziție UI | [UiEngineProvider](src/main/java/com/ai/ui/core/engine/UiEngineProvider.java), registru și fabrică |
| Ciclu de viață UI | [BaseTestUI](src/main/java/com/ai/ui/core/runtime/BaseTestUI.java), sesiune și metadate de execuție |
| Execuție API | [BaseTestAPI](src/main/java/com/ai/api/core/BaseTestAPI.java), execuție HTTP, reîncercări, interogare repetată și validare |
| Contracte de raportare | [ReportLogger](src/main/java/com/ai/core/reporting/ReportLogger.java), TestStatus și [SPI ReportingBackend](src/main/java/com/ai/core/reporting/spi/ReportingBackend.java) |
| Pași | [StepLogger](src/main/java/com/ai/core/reporting/StepLogger.java), domenii de pas, ieșire în consolă și delegare neutră către rapoarte |

[Suportul Core](src/main/java/com/ai/core/support/README.md) gestionează primitivele
reutilizabile de configurație, metadate, YAML și HTML. UIActions rămâne neutru în
Core; paginile produsului, serviciile, fluxurile și aserțiunile de afaceri aparțin
[atf-tests](../atf-tests/README.md).

<a id="dependencies-and-extension"></a>
## Dependențe și extensie

Core nu depinde de alt modul ATF. Consumatorii adaugă un adaptor de browser
pentru execuție UI și atf-reporting pentru publicare Extent/Allure.
Java ServiceLoader descoperă implementările UiEngineProvider și ReportingBackend.
Semnăturile publice Core rămân independente de tipurile SDK-urilor furnizorilor.
Fațada neutră de compatibilitate [TestListener](src/main/java/com/ai/core/reporting/listener/TestListener.java)
transmite evenimentele de ciclu de viață către implementarea selectată.

Consultă [Arhitectură](../docs/Architecture.md) pentru compoziția furnizorilor și
secvența de execuție.

<a id="configuration"></a>
## Configurație

[framework.properties](src/main/resources/framework.properties) furnizează valorile implicite.
Proprietățile JVM suprascriu chei individuale; `framework.properties` poate selecta
un fișier explicit. Printre valorile implicite importante se numără `ui.engine=playwright`,
`ui.headless=true`, `ui.wait.timeout.seconds=20`,
`api.wait.timeout.seconds=10` și `reporting.engine=extent`.
Implementările de browser și raportare sunt furnizate de modulele lor.

<a id="verification"></a>
## Verificare

Dintr-o copie izolată a reactorului:

```powershell
mvn -B -ntp -pl atf-core -am test
mvn -B -ntp -pl atf-tests -am "-Dtest=ConfigManagerFileContractTest,BaseTestAPIContractTest,BaseTestUIContractTest,StepLoggerContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

Prima comandă rulează verificările native Core; a doua selectează regresiile
între module. [Testare](../docs/Testing.md) descrie verificările complete locale,
de browser și de raportare.
