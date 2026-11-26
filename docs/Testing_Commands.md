<a id="framework-testing-command-cookbook"></a>
# Colecția comenzilor de testare a framework-ului

Alege limitele dovezii în [Testare](Testing.md) înaintea unei rețete.
Fiecare rețetă de mai jos este pliată implicit. Extinde numai comenzile relevante
pentru contractul schimbat; o comandă afișată aici nu este o dovadă de execuție.

<a id="run-location-and-scope"></a>
## Locația și domeniul rulării

Rulează fiecare comandă Maven de mai jos din rădăcina reactorului unei copii
temporare de validare, conform [Dezvoltare](Development.md). Nu compila în
checkout-ul de dezvoltare sau live. Păstrează cache-urile Maven/browser și dovezile
fiecărei rulări în afara acestor checkout-uri sursă.

<a id="local-baseline"></a>
## Baza locală

<details>
<summary>Rulează selecția implicită locală și de guvernanță</summary>

```powershell
mvn -B -ntp test
```

</details>

Comanda din rădăcină rulează verificările native ale bibliotecilor și
[suita locală a framework-ului](../atf-tests/src/test/resources/suites/framework-local.xml)
în atf-tests, inclusiv guvernanța. Regresiile locale folosesc fixture-uri și dubluri
pentru configurație, execuție API, delegare UI și raportare. Exclud cazurile dependente
de browser, fixture-urile cu eșec intenționat și scenariile live de aplicație.
O compilare reușită sau o suită omisă nu reprezintă dovezi de execuție.

<a id="selective-checks"></a>
## Verificări selective

Folosește `-pl atf-tests -am` pentru a compila modulul testelor și bibliotecile sale
dependente. Căile suitelor de mai jos sunt relative la modulul atf-tests:

<details>
<summary>Rulează un subset UI/API existent sau o selecție de contracte numite</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-ui.xml" test
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-api.xml" test
mvn -B -ntp -pl atf-tests -am "-Dtest=InvoiceEvidenceContractTest,RegistrationSubmissionContractTest,ConfigManagerFileContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

</details>

Opțiunea filtrului de clasă permite modulelor din amonte să continue când clasa
cerută există numai în atf-tests. Verifică dacă testele selectate din aval chiar
s-au executat. Folosește suita [API](../atf-tests/src/test/resources/suites/framework-api.xml),
[UI](../atf-tests/src/test/resources/suites/framework-ui.xml) sau
[raportare](../atf-tests/src/test/resources/suites/framework-reporting.xml)
pentru un subset menținut. Subseturile nu înlocuiesc rularea completă implicită de guvernanță.

Subsetul API selectează BaseTestAPIContractTest și
ApiCoreExecutionArchitectureContractTest. Verifică inițializarea framework-ului,
pregătirea scrierii, bugetele reîncercărilor, ordinea apelurilor și propagarea eșecului
cu fixture-uri și dubluri locale. Nu apelează serviciul GoRest. Clasa existentă
[PositiveUserTests](../atf-tests/src/test/java/com/ai/api/tests/PositiveUserTests.java)
conține două scenarii opționale de scriere GoRest; ambele necesită auth.token.
Un token gol determină omiterea lor în inițializarea comună, ceea ce nu reprezintă
acceptanță API a aplicației trecută. Consemnează separat aceste două domenii de execuție.

Verificările native ale unei biblioteci pot fi selectate prin modul și dependențe,
de exemplu `mvn -B -ntp -pl atf-core -am test`. Selecția nu rulează regresiile
între module din atf-tests.

Această selecție de componentă injectează eșecuri ale colaboratorilor și nu
pornește browsere. Păstrează eroarea originală și observă independent starea
curățării. Rulează și selecția locală completă înaintea închiderii feedback-ului.

<details>
<summary>Verifică contractele existente de ciclu de viață UI, configurație și curățare a adaptoarelor</summary>

```powershell
mvn -B -ntp "-Dsurefire.failIfNoSpecifiedTests=false" "-Dtest=BaseTestUIContractTest,UiEngineRuntimeOptionsContractTest,WaitUtilsContractTest" test
mvn -B -ntp -pl atf-playwright,atf-selenium -am test
```

</details>

<a id="browser-preparation-and-checks"></a>
## Pregătirea și verificarea browserelor

Folosește variabilele portabile pentru rădăcina de lucru/cache din
[Dezvoltare](Development.md#working-roles), apoi intră în rădăcina reactorului copiei
de validare. Separă obținerea browserelor de dovezile de acceptanță.

<a id="playwright-runtime"></a>
### Mediul de execuție Playwright

Instalează revizia Chromium selectată de SDK-ul Java Playwright fixat.
SDK-ul folosește propriul mediu Node inclus. Pregătește artefactele Maven locale
înainte de invocarea CLI-ului adaptorului:

<details>
<summary>Pregătește mediul de browser Playwright fixat</summary>

```powershell
$env:PLAYWRIGHT_BROWSERS_PATH = Join-Path $workRoot 'cache/playwright'
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -pl atf-playwright -am "-DskipTests" install
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -f atf-playwright/pom.xml org.codehaus.mojo:exec-maven-plugin:3.5.0:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = '1'
```

</details>

Comanda de pregătire omite execuția și nu este dovadă de acceptanță.
Opțiunea de omitere Playwright afectează descărcările inițiale, nu acțiunile browserului.

<a id="selenium-pinned-manager-route"></a>
### Calea Selenium Manager cu versiuni fixate

Folosește versiunile selectate de [POM-ul părinte](../pom.xml) și contractul browserului:

| Componentă | Versiune selectată |
|---|---|
| Selenium Java | 4.54.0 |
| Chrome for Testing | 158.0.8127.88 |
| ChromeDriver | 158.0.8127.88 |
| CDP tipizat obligatoriu | 158 |

[Versiunea Selenium](https://www.selenium.dev/downloads/) oferă SDK-ul Java;
[manifestul Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/158.0.8127.88.json)
oferă descărcările browserului/driverului pentru fiecare platformă. Obține binarul
Chrome exact pentru sistemul gazdă și setează ATF_CHROME_BINARY oferit de apelant
către acel executabil existent. Pregătirea următoare rezolvă intrarea fără să
presupună o cale de stație de lucru sau o structură de cache a sistemului de operare.

Fabrica existentă acordă prioritate unui webdriver.chrome.driver explicit sau
unui driver din cache-ul ATF față de Selenium Manager. Lasă proprietatea JVM
nesetată, inclusiv în opțiunile JVM moștenite, și folosește un cache alternativ
ATF gol sau inexistent. Cache-ul separat Selenium Manager ține resursele descărcate.
Configurația lui trebuie să respecte versiunile fixate; un se-config.toml conflictual
poate suprascrie setările de mediu, conform [configurației Selenium Manager](https://www.selenium.dev/documentation/selenium_manager/#configuration).

<details>
<summary>Pregătește Selenium Manager și rulează clasa existentă de paritate locatori/rețea</summary>

```powershell
$seleniumCache = Join-Path $workRoot 'cache/selenium'
$fallbackCache = Join-Path $workRoot 'cache/selenium-fallback-empty'
if (Test-Path -LiteralPath $fallbackCache) {
    if (@(Get-ChildItem -LiteralPath $fallbackCache -Force).Count -gt 0) {
        throw 'Use an empty ATF fallback cache to exercise Selenium Manager.'
    }
}
$env:SE_BROWSER_PATH = (Resolve-Path -LiteralPath $env:ATF_CHROME_BINARY -ErrorAction Stop).Path
$env:SE_DRIVER_VERSION = '158.0.8127.88'
$env:SE_CACHE_PATH = $seleniumCache
$env:SE_SKIP_DRIVER_IN_PATH = 'true'
$env:SE_SKIP_BROWSER_IN_PATH = 'true'
$env:SE_AVOID_STATS = 'true'
Remove-Item Env:SE_OFFLINE -ErrorAction SilentlyContinue
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -pl atf-tests -am "-Datf.selenium.cache.root=$fallbackCache" "-Dtest=SeleniumPlaywrightLocatorParityContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

</details>

Clasa existentă de paritate rulează ambele adaptoare; ui.engine nu restrânge
furnizorii săi de date la un singur motor. Cu descărcări permise, invocarea pregătește
calea reală Manager și execută contractele locale de locatori/rețea. Păstrează
dovezile reale de browser, driver, binar selectat și CDP tipizat. Nu stabilește un
scenariu de aplicație și nu înlocuiește selecția completă de browser.

<a id="complete-browser-selection"></a>
### Selecția completă de browser

După pregătirea dependențelor Maven și a ambelor medii de browser, păstrează același
mediu și argumentul cache-ului alternativ pentru acceptanța offline:

<details>
<summary>Rulează selecția completă de browser cu cache-uri offline pregătite</summary>

```powershell
$env:SE_OFFLINE = 'true'
mvn -B -ntp -o "-Dmaven.repo.local=$mavenCache" "-Datf.selenium.cache.root=$fallbackCache" -Pbrowser test
```

</details>

SE_OFFLINE controlează accesul la rețea al Selenium Manager; `-o` Maven controlează
rezolvarea artefactelor Maven. Activează-le numai după punerea în cache a resurselor
necesare. Profilul browser selectează
[framework-browser.xml](../atf-tests/src/test/resources/suites/framework-browser.xml)
numai în atf-tests; verificările native ale bibliotecilor rămân parte a rulării
reactorului. Suita folosește pagini locale pentru paritatea motoarelor, capturi,
comportamentul ferestrei/zoomului și interacțiunile raportării.

Păstrează capturile complete când inspectezi dovezile browserului. O schimbare de
versiune necesită o nouă verificare a matricei și o rulare de browser, cu rezultate
consemnate în feedback.

<a id="reporting-acceptance"></a>
## Acceptanța raportării

Subsetul Allure nativ și publicarea agregată sunt explicite:

<details>
<summary>Rulează verificările Allure native și acceptanța raportării agregate</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-allure.xml" "-Dreporting.engine=allure" test
mvn -B -ntp -Paggregate test
```

</details>

[Suita Allure](../atf-tests/src/test/resources/suites/framework-allure.xml)
verifică integrarea nativă.
[Suita agregată](../atf-tests/src/test/resources/suites/framework-aggregate.xml)
colectează cazuri executate și publică dovezi Extent și Allure. Inspectează
artefactele generate împreună cu rezultatul TestNG: verifică numărătorile, stările,
temporizarea, atașamentele și căile declarate ale rezultatelor.

<a id="application-journeys"></a>
## Scenarii de aplicație

Suportul produsului și clasele opționale de aplicație se află în atf-tests.
Configurează `base.url` și `base.url.api` pentru mediul țintă. Scrierile API cer
`auth.token` explicit. Selectează o clasă concretă cu `-Dtest` și aceeași opțiune
de permitere a modulelor fără potrivire din amonte arătată mai sus.

Aceste scenarii pot crea utilizatori, comenzi sau facturi. O acreditare absentă,
un serviciu indisponibil sau un scenariu neexecutat nu stabilesc acceptanța
aplicației. Lacunele active de acceptanță rămân în
[feedback](feedback/README.md).

<a id="existing-product-e2e-audit"></a>
<a id="retained-product-ui-audit"></a>
## Auditul UI de produs păstrat

Verificările existente de înregistrare, zonă autentificată și profil folosesc
aceleași metode de test pentru ambii furnizori. Sunt distincte de framework-ui
și framework-browser, care selectează contracte de framework și fixture-uri de
browser. Nicio suită menținută de framework nu selectează tacit aplicația publică.

| Responsabil existent | Metode | Contract observat |
|---|---:|---|
| [RegistrationTests](../atf-tests/src/test/java/com/ai/atf/ui/RegistrationTests.java) | 6 | Înregistrare reușită și respingerea câmpurilor obligatorii, emailului, duplicatelor, parolei și telefonului |
| [AccountAuthenticatedAreaTests](../atf-tests/src/test/java/com/ai/ui/tests/AccountAuthenticatedAreaTests.java) | 5 | Navigare autentificată în cont, profil, favorite, facturi și mesaje |
| [ProfileContactAddressMemoryTests](../atf-tests/src/test/java/com/ai/ui/tests/ProfileContactAddressMemoryTests.java) | 5 | Valorile înregistrate de contact și adresă rămân observabile în profil |

Folosește o copie temporară de validare și un istoric separat pentru fiecare
comandă. Pregătește binarele browserelor conform secțiunilor de mai sus. URL-ul
explicit de mai jos este demo-ul de testare configurat; înlocuiește-l cu ținta dorită
când este cazul. Metodele păstrate creează clienți sintetici. Nu creează facturi,
nu schimbă comenzile admin și nu șterg acești clienți. Închiderea browserului
închide sesiunile; nu curăță datele aplicației.

<details>
<summary>Rulează aceleași metode UI de produs păstrate prin fiecare furnizor de browser</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Dtest=RegistrationTests,AccountAuthenticatedAreaTests,ProfileContactAddressMemoryTests" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dui.engine=playwright" "-Dbase.url=https://practicesoftwaretesting.com" "-Dreporting.engine=extent" "-Dreports.timestamp.enabled=false" "-Dui.screenshots.enabled=true" "-Dui.screenshots.on.failure.only=false" "-Dui.screenshots.capture.final.state=true" test
mvn -B -ntp -pl atf-tests -am "-Dtest=RegistrationTests,AccountAuthenticatedAreaTests,ProfileContactAddressMemoryTests" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dui.engine=selenium" "-Dbase.url=https://practicesoftwaretesting.com" "-Dreporting.engine=extent" "-Dreports.timestamp.enabled=false" "-Dui.screenshots.enabled=true" "-Dui.screenshots.on.failure.only=false" "-Dui.screenshots.capture.final.state=true" test
```

</details>

Pentru fiecare motor, păstrează XML-ul Surefire original, hash-urile sursei, comanda,
URL-ul, identitatea browserului și ExtentReport.html. Reconciliază subiecții exacți
clasă/metodă și stările cu nodurile raportului. PASS al acțiunii stabilește finalizarea
acțiunii; pasul separat al validării trebuie să declare observații așteptate/reale
înainte de PASS de afaceri. Păstrează FAIL și SKIP și distinge validarea absentă sau
neatinsă de un scenariu negativ reușit.

[Principiile vii](Living_Architecture_UI_API_doc_v1_0.md#retained-principles)
definesc responsabilitățile sursei.
[Contractul publicării rapoartelor](../artifacts/reports/README.md#retained-ui-report-publication)
definește cititorul fiecărui motor și limitele dovezii. Consistența auditului nu
stabilește execuția unui test, curățarea datelor aplicației sau acoperire exhaustivă.

<details>
<summary>Rulează controalele de mentenanță ale auditului UI păstrat</summary>

```powershell
node --test tools/verify-ui-e2e-audit.test.mjs tools/collect-artifacts.test.mjs
```

</details>

Comanda de mai sus execută controalele de mentenanță. Pentru reverificarea unui
cititor nativ deja publicat fără lansarea browserelor sau schimbarea dovezilor,
selectează directorul său existent cu patru fișiere din rădăcina reactorului:

<details>
<summary>Reverifică un cititor UI nativ deja publicat fără lansarea browserului</summary>

```powershell
$engine = 'playwright'
$auditRoot = Join-Path (Get-Location).Path "artifacts/reports/current/e2e/$engine"
node tools/verify-ui-e2e-audit.mjs --source "$auditRoot/source.json" --xml "$auditRoot/native.xml" --html "$auditRoot/ExtentReport.html" --capture "$auditRoot/audit.json"
```

</details>

Alege selenium pentru celălalt motor publicat. Aceste fișiere sunt precondiții ale
publicării și lipsesc într-un checkout numai cu surse. audit.json este captura
DOM nativă păstrată, nu rezultatul consistenței afișat de această comandă.
Verificatorul citește fișierele originale și scrie verdictul la ieșirea standard;
păstrează o înregistrare nouă în afara sursei când consemnezi reverificarea.
Un rezultat de consistență PASSED păstrează rezultatele native originale PASS, FAIL și SKIP.

<a id="explicit-configuration"></a>
## Configurație explicită

[ConfigManager](../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java)
încarcă implicit resursa framework.properties din classpath. Un fișier explicit
înlocuiește acea resursă; trebuie să ofere setările consumatorului selectat.
Proprietățile JVM individuale au în continuare prioritate.

Pentru un fișier al consumatorului la config/local.properties în copia de validare
sau un fișier existent selectat prin ATF_CONFIG_FILE, rezolvă calea o singură dată
la rădăcina reactorului înainte ca Maven să intre în module:

<details>
<summary>Rezolvă configurația explicită a consumatorului înainte ca Maven să intre în module</summary>

```powershell
$projectRoot = (Get-Location).Path
$configInput = if ($env:ATF_CONFIG_FILE) {
    $env:ATF_CONFIG_FILE
} else {
    Join-Path $projectRoot 'config/local.properties'
}
$configPath = (Resolve-Path -LiteralPath $configInput -ErrorAction Stop).Path
mvn -B -ntp "-Dframework.properties=$configPath" test
```

</details>

Fișierul este o precondiție explicită a consumatorului, nu un fișier inclus în depozit.
Rezolvarea eșuează înainte de Maven dacă nu există. Transmiterea căii rezolvate evită
presupunerea că fiecare proces separat de test are același director curent.

<a id="output-and-evidence"></a>
## Rezultate și dovezi

[Arhiva artefactelor](../artifacts/reports/README.md) definește clasificarea rapoartelor,
integritatea copiilor și verificările explicite ale instrumentelor de mentenanță.
Rulează selecția sa Node când schimbi colectorul; guvernanța Java nu execută acel CLI.

Suprascrierea `atf.build.directory` mută rezultatele compilării Maven numai pentru
atf-tests. `surefire.reportsDirectory` selectează rezultatele execuției acelui modul.
Directoarele target ale bibliotecilor din amonte și destinațiile rapoartelor de
execuție sunt configurate separat; aceste proprietăți nu mută fiecare artefact al reactorului.

În configurația implicită, căile sunt relative la copia de validare:

| Rezultat | Locație |
|---|---|
| Rezultate Surefire/TestNG | target/surefire-reports al fiecărui modul testat |
| Rezultate între module | atf-tests/target/surefire-reports |
| Rapoarte Extent | atf-tests/target/reports |
| Rezultate brute Allure | atf-tests/target/allure-results |
| Publicare agregată | atf-tests/target/atf-core-aggregate-report |

Directorul agregat păstrează numele de compatibilitate. Proprietățile de execuție
pot redirecționa rapoartele. Păstrează o copie nouă sau directoare unice de rezultate
pentru fiecare rulare de acceptanță, ca o rerulare focalizată să nu suprascrie XML-ul anterior.

Consemnează în feedback punctul sursei, comanda exactă, suita selectată, numărătorile
și căile externe ale artefactelor. Distinge dovezile bazei istorice, verificările
nou executate, cazurile omise și verificările neexecutate. Un raport copiat sau un
director redenumit nu reprezintă o rulare nouă.
