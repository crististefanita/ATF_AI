<a id="browser-compatibility-and-portable-project-guidance"></a>
# Compatibilitatea browserelor și ghiduri portabile pentru proiect

Referință inițială: `805d58e7670ff1e4fa16d53c56fc0827f7cfd3cb`.
Notele urmează grila separată de prioritate/calitate din
[registrul de feedback](README.md#assessment-rubric-and-evidence-locations).
Notele inițiale ale componentelor și dovezile rămân acolo după închidere.

<a id="shared-acceptance-ledger"></a>
## Registru comun de acceptanță

Reper de implementare: `6a88fc9536625ee019f3010607c9a9b1ba256923`.
Fișierul extern `${ATF_WORK_ROOT}/runs/<stem>-source.json` al fiecărei rulări păstrează argumentele
exacte, intrările de mediu, hash-urile binarelor și manifestul complet al hash-urilor
fișierelor testate. Câmpul său sourceCommit înregistrează baza `805d58e`, înainte de comiterea
conținutului de lucru; nu este identitatea finală a implementării. Aceste trei
fișiere de închidere a feedback-ului au fost actualizate după execuție.

Toate cele trei copii acceptate au rulat sub
`${ATF_WORK_ROOT}/validation/portable workspace/<stem>`. Prefixul exact comun al argumentelor
Maven, cu rădăcina externă normalizată, este
`mvn -B -ntp -Dmaven.repo.local=${ATF_WORK_ROOT}/cache/m2`:

| Identificator de bază al rulării | Argumente rămase | Rezultat |
|---|---|---|
| modules-f008-local-accepted | `-o verify` | 514 trecute: 502 atf-tests și 12 verificări native; fără eșecuri/erori/omiteri |
| modules-f008-aggregate-final | `-o -Paggregate test` | 514 trecute: 502 atf-tests și 12 verificări native; fără eșecuri/erori/omiteri |
| modules-f008-browser-accepted | `-o -Pbrowser test` | 120 trecute: 108 din suita de browser și 12 verificări native; fără eșecuri/erori/omiteri |

Jurnalele folosesc aceiași identificatori sub `${ATF_WORK_ROOT}/runs`; XML-ul rămâne în directoarele
target/surefire-reports ale modulelor fiecărei copii de validare. Numerele se suprapun și
nu trebuie însumate ca teste unice. Auditurile de publicare și ale JAR-urilor împachetate sunt
verificări separate și nu sunt afirmate de acest registru.

Lansatorul a adăugat proprietățile java.io.tmpdir și atf.selenium.cache.root, între ghilimele,
prin JAVA_TOOL_OPTIONS. TEMP/TMP au folosit directorul .tmp al rulării; rădăcina ATF de rezervă
a fost `${ATF_WORK_ROOT}/cache/selenium-f008-fallback-empty`, fără un driver
în cache. Nicio suprascriere webdriver.chrome.driver nu a ocolit Manager. Mediul
fixat a folosit:

```text
SE_BROWSER_PATH=${ATF_WORK_ROOT}/cache/selenium/chrome/win64/158.0.8127.88/chrome.exe
SE_DRIVER_VERSION=158.0.8127.88
SE_CACHE_PATH=${ATF_WORK_ROOT}/cache/selenium
SE_OFFLINE=true
SE_SKIP_DRIVER_IN_PATH=true
SE_SKIP_BROWSER_IN_PATH=true
SE_AVOID_STATS=true
PLAYWRIGHT_BROWSERS_PATH=${ATF_WORK_ROOT}/cache/playwright
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
```

Gazdă: Windows 11 x64, NT 10.0.26300; Temurin JDK 21.0.12.1+1 și Maven 3.10.
Compilarea bibliotecilor vizează Java 11. SDK-ul Playwright a folosit Node inclus.
Pregătirea și execuția omisă nu sunt înregistrări de acceptanță.

| Jurnal de eșec păstrat | Observație originală |
|---|---|
| modules-f008-governance-probe.log | 173 de cazuri de guvernanță, 3 eșecuri: schema antetului de notare, un fals pozitiv XPath și o constatare într-un fixture negativ de portabilitate |
| modules-f008-local-final.log | 514 verificări incluzând 12 verificări native, 1 fixture UNC de portabilitate eșuat |
| modules-f008-browser-final.log | 120 de verificări incluzând 12 verificări native, 1 invocare de rețea eșuată, întreruptă în curățarea DriverService.stop după 37.17 secunde, față de limita anterioară de 30 de secunde a metodei |

Aceste eșecuri rămân dovezi istorice și nu sunt rulări acceptate.

<a id="closure-assessments"></a>
## Evaluări de închidere

Prioritatea/calitatea inițiale și componentele lor originale rămân în registru.
Prioritatea de mai jos păstrează urgența remedierii la acea referință inițială; nu este o nouă
prioritate curentă atribuită după închidere. Calitatea la închidere reevaluează numai
domeniul și dovezile documentate, folosind grila existentă.

| ID | Prioritate / calitate inițiale | Calitate la închidere | Corectitudine / verificare / claritate | Domeniu evaluat |
|---|---|---:|---|---|
| F-008 | 81 / 72 | 94 | 95 / 95 / 90 | Matricea Windows efectiv selectată și contractele locale de răspuns; alte matrice necesită dovezi noi |
| G-005 | 82 / 57 | 95 | 95 / 95 / 95 | Reguli delimitate pentru căile redactate și execuție Windows relocată |
| D-001 | 55 / 80 | 94 | 95 / 90 / 95 | Navigarea curentă către responsabili, exemplele publice și randorul adaptiv inspectat |
| T-004 | 70 / 72 | 95 | 95 / 95 / 95 | Șase corpuri de test de rețea observate și căi de curățare cu termene-limită independente |

<a id="f-008"></a>
<a id="f-008-align-the-maintained-browser-matrix"></a>
## F-008: Alinierea matricei de browsere întreținute

- Responsabil: gestionarea dependențelor atf-selenium și contractele native/de browser existente.
- Observație originală: Selenium 4.45.0 includea CDP tipizat 147–149, în timp ce
  Chrome și ChromeDriver selectate erau 158.0.8127.88. Sondele reale de răspuns
  treceau, dar rămânea o avertizare de CDP tipizat aproximativ/lipsă.
- Matrice selectată: Selenium 4.54.0, Chrome for Testing 158.0.8127.88,
  ChromeDriver 158.0.8127.88 și CDP tipizat exact v158 pe gazda înregistrată mai sus.
- Limita implementării: schimbarea proprietății existente a versiunii Selenium și
  întărirea testelor existente. Păstrarea claselor de execuție și a comportamentului
  de rezervă al driverului. Configurarea întreținută folosește setările de mediu Selenium Manager
  existente și rădăcina de cache de rezervă ATF configurabilă existentă.
- Rezultat așteptat: capabilitățile efective browser/driver WebDriver, furnizorul tipizat
  selectat, produsul CDP și executabilul lansat corespund acestei matrice. Sondele reale
  pozitive și cele de nepotrivire URL/metodă trec pe ambele adaptoare.
  Nicio avertizare de furnizor aproximativ nu este suprimată sau acceptată drept potrivire exactă.
- Acceptanță: rerularea testelor native/de rețea delimitate, a verificării locale complete,
  a profilului complet de browser și a publicării agregate din copii temporare
  independente. Înregistrarea sistemului de operare, JDK, Maven, binarelor exacte, selecției dependențelor și dovezilor.
  [Pregătirea canonică](../Testing.md#browser-preparation-and-checks)
  trebuie să fixeze executabilul, precum și versiunea driverului; un Chrome instalat
  care se întâmplă să aibă aceeași versiune nu este o dovadă echivalentă de selecție a binarului.
- Livrat:
  [verificarea furnizorului nativ](../../atf-selenium/src/test/java/com/ai/ui/core/engine/selenium/SeleniumProviderTest.java)
  existentă selectează exact v158CdpInfo. [Fixture-ul de browser](../../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java)
  existent verifică capabilitățile efective browser/driver, produsul CDP,
  v158Domains instanțiat și binarul lansat față de SE_BROWSER_PATH. Ambele sonde
  pozitive și toate cele patru sonde de nepotrivire URL/metodă au trecut în acceptanța completă de browser.
  Nu a apărut nicio avertizare de CDP tipizat lipsă/aproximativ și niciuna nu a fost suprimată.
- Stare: VERIFIED pentru această matrice Windows fixată, prin execuțiile acceptate
  de mai sus. Această etapă schimbă zero fișiere Java de producție; corecția dependenței
  este proprietatea existentă a versiunii Selenium. O altă platformă sau versiune necesită
  o nouă acceptanță a matricei și browserului, nu extrapolare din acest rezultat.

Referințe primare care susțin matricea și configurarea selectate:

- [Descărcări oficiale Selenium](https://www.selenium.dev/downloads/).
- [Jurnalul modificărilor Java Selenium 4.54](https://github.com/SeleniumHQ/selenium/blob/selenium-4.54.0/java/CHANGELOG).
- [Descriptorul publicat al dependențelor Selenium 4.54](https://repo.maven.apache.org/maven2/org/seleniumhq/selenium/selenium-java/4.54.0/selenium-java-4.54.0.pom).
- [Manifestul exact al versiunii Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/158.0.8127.88.json).
- [Configurarea oficială Selenium Manager](https://www.selenium.dev/documentation/selenium_manager/).

<a id="g-005"></a>
<a id="g-005-keep-project-paths-portable"></a>
## G-005: Păstrarea portabilității căilor proiectului

- Responsabil: guvernanța surselor/documentației și ghidurile responsabile.
- Observație originală: pornirea și înregistrările istorice încorporau structura stației
  de lucru a autorului. Un test de încărcare nesusținută folosea un nume absolut fără legătură
  pentru un fișier fictiv. Rădăcinile de cache/temporare la execuție derivau deja din setările apelantului.
- Cerință: instrucțiunile redactate din depozit, sursele și configurația folosesc
  căi relative la proiect, resurse classpath sau rădăcini explicite ale apelantului/mediului.
  Rezolvați o cale absolută la execuție din acele intrări când un API o
  cere. Nu încorporați unități de disc ale stației de lucru, directoare de cont sau URI-uri private de fișier.
- Livrat: normalizarea dovezilor istorice la `${ATF_WORK_ROOT}`;
  păstrarea provenienței originale a execuțiilor în afara copiei de lucru. Rezolvarea configurației
  consumatorului o singură dată din rădăcina reactorului, astfel încât procesele modulelor Maven să vadă același
  fișier. Păstrarea validității API-urilor JVM pentru temporare/directorul utilizatorului și a căilor HTTP/API.
- Acceptanță: fixture-uri pozitive și negative relevante pentru surse/configurație/documentație
  protejează limita, inclusiv literali Java decodați, secvențe de escapare,
  forme Windows/UNC/rădăcină de cont și căi derivate din mediu. Compilarea/testarea unei
  copii temporare relocate și păstrarea comparațiilor surselor.
- Verificare:
  [guvernanța căilor](../../atf-tests/src/test/java/com/ai/atf/governance/GovernanceFiles.java)
  și [fixture-urile de arhitectură](../../atf-tests/src/test/java/com/ai/atf/governance/FrameworkArchitectureGovernanceTest.java)
  existente verifică căile fixe în textul deținut și în literalii Java decodați. Rădăcinile dinamice ale utilizatorului/apelantului,
  rezolvarea JVM pentru temporare/directorul utilizatorului, căile HTTP/API și selectorii XPath expliciți
  rămân valide. Ambele copii acceptate, locală și agregată, au rulat cu succes
  sub calea relocată a spațiului de lucru portabil.
- Stare: VERIFIED pentru controlul delimitat și execuțiile relocate înregistrate.
  Aceasta nu stabilește toate expansiunile shell posibile sau acceptanța pe fiecare sistem de operare.

<a id="d-001"></a>
<a id="d-001-improve-readme-navigation-and-review-practice"></a>
## D-001: Îmbunătățirea navigării README și a practicii de revizie

- Responsabil: documentația din rădăcină/module și politica canonică de guvernanță.
- Observație originală: README-ul avea exemple publice corecte, dar navigarea
  cititorului, separarea fluxurilor de dependențe și îndrumarea pentru revizie puteau fi mai clare.
- Rezultat așteptat: hărți compacte de arhitectură răspund unor întrebări specifice ale cititorului. Legendele
  alăturate explică semnificația săgeților și responsabilitatea. Notele detaliate de implementare
  rămân în ghidurile lor responsabile. Exemplele concrete Playwright/Selenium se compilează
  față de API-ul public. Întreaga proză, întregul cod și toate comentariile redactate rămân în engleză.
- Preluare din J0: navigare după întrebările cititorului, diagrame cu un singur scop, legende
  apropiate și o singură listă canonică de revizie bazată pe dovezi. Nu copiați catalogul
  de module, aliasurile lingvistice sau contractele de execuție J0 în acest framework.
- Acceptanță: guvernanța documentației/surselor trece; legăturile și secțiunile/exemplele
  obligatorii se rezolvă; diagramele sunt randate și verificate pentru lizibilitate.
  Conținutul README corespunde reactorului cu cinci module și Atlas-ului exclusiv de documentație.
- Dovezi de închidere inițială: indicii din rădăcină și ai responsabililor îndrumau cititorii după întrebare; revizia codului/documentației
  are o singură politică canonică. Cele trei exemple Java complete sunt neschimbate
  și se compilează prin guvernanța existentă a documentației. Mermaid 11.4.1 efectiv
  a randat toate cele cinci diagrame README la 1440 și 520 px; înălțimea minimă a etichetelor
  randate a fost de 17 px, fără etichete tăiate sau depășire orizontală a paginii. HTML-ul Living
  păstrează 34 de titluri, 7 SVG-uri statice și zero scripturi, cu introducerea
  inspectată la ambele lățimi.
- Dovezi: `${ATF_WORK_ROOT}/tools/f008-readme-preview-iteration-03` păstrează
  PNG-uri, SVG-uri, hash-uri de surse/resurse, comparații ale exemplelor și rezultate măsurate.
  Diagramele inițiale cu etichete mici și iteration-02 rămân previzualizări istorice
  separate. Aliasul final al ciclului de viață este Test, evitând împărțirea anterioară a lui Consumer pe rânduri.
- Stare: VERIFIED prin rulările acceptate care includ guvernanță și prin inspecția vizuală
  efectivă. Vizualizatorul local adaptiv nu dovedește randarea pe fiecare gazdă Markdown;
  markerii lingvistici finiți necesită în continuare revizie umană în engleză.

Revizie ulterioară: README-ul rădăcină explică traseele API/UI ca hartă a
framework-ului, păstrează rezumatul celor cinci module în două coloane și
trimite exemplele către ghidurile Playwright/Selenium. Guvernanța impune harta
dependențelor și diagrama de execuție API/UI în secțiunile lor și include
fixture-uri negative pentru rute lipsă, exemple plasate greșit, diagrame lipsă,
celule dense și rânduri nealiniate. Ambele exemple existente ale adaptoarelor
rămân verificate prin compilare. Verificarea din 2026-11-26: selecția de
guvernanță/lizibilitate a trecut cu 176/176 în fiecare ediție, folosind rădăcini
peer explicite; inventarul revizuit al metodelor de test a trecut cu 1/1 în
ambele. Nu s-a rulat E2E de produs, profil de browser sau emulator. Limita de
140 caractere/22 de cuvinte este doar un semnal de triere; tabelele rădăcinii
modificate nu au fost randate din nou, iar verificările deterministe nu înlocuiesc
revizia în rendererul Markdown țintă.

<a id="t-004"></a>
<a id="t-004-bound-browser-fixture-lifetime-separately-from-network-matching"></a>
## T-004: Delimitarea duratei de viață a fixture-ului de browser separat de potrivirea rețelei

- Responsabil: cele două metode de rețea existente în SeleniumPlaywrightLocatorParityContractTest.
- Reproducere: modules-f008-browser-final.log înregistrează o expirare între cele 108
  cazuri ale suitei de browser, cu toate cele 12 verificări native trecute. Invocarea a durat
  37.17 secunde și a fost întreruptă în DriverService.stop/ExternalProcess.waitFor
  în timpul curățării ChromeDriver deținute de finally. Rulările istorice anterioare
  păstrează și observații privind expirarea curățării; ele nu sunt șterse de această corecție.
- Rezultat așteptat: potrivirea de rețea își păstrează setarea de două secunde, iar o
  nepotrivire de URL sau metodă își păstrează limita superioară observată independentă de 15 secunde. Pregătirea și
  curățarea proceselor deținute au o durată de viață finită separată pentru întregul fixture.
- Corecție: schimbarea numai a limitelor celor două metode existente de la 30 la 60 de secunde,
  calibrată față de stiva repetată de curățare și durata de viață observată de 37 de secunde.
  Adăugarea unor înregistrări monotone pentru pregătire/finalizarea corpului/curățare. Un marker de finalizare este
  emis numai după fiecare aserțiune din corp; eșecurile de curățare rămân eșecuri.
  Păstrarea numerelor de cereri, încărcăturilor de date, verificărilor de versiune/binar/domeniu, firelor
  de invocare și comportamentului de oprire din producție.
- Acceptanță: suitele complete locală și agregată, plus profilul de browser
  separat, trec fără omiteri. Inspectarea înregistrărilor de fază ale tuturor celor șase invocări și a rezultatelor
  reale cerere/răspuns; schimbarea unui buget sau o rerulare parțială singură nu reprezintă dovadă.
- Livrat: rularea de browser separată acceptată conține 18 linii de fază monotone
  pentru toate cele șase invocări de rețea: pregătire completă, corp complet și ieșire din curățare.
  Fiecare curățare înregistrează bodyCompleted=true după aserțiunile corpului. Curățarea
  maximă a fost de 458 ms, iar timpul maxim complet al fixture-ului a fost de 5280 ms. Așteptarea de rețea de două secunde
  și limita negativă independentă de 15 secunde sunt neschimbate; nicio aserțiune
  sau eroare de curățare nu a fost convertită în succes.
- Stare: VERIFIED prin toate cele trei selecții complete acceptate de mai sus. Eșecul inițial de curățare
  de 37.17 secunde rămâne în registrul rulărilor eșuate; aceste observații
  stabilesc execuțiile înregistrate, nu o garanție universală privind durata proceselor.
