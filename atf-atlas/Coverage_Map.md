<a id="capability-coverage-map"></a>
# Harta acoperirii capabilităților

<a id="how-to-read-the-map"></a>
## Cum se citește harta

Fiecare rând numește o capabilitate, sursa/suportul său, un criteriu de verificare
existent și selecția menținută care îl atinge. Aceasta este o hartă de lectură selectată,
fără pretenția unui inventar complet sau a unei execuții noi. Consultă
[Testare](../docs/Testing.md) pentru comenzi și [feedback](../docs/feedback/README.md)
pentru rezultate observate și acceptanță nerezolvată.

| Capabilitate și rezultat observat | Sursă sau suport | Criteriu existent de verificare | Selecție menținută și limitele dovezii |
|---|---|---|---|
| Pornirea alege valorile implicite din classpath sau un fișier explicit; suprascrierile rămân deterministe | [ConfigManager](../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java) | [ConfigManagerFileContractTest](../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java) | [Suita locală](../atf-tests/src/test/resources/suites/framework-local.xml): contract local de configurație; fără afirmație despre disponibilitatea aplicației |
| Consumatorii UI rezolvă furnizorii instalați prin contracte neutre | [UiEngineRegistry](../atf-core/src/main/java/com/ai/ui/core/engine/UiEngineRegistry.java), [furnizorul Playwright](../atf-playwright/src/main/java/com/ai/ui/core/engine/playwright/PlaywrightProvider.java), [furnizorul Selenium](../atf-selenium/src/main/java/com/ai/ui/core/engine/selenium/SeleniumProvider.java) | [Testul registrului Core](../atf-core/src/test/java/com/ai/ui/core/engine/UiEngineRegistryTest.java), [contractul registrului între module](../atf-tests/src/test/java/com/ai/atf/ui/component/registry/UiEngineRegistryContractTest.java) | Verificările native Core și [suita locală](../atf-tests/src/test/resources/suites/framework-local.xml): descoperire, furnizori absenți/duplicați și delegare; descoperirea nu pornește un browser |
| Consumatorii API cer explicit acreditările de scriere și păstrează separarea API/execuție | [BaseTestAPI](../atf-core/src/main/java/com/ai/api/core/BaseTestAPI.java), [clientul API](../atf-core/src/main/java/com/ai/api/core/client/ApiClient.java) | [BaseTestAPIContractTest](../atf-tests/src/test/java/com/ai/atf/core/support/base/BaseTestAPIContractTest.java), [arhitectura execuției API](../atf-tests/src/test/java/com/ai/atf/core/support/base/ApiCoreExecutionArchitectureContractTest.java) | [Suita locală](../atf-tests/src/test/resources/suites/framework-local.xml): contracte de pregătire și arhitectură; nu stabilește scrieri reușite într-un API extern |
| Consumatorii de browser primesc comportamente echivalente pentru locatori, zoom, fereastră și capturi | [Contractul motorului UI](../atf-core/src/main/java/com/ai/ui/core/engine/UIEngine.java) și cele două implementări de furnizori de mai sus | [Paritatea locatorilor](../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java), [paritatea execuției](../atf-tests/src/test/java/com/ai/atf/core/reporting/screenshot/UiEngineConfiguredRuntimeParityContractTest.java) | [Suita de browser](../atf-tests/src/test/resources/suites/framework-browser.xml): sesiuni reale de browser pe fixture-uri locale controlate; fără acceptanță E2E de produs sau matrice completă browser/CDP |
| Rezultatele testelor, diagnosticele și atașamentele păstrează sensul ciclului de viață | [Fațada ascultătorului Core](../atf-core/src/main/java/com/ai/core/reporting/listener/TestListener.java), [ascultătorul de raportare](../atf-reporting/src/main/java/com/ai/core/reporting/listener/ReportingTestListener.java) | [Ciclul de viață la eșec](../atf-tests/src/test/java/com/ai/atf/core/reporting/lifecycle/TestListenerFailureContractTest.java), [prezentarea Allure](../atf-tests/src/test/java/com/ai/atf/core/reporting/adapters/allure/AllurePresentationContractTest.java) | Selecțiile [locală](../atf-tests/src/test/resources/suites/framework-local.xml), [Allure](../atf-tests/src/test/resources/suites/framework-allure.xml) și [browser](../atf-tests/src/test/resources/suites/framework-browser.xml) protejează limitele explicite ale fixture-urilor lor |
| Publicarea agregată păstrează octeții observați ai rapoartelor și câmpuri corecte în manifest | [Generatorul agregat](../atf-reporting/src/main/java/com/ai/core/reporting/aggregate/AggregateExtentReportWriter.java), [fixture-ul publicării manifestului](../atf-tests/src/test/java/com/ai/atf/core/reporting/aggregate/publish/AtfCoreAggregatePublishedManifestContractTest.java) | Contractul de publicare verifică directorul explicit de ieșire, octeții copiați și valorile interpretate ale manifestului | Selecțiile [locală](../atf-tests/src/test/resources/suites/framework-local.xml) și [agregată](../atf-tests/src/test/resources/suites/framework-aggregate.xml); distinge fixture-urile de publicare de inspecția unui agregat generat efectiv |
| Căutarea facturii unui client respinge dovezi absente, fără legătură sau bazate numai pe prefix | [Acțiunile facturilor](../atf-tests/src/test/java/com/ai/ui/support/actions/AccountInvoiceActions.java), [dovada facturii](../atf-tests/src/test/java/com/ai/ui/support/validation/InvoiceEvidence.java) | [InvoiceEvidenceContractTest](../atf-tests/src/test/java/com/ai/atf/ui/component/InvoiceEvidenceContractTest.java), [paritatea locatorilor](../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java) | Verificări cu dubluri ale motorului în selecția [locală](../atf-tests/src/test/resources/suites/framework-local.xml) și fixture-uri de selectori în selecția [browser](../atf-tests/src/test/resources/suites/framework-browser.xml); nu se afirmă un scenariu controlat customer/admin |
| Schimbarea stării de către admin trebuie să corespundă valorii persistate după redeschiderea comenzii | [Acțiunile admin](../atf-tests/src/test/java/com/ai/ui/support/actions/AdminOrderStatusActions.java), [validarea stării](../atf-tests/src/test/java/com/ai/ui/support/validation/AdminOrderStatusValidation.java) | [InvoiceEvidenceContractTest](../atf-tests/src/test/java/com/ai/atf/ui/component/InvoiceEvidenceContractTest.java) acoperă valoarea selectată, eticheta afișată și eșecuri pentru comenzi fără legătură | [Suita locală](../atf-tests/src/test/resources/suites/framework-local.xml): dovezi de stare cu dubluri ale motorului; persistența în aplicația controlată necesită în continuare acceptanță independentă |
| Înregistrarea trimite o singură identitate și păstrează primul eșec al formularului | [Serviciul de înregistrare](../atf-tests/src/test/java/com/ai/ui/support/service/RegistrationService.java), [validarea](../atf-tests/src/test/java/com/ai/ui/support/validation/RegistrationValidation.java) | [Contractul trimiterii](../atf-tests/src/test/java/com/ai/atf/ui/component/RegistrationSubmissionContractTest.java), [contractul validării](../atf-tests/src/test/java/com/ai/atf/ui/component/RegistrationValidationContractTest.java) | [Suita locală](../atf-tests/src/test/resources/suites/framework-local.xml): dubluri și valori controlate; nu demonstrează crearea contului pe server |

<a id="explicit-application-and-environment-boundaries"></a>
## Limitele explicite ale aplicației și mediului

[Testele existente ale zonei autentificate](../atf-tests/src/test/java/com/ai/ui/tests/AccountAuthenticatedAreaTests.java)
și [testele memoriei profilului](../atf-tests/src/test/java/com/ai/ui/tests/ProfileContactAddressMemoryTests.java)
sunt selecții de aplicație opționale. Prezența lor nu oferă un scenariu controlat
complet invoice/admin și nu demonstrează că ipotezele încorporate și pregătirea
contului sunt potrivite unui mediu țintă.

[Registrul de feedback](../docs/feedback/README.md) consemnează guvernanța framework-ului
și verificarea F-008 pentru matricea desktop browser/driver/CDP selectată.
Etapa curentă îmbunătățește framework-ul și verificările sale existente; nu adaugă
scenarii E2E. Fixture-urile locale nu stabilesc acceptanța aplicației.

<a id="maintenance-rule"></a>
## Regula de mentenanță

Când se schimbă comportamentul, responsabilitățile sau selecția suitei, reconciliază
rândul afectat cu sursa live și criteriul de verificare. Păstrează vizibile absența
unui criteriu sau indisponibilitatea mediului. Adaugă o afirmație de execuție numai
în înregistrarea dovezii sale după rularea efectivă a selecției numite; păstrează
harta concentrată pe contractele și limitele curente.
