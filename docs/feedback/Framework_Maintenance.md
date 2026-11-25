<a id="framework-maintenance"></a>
# Mentenanța frameworkului

F-008 consemnează matricea verificată a browserelor desktop. Celelalte secțiuni
păstrează dovezile de configurare, curățare și regresie ale frameworkului în
domeniul lor original. Comenzile curente aparțin [Testării](../Testing.md).

<a id="f-008--selenium-browser-matrix"></a>
## F-008 — matricea browserelor Selenium

- Stare: VERIFIED pentru matricea Windows 11 x64 selectată: Selenium 4.54.0,
  Chrome/ChromeDriver 158.0.8127.88 și CDP tipizat exact v158. Executabilul
  configurat efectiv, capabilitățile și domeniul instanțiat au fost verificate;
  rularea acceptată nu are avertismente privind CDP tipizat absent/aproximat și nu suprimă avertismente.
- Responsabil: [atf-selenium](../../atf-selenium/README.md), mai precis
  [implementarea de rețea](../../atf-selenium/src/main/java/com/ai/ui/core/engine/selenium/network).
  [atf-tests](../../atf-tests/README.md) deține acceptanța între adaptoare.
- Reproducere originală: suita de browser păstrată, cu
  `org.seleniumhq.selenium:selenium-java:4.45.0` și Chrome/ChromeDriver
  `158.0.8127.88` descărcate; rulează `mvn -B -ntp -Pbrowser test` dintr-o
  copie izolată a reactorului, cu binarele selectate.
- Observație originală: Selenium a raportat că nu include o implementare CDP tipizată corespunzătoare.
  Paritatea browserelor a trecut prin comportamentul existent al adaptorului,
  deși avertismentul de aliniere a versiunilor a rămas.
- Așteptat: documentează și verifică o combinație intenționată SDK/browser/driver/CDP,
  inclusiv capabilitatea reală de răspuns de rețea pe care se bazează apelanții.
- Dovezi istorice: framework-browser-02.log în
  ${ATF_WORK_ROOT}/runs/20261124-framework. Binarele și driverele rămân în
  ${ATF_WORK_ROOT}/cache. Contractele existente de
  [capabilitate](../../atf-tests/src/test/java/com/ai/atf/ui/component/UiEngineCapabilityContractTest.java),
  [delegare](../../atf-tests/src/test/java/com/ai/atf/ui/component/UiEngineDelegationContractTest.java)
  și [sursă Selenium](../../atf-tests/src/test/java/com/ai/atf/ui/component/engine/SeleniumEngineInternalContractTest.java)
  nu înlocuiesc o probă reală de răspuns de rețea în browser.
- Dovezi curente: cazul existent de
  [paritate între motoare](../../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java)
  a trecut 2 probe pozitive de răspuns și 4 probe de nepotrivire URL/metodă
  pe serverul său local. Rularea de browser separată acceptată a trecut 120 de
  verificări: 108 în atf-tests și 12 verificări native ale bibliotecilor.
  Verificarea nativă a furnizorului exact și aserțiunile de browser verifică
  matricea selectată prin ruta Manager existentă. Sursa, mediul, comenzile exacte
  și rulările istorice eșuate sunt în
  [F-008 și registrul său comun](Browser_Compatibility_And_Portability.md#f-008).

| Probă de închidere | Observație necesară | Dovezi curente |
|---|---|---|
| Selectarea versiunilor | Înregistrează sistemul, SDK-ul, versiunile reale ale browserului/driverului și disponibilitatea CDP tipizat; păstrează orice avertisment | Matricea Windows selectată, executabilul real și domeniul exact v158 instanțiat au trecut; avertismentul vechi pentru 4.45.0 rămâne dovadă istorică |
| Răspuns real de rețea | Declanșează o cerere locală; verifică metoda, URL-ul, starea și corpul răspunsului observat | 2 probe pozitive trecute, câte una pentru fiecare adaptor, inclusiv corpurile reale ale cererii și răspunsului |
| Rezultat negativ de rețea | Răspunsul absent/nepotrivit eșuează în bugetul de așteptare declarat | 4 probe reale de nepotrivire URL/metodă trecute pentru cele două adaptoare |
| Păstrarea parității adaptoarelor | Suita de browser existentă rămâne verde cu mediul selectat și Node implicit din SDK-ul Playwright | modules-f008-browser-accepted a trecut toate cele 120 de verificări de browser/native; selecțiile locală și agregată au trecut fiecare 514 |

Această închidere acoperă sistemul înregistrat și binarele fixate. O altă
platformă, versiune SDK/browser/driver sau selecție CDP tipizat necesită aceleași
observații noi și acceptanța completă de browser;
[Testarea](../Testing.md#browser-preparation-and-checks) deține pregătirea întreținută.
Declararea unei versiuni sau suprimarea avertismentelor nu este suficientă.
Eșecurile anterioare de curățare modules-phase2-browser-final, dovada de 119
verificări din etapa a doua și eșecul ulterior de curățare modules-f008-browser-final
își păstrează domeniul original în înregistrările legate. Acceptanța locală de
rețea stabilește limita înregistrată a frameworkului; nu pretinde un scenariu de aplicație controlat.

<a id="f-002--reproducible-startup"></a>
## F-002 — pornire reproductibilă

- Stare: VERIFIED pentru corecția istorică anterioară modularizării.
- Reproducere originală: urmează ghidul de pornire din rădăcină într-o copie nouă.
- Observație anterioară: erau referite clase/fișiere de calibrare absente,
  iar descoperirea implicită amesteca verificările locale cu testele site-ului public.
- Corecție livrată: valori locale implicite întreținute, selecții explicite
  de browser/aplicație și suprascriere explicită a fișierului de configurare.
- Verificare istorică: 304 cazuri locale și verificările focalizate de suprascriere
  prin fișier au trecut. Comenzile modulare curente aparțin [Testării](../Testing.md).

<a id="f-003--functional-framework-boundary"></a>
## F-003 — limita funcțională a frameworkului

- Stare: VERIFIED pentru curățarea istorică; implementarea originală este
  păstrată pe `main_mcp`.
- Reproducere originală: inspectează sursele de producție și profilurile de test.
- Observație anterioară: prompturi, infrastructură de generare/promovare și
  artefacte istorice generate erau amestecate în frameworkul runtime.
- Corecție livrată: păstrează execuția UI/API, raportarea runtime și regresiile
  semnificative; elimină componentele runtime retrase și resursele/testele dedicate.
- Verificare istorică: acceptanța sursei, JAR-ului, selecției locale și browserelor
  a trecut. Recuperarea unei reguli de revizuire reutilizabile nu restaurează mecanismul său runtime vechi.

<a id="f-006--regression-setup"></a>
## F-006 — pregătirea regresiilor

- Stare: VERIFIED pentru corecția istorică a regresiilor.
- Reproducere originală: execută suita locală păstrată după retragerea profilurilor vechi.
- Observație anterioară: două regresii Allure reflectau o metodă mutată, o
  verificare de topologie citea o suită retrasă, iar un caz pozitiv de interogare repetată avea un buget real de 50 ms.
- Corecție livrată: exercită fabrica publică de rezultate, elimină verificarea
  de topologie retrasă și păstrează aserțiunile exacte ale furnizorului/încercărilor
  de interogare, cu un caz separat de expirare și un buget de planificare utilizabil.
- Verificare istorică: 304 teste au trecut fără eșecuri/erori/omiteri în copia
  framework-final; local-acceptance.log și local-results/ păstrează dovada.

<a id="historical-correction-evidence"></a>
## Dovezile corecțiilor istorice

Înregistrările din această secțiune preced extragerea modulelor. Păstrează
numerele și proveniența originale; nu sunt instrucțiuni de distribuție curente.

- Bază: toate sursele originale de producție și de test s-au compilat cu succes.
- Corecții inițiale: 12 cazuri locale trecute, 0 eșecuri/erori/omiteri.
- Dovezile istorice de sursă și execuție rămân în istoricul Git și arhiva externă
  runs/20261124-framework. Înregistrările privind îmbunătățirea fluxurilor de
  produs au fost eliminate din această listă de mentenanță a frameworkului.

Reviziile istorice sunt `c590d62` pentru curățare și `661d4dc` pentru profilul
agregat activat. Originalul arhivat este `70a7279` pe main_mcp. Înregistrarea
[M-001](Module_Architecture.md) ulterioară deține dovezile modularizării.

Rădăcina dovezilor: ${ATF_WORK_ROOT}/runs/20261124-framework. Fiecare rând este
o rulare separată; numerele se suprapun și nu trebuie însumate ca teste unice.

| Verificare istorică | Rezultat | Dovadă relativă la rădăcină |
|---|---|---|
| Suita locală implicită | 304 trecute, 0 eșecuri/erori/omiteri | local-acceptance.log și local-results/ |
| Subsetul Allure explicit | 11 trecute, 0 eșecuri/erori/omiteri | framework-allure.log |
| Suita de browser | 100 trecute, 0 eșecuri/erori/omiteri | framework-browser-02.log; XML în copia de validare framework-browser-01 |
| Profilul agregat | 304 trecute; Extent și 305 rezultate Allure JSON trecute, inclusiv sumarul | aggregate-enabled.log și aggregate-enabled-results/ |
| Prezentarea Extent generată | 0 încălcări ale prezentării publicate | aggregate-artifact-audit.log |
| Pachetul | JAR construit; 0 intrări retrase | package.log și package-provenance.json |

Artefactele agregate istorice sunt în
${ATF_WORK_ROOT}/validation/framework-final/target/atf-core-aggregate-report.
Pachetul istoric era `com.ai:test-framework:1.0.0`; calea sa JAR originală,
commitul sursă și SHA-256 rămân în package-provenance.json. Consumul curent
folosește [coordonatele modulare](../../README.md#consumer-migration). Copierea
sau redenumirea acelei dovezi nu implică o versiune nouă de pachet sau o nouă execuție.

Rularea istorică de browser a folosit Node implicit din SDK. Rularea selectorului
a setat PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 după pregătirea browserului.
Avertismentul instalatorului și cel CDP rămân parte a acelei dovezi;
niciunul nu a fost eliminat tacit de această revizuire a documentației.
