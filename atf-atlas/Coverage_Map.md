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
| Rezultatele testelor, diagnosticele și atașamentele păstrează sensul ciclului de viață | [Fațada ascultătorului Core](../atf-core/src/main/java/com/ai/core/reporting/listener/TestListener.java), [ascultătorul de raportare](../atf-reporting/src/main/java/com/ai/core/reporting/listener/ReportingTestListener.java) | [Ciclul de viață la eșec](../atf-tests/src/test/java/com/ai/atf/core/reporting/lifecycle/TestListenerFailureContractTest.java), [Prezentarea Extent](../atf-tests/src/test/java/com/ai/atf/core/reporting/adapters/ExtentAdapterTest.java) | Selecțiile [locală](../atf-tests/src/test/resources/suites/framework-local.xml), [Raportare](../atf-tests/src/test/resources/suites/framework-reporting.xml) și [browser](../atf-tests/src/test/resources/suites/framework-browser.xml) protejează limitele explicite ale fixture-urilor lor |
| Publicarea agregată păstrează octeții observați ai rapoartelor și câmpuri corecte în manifest | [Generatorul agregat](../atf-reporting/src/main/java/com/ai/core/reporting/aggregate/AggregateExtentReportWriter.java), [fixture-ul publicării manifestului](../atf-tests/src/test/java/com/ai/atf/core/reporting/aggregate/publish/AtfCoreAggregatePublishedManifestContractTest.java) | Contractul de publicare verifică directorul explicit de ieșire, octeții copiați și valorile interpretate ale manifestului | Selecțiile [locală](../atf-tests/src/test/resources/suites/framework-local.xml) și [agregată](../atf-tests/src/test/resources/suites/framework-aggregate.xml); distinge fixture-urile de publicare de inspecția unui agregat generat efectiv |
| Căutarea facturii unui client respinge dovezi absente, fără legătură sau bazate numai pe prefix | [Acțiunile facturilor](../atf-tests/src/test/java/com/ai/ui/support/actions/AccountInvoiceActions.java), [dovada facturii](../atf-tests/src/test/java/com/ai/ui/support/validation/InvoiceEvidence.java) | [InvoiceEvidenceContractTest](../atf-tests/src/test/java/com/ai/atf/ui/component/InvoiceEvidenceContractTest.java), [paritatea locatorilor](../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java) | [locală](../atf-tests/src/test/resources/suites/framework-local.xml) cu dubluri ale motorului; [browser](../atf-tests/src/test/resources/suites/framework-browser.xml) cu selectori; fără acceptanță controlată customer/admin |
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

<a id="reviewed-semantic-boundaries"></a>
## Limite semantice revizuite

Aceste observații din surse rafinează harta existentă; nu adaugă un flux de teste.

| Limită | Comportament observat | Responsabilitate rămasă |
|---|---|---|
| [Sursa configurației](../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java) | Suprascrierile apelantului preced setările încărcate; sursa explicită absentă produce eșec | Proprietățile încărcate sunt memorate; modificarea fișierului nu le reîncarcă automat |
| [Pregătirea scrierii](../atf-core/src/main/java/com/ai/api/core/BaseTestAPI.java) | Accesul local gol face fluxul de scriere indisponibil | Accesul negol nu demonstrează autorizarea la distanță sau scrierea reușită |
| [Așteptare delimitată](../atf-core/src/main/java/com/ai/ui/support/wait/WaitUtils.java) | Verificările existente compară prima potrivire, ultima observație nepotrivită la expirare și varianta cu aserțiune | Ultima valoare returnată nu este acceptanță; erorile și blocarea furnizorului rămân separate |
| [Politica imaginii finale](../atf-core/src/main/java/com/ai/ui/core/reporting/ScreenshotManager.java) | Contractele existente disting eligibilitatea la succes/eșec, UI absent, deduplicarea și responsabilitatea invocării | Politica și conținutul capturii nu certifică pixelii; două sarcini nu stabilesc orice intercalare |
| [Raportare opțională](../atf-core/src/main/java/com/ai/core/reporting/spi/ReportingBackendLoader.java) | Contractele existente disting funcționarea doar cu Core, un furnizor instalat și refuzul duplicatelor înaintea construirii | Fixture-urile de descoperire nu dovedesc orice instalare; jurnalizarea cerută explicit are cerința proprie |
| [Achiziția browserului](../atf-playwright/src/main/java/com/ai/ui/core/engine/playwright/PlaywrightBrowserSession.java) | Sesiunea Playwright inspectată selectează Chromium; Selenium folosește fabrica sa Chrome | Capabilitatea bibliotecii nu demonstrează toate combinațiile de browser și sistem de operare |
| [Valorile agregării](../atf-reporting/src/main/java/com/ai/core/reporting/aggregate/model/AggregateSummary.java) | Constructorii selectați resping valori negative; agregarea poate deriva numere din cazuri observate | Constructorul public al sumarului nu impune singur toate relațiile dintre totalurile apelantului |
| [Compatibilitatea raportării](../atf-reporting/src/main/java/com/ai/core/reporting/ReportingSession.java) | Comutatorul compatibil de oglindire folosește politica sa existentă de interpretare | Nu susține că toate opțiunile raportării folosesc cititorul boolean comun strict |

[Profilul semantic](Report_Contract.md#semantic-review-and-compact-identity)
cere observații cu domeniu precis și criterii independente existente. O fațetă
REVIEWED încheie domeniul declarat; întrebările rămase continuă să se aplice.

Reviziile succesive păstrează limitele lor inițiale în [dosarul de aprofundare](Report_Contract.md#partial-facet-refinement). Verificările de configurare disting comenzile SDK de aspectul real al ferestrei; agregarea distinge totalurile calculate de valorile manuale. Necunoscutele despre pixeli, rețea și curățarea datelor rămân vizibile.

<a id="successive-facet-review"></a>
## Revizia succesivă a fațetelor

Rezultatul curent păstrează limitele inițiale: 40 REVIEWED / 2 PARTIAL.
Revizia completă privește domeniul declarat, nu orice comportament posibil.

| Dimensiune aprofundată | Observație și limită |
|---|---|
| Configurație Playwright | Opt combinații inspectează profilul, viewportul și închiderea; aspectul fizic nu este certificat |
| Configurație Selenium | Patru combinații inspectează opțiunile și comenzile ferestrei; cache-urile și sistemele complete rămân separate |
| Date de raportare | Totalurile derivate urmează rezultatele; câmpurile manuale nenegative pot fi neconservante |
| Configurație de raportare | Compatibilitatea și cache-ul au politici explicite; nu se deduce acceptanța oricărei destinații |
| Selecția testelor | Catalogul local este separat de selecțiile opționale; acreditările și aplicația rămân externe |
| Configurație Atlas | Politica obligatorie și intrările explicite sunt verificate; regăsirea semantică completă nu este revendicată |
| Date Playwright | Componentele păstrează câmpurile observate, previzualizările și octeții imaginii; sensul pixelilor rămâne separat |
| Date Selenium | Identitățile și conținutul sunt verificate; citirea Chrome live și ordinea evenimentelor rămân separate |

| Dimensiune păstrată PARTIAL | Motiv și următorul prag de dovadă |
|---|---|
| Comportamentul aplicației | Componentele locale explică validările; nu stabilesc persistența completă în aplicație |
| Datele aplicației | Resursele plate și valorile aleatorii sunt explicate; unicitatea globală și ștergerea datelor nu sunt garantate |

<a id="local-evidence-for-retained-boundaries"></a>
## Dovezi locale pentru limitele păstrate

Cele două fațete ale aplicației rămân PARTIAL. Verificările existente urmăresc acum câmpurile complete ale ambelor fabrici până la singura trimitere respinsă, forma credențialelor generate și independența copiei de date. Respingerea valorii vechi a stării și a facturii fără legătură verifică mesajul așteptat, astfel încât altă eroare să nu poată produce un fals rezultat pozitiv.

Întrebările și dovezile lipsă sunt păstrate conform [contractului reviziilor succesive](Report_Contract.md#successive-application-boundaries). Nu se deduc persistență, unicitate globală, interpretare generală a formatelor sau ștergere a datelor aplicației.

<a id="deeper-project-understanding-review"></a>
## Revizie aprofundată a înțelegerii proiectului

Rândurile despre așteptare, imaginea finală și raportarea opțională expun decizii
existente importante, omise din cititorul selectat anterior. Revizuiește starea
comparată concretă conform [profilelor curente](Report_Contract.md#current-source-bound-authoring-profiles);
nu deduce amploarea din fișiere capturate sau din creșterea numărului de fluxuri.

Hook-urile pentru atașarea la eșec și cele pentru starea finală au politici diferite
de propagare. Dispecerizarea inspectată a stării finale este directă; testele cu
handleri sănătoși nu stabilesc că orice eroare de hook sau logger nu blochează
procesul. Păstrează această limită numai de sursă separat de componentele protejate
pentru atașarea la eșec.

Revizia între proiecte distinge și implementarea locală de contractele selectate
ale producătorului. Verificările consumatorului nu certifică sursa absentă a
producătorului, iar componentele selectate care trec nu înlocuiesc o poartă
completă eșuată. Comparația datată și rezultatele originale aparțin feedback-ului
și dosarului extern de revizie, nu unei afirmații permanente despre acoperire.

<a id="reference-presentation-governance"></a>
## Guvernanța prezentării de referință

[Componentele prezentării](../tools/atlas-reference.test.mjs) protejează fațetele și oracolele reale ale fiecărui obiect, limitele de maturitate și notele explicate. [Calibrarea nativă](../tools/atlas-calibration.mjs) verifică starea inițială înainte de interacțiuni și compară harta, dovezile și justificările cu analiza originală. Aceste controale nu introduc comportamente de produs sau teste E2E.
