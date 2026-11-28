<a id="deeper-executable-contracts"></a>
# Contracte executabile aprofundate

Referință inițială: `2f676d9`. Această etapă adaptează practici utile J0 de guvernanță
a documentației/surselor și principiile de responsabilitate din arhivă. Nu adaugă funcționalități de execuție,
module, registre de skill-uri sau fișiere de instrucțiuni pentru agenți. Corecțiile minime ale defectelor
existente sunt permise; schimbările surselor și dovezile trebuie identificate explicit.

<a id="closure-evidence"></a>
## Dovezi de închidere

Reper de implementare: `3286731a158dce2885608e8f9f2e4fd684f6b72f`.
Copiile finale locală și agregată includ corecția fixture-ului T-003.
Fiecare comandă de mai jos a rulat din propriul reactor izolat sub
${ATF_WORK_ROOT}/validation. Prefixul comun al comenzii, normalizat la spațiul de lucru, a fost
`mvn -B -ntp -Dmaven.repo.local=${ATF_WORK_ROOT}\cache\m2`; adăugați argumentele rândului.
Jurnalele sunt sub ${ATF_WORK_ROOT}/runs, iar rezultatele XML ale modulelor rămân în directoarele
target/surefire-reports ale copiei de validare corespunzătoare.

| Copie de validare și identificator de bază al jurnalului | Argumentele comenzii | Rezultat observat |
|---|---|---|
| modules-phase2-local-reliable-final | `-o verify` | 474 trecute: 463 atf-tests și 11 verificări native ale bibliotecilor; fără eșecuri/erori/omiteri |
| modules-phase2-aggregate-reliable-final | `-o -Paggregate test` | 474 trecute: 463 atf-tests și 11 verificări native ale bibliotecilor; fără eșecuri/erori/omiteri |
| modules-phase2-browser-alone | `-o -Pbrowser test` | 119 trecute: 108 din suita de browser și 11 verificări native ale bibliotecilor; fără eșecuri/erori/omiteri |

Copia separată pentru browser precedă modificarea finală T-003 din
WaitUtilsContractTest. [Suita de browser](../../atf-tests/src/test/resources/suites/framework-browser.xml)
întreținută nu selectează acea clasă; testele de browser selectate și sursele de execuție erau
neschimbate. Aceasta stabilește domeniul de browser selectat, nu o copie de lucru complet
identică. Numerele se suprapun între rulări și nu trebuie însumate ca teste unice.
Aceste înregistrări de execuție nu afirmă o publicare nouă sau un audit nou al JAR-urilor împachetate.

Execuțiile eșuate anterioare rămân dovezi, nu înregistrări de succes care le înlocuiesc:

| Jurnal istoric de eșec | Eșec observat |
|---|---|
| modules-phase2-browser-final.log | 108 invocări ale suitei de browser, 2 eșecuri: invocările de rețea pozitivă și de nepotrivire au depășit limitele metodelor de 30 de secunde în timpul curățării browserului; 11 verificări native au trecut |
| modules-phase2-aggregate-readme-final.log | 463 de invocări atf-tests, 1 eșec: fixture-ul de succes pentru valoarea de intrare a durat 898 ms față de bugetul său anterior de 600 ms; 11 verificări native au trecut |

Dovezile independente pentru exemplele README sunt
${ATF_WORK_ROOT}/tools/readme-example-qa-20261124-01/results.json, compile.log și
jurnalele/XML-ul de execuție ale motoarelor din acel director. Toate cele trei exemple README complete
s-au compilat față de API-ul public existent cu Java 11; exemplele Playwright și Selenium
au executat fiecare cu succes câte un test pe o pagină locală, fără eșecuri/omiteri.
Sunt execuții QA separate, nu cazuri suplimentare ale reactorului.

<a id="g-002"></a>
<a id="g-002-source-visibility-and-execution-ownership"></a>
## G-002: Vizibilitatea surselor și responsabilitatea execuției

- Responsabil: atf-tests,
  [GovernanceFiles](../../atf-governance/src/main/java/com/ai/atf/governance/GovernanceFiles.java),
  [FrameworkArchitectureRules](../../atf-governance/src/main/java/com/ai/atf/governance/FrameworkArchitectureRules.java)
  și [regresiile de arhitectură existente](../../atf-governance/src/test/java/com/ai/atf/governance/FrameworkArchitectureGovernanceTest.java).
- Observație originală: excluderile globale după numele directoarelor puteau ascunde pachete sursă legale
  numite target sau artifacts. Politicile de pauză, consolă și întrerupere pentru întregul cod de execuție
  erau documentate sau verificate local, dar nu erau guvernate între module.
- Rezultat așteptat: sursele Java/resurse deținute rămân vizibile indiferent de numele legale
  ale pachetelor; rezultatele reale ale compilării rămân excluse. Pauzele directe și publicarea
  în consolă au responsabili exacți. Tratarea excepțiilor InterruptedException restaurează structural
  indicatorul firului curent, susținută de teste comportamentale separate.
- Acceptanță: fixture-uri compilabile cu pachete legale expun importuri interzise/duplicate;
  fixture-urile de ieșire rămân excluse. Fixture-urile negative de execuție disting
  apelurile reale de comentarii/literali/importuri statice și de restaurarea pe firul greșit.
  Testele API/UI existente de așteptare dovedesc păstrarea efectivă a întreruperii.
- Livrat: inventarele surselor păstrează căile pachetelor legale deținute; controalele de responsabilitate
  exactă resping încălcările reale de pauză/consolă și restaurarea incorectă a întreruperii.
  Verificările structurale sunt completate de cazuri comportamentale de întrerupere API/UI;
  nu stabilesc corectitudinea universală a fluxului de control.
- Stare: VERIFIED prin execuțiile finale locală și agregată de mai sus.

<a id="g-003"></a>
<a id="g-003-executable-documentation-facts-and-lifecycle"></a>
## G-003: Fapte executabile din documentație și ciclu de viață

- Responsabil:
  [regulile de documentație](../../atf-governance/src/main/java/com/ai/atf/governance/MarkdownDocumentationRules.java)
  și [responsabilii tematici ai testelor de documentație](../../docs/governance/README.md)
  existente, ghidurile stabile și perechea canonică Living Architecture Markdown/HTML.
- Observație originală: comenzile Maven din blocuri nu erau legate de selecții reale de profiluri/module/suite/teste,
  iar o valoare implicită documentată schimbată putea rămâne verde după simpla
  reîmprospătare a hash-ului sursă HTML. Ghidurile stabile puteau acumula tabele de note sau
  înregistrări datate de succes fără o constatare de ciclu de viață.
- Rezultat așteptat: comenzile executabile susținute se rezolvă față de POM-ul selectat și
  inventarul surselor. Blocurile explicite de valori implicite ale proprietăților corespund fișierului curent
  framework.properties; exemplele alternative rămân exemple. Ghidurile stabile ale responsabililor
  descriu contractele actuale, în timp ce feedback-ul păstrează înregistrările istorice delimitate.
- Acceptanță: fixture-uri relevante de succes/eșec acoperă profiluri indisponibile, suite/clase/POM-uri
  lipsă, valori implicite învechite și înregistrări plasate greșit, inclusiv exemple
  literale și excepții legitime de feedback. Ghidul întreținut rămâne în pereche.
- Livrat: blocurile de comenzi Maven susținute, pe un singur rând, rezolvă POM-urile selectate,
  profilurile reactorului/părintelui, modulele, suitele și filtrele efective de clase de test. Argumentele între
  ghilimele și exemplele literale/din comentarii au acoperire explicită prin fixture-uri.
  Blocurile `properties defaults` corespund framework.properties. Verificările ciclului de viață al ghidurilor
  stabile păstrează excepțiile pentru feedback/înregistrări istorice și hash-ul ghidului pereche.
  Aceasta este o analiză delimitată a comenzilor, nu un interpretor shell general sau un rezolvitor de POM efectiv.
- Stare: VERIFIED prin execuțiile finale locală și agregată de mai sus.

<a id="t-002"></a>
<a id="t-002-actual-api-execution-and-local-browser-network-evidence"></a>
## T-002: Execuție API efectivă și dovezi locale de rețea în browser

- Responsabil:
  [regresiile de execuție API](../../atf-tests/src/test/java/com/ai/atf/core/support/base/ApiCoreExecutionArchitectureContractTest.java),
  [regresiile sesiunilor de raportare](../../atf-tests/src/test/java/com/ai/atf/core/reporting/regression/ReportingManagerSessionContractTest.java)
  și [fixture-ul de paritate între motoare](../../atf-tests/src/test/java/com/ai/atf/ui/component/parity/SeleniumPlaywrightLocatorParityContractTest.java)
  existente.
- Observație originală: verificările politicii/parserului de reîncercare nu executau ApiRequestExecutor în sine.
  Nu era înregistrată nicio acceptanță reală a răspunsurilor de rețea în browser; verificările de capabilitate, delegare
  și surse nu puteau stabili încărcăturile cerere/răspuns observate.
- Rezultat așteptat: încercările executorului, întârzierile reîncercărilor și ordinea raportării au observații
  independente. Comportamentul cache-ului/generării suitei urmează semantica efectivă ReportingSession.
  Ambele motoare observă o cerere și un răspuns locale reale; nepotrivirile
  eșuează în bugetul lor de așteptare, iar curățarea deține starea serverului/browserului.
- Acceptanță: regresiile API/de sesiune existente acoperă succesul/epuizarea reîncercărilor,
  căile fără reîncercare și cele de excepție. Testele existente de paritate în browser verifică URL-ul, metoda,
  starea și încărcăturile reale, plus rezultatele nepotrivirilor URL/metodă față de un server
  local. Nicio modificare a aplicației publice sau soluție de rezervă cu corp gol nu contează drept dovadă.
- Livrat: cazurile locale ale executorului și sesiunilor de raportare au trecut în suitele finale
  locală și agregată. Observațiile reale din browser au trecut pentru ambele motoare:
  2 sonde pozitive de răspuns și 4 sonde de nepotrivire URL/metodă în rularea completă
  cu 119 verificări de browser/native. Curățarea serverului și browserului este exercitată;
  expirările anterioare ale curățării rămân în registrul rulărilor eșuate de mai sus.
- Stare: VERIFIED pentru aceste contracte locale ale framework-ului. F-008 are o înregistrare ulterioară,
  independentă, a matricei browser/driver/CDP tipizat. Paginile locale de rețea
  stabilesc contractul declarat al framework-ului. Domeniul curent este înregistrat în [registru](README.md).

<a id="f-009"></a>
<a id="f-009-negative-api-retry-budgets-skip-the-request"></a>
## F-009: Bugetele negative de reîncercare API omit cererea

- Responsabil: atf-core
  [ApiRuntimeOptions](../../atf-api/src/main/java/com/ai/api/core/client/ApiRuntimeOptions.java)
  și regresiile existente de execuție API legate în T-002.
- Reproducere originală: crearea opțiunilor cu maxRateLimitRetries=-1 și executarea unei cereri
  al cărei furnizor incrementează un contor. Versiunea de referință compilată neschimbată a returnat
  attempts=0 și response=null, cu supplierCalls=0.
- Dovezi: ${ATF_WORK_ROOT}/runs/negative-api-retry-baseline.log și reproducerea independentă
  runtime-deepen-20261124 sub ${ATF_WORK_ROOT}/tools.
- Rezultat așteptat: respingerea unui argument negativ al fabricii sau a unei valori configurate negative pentru reîncercări înainte de
  execuție, cu un diagnostic acționabil. Un buget de zero reîncercări execută în continuare
  cererea inițială o dată; comportamentul valid de reîncercare rămâne neschimbat.
- Decizie de domeniu: utilizatorul a aprobat corecții minime ale defectelor existente,
  fără clase/funcționalități de execuție noi. Nu validați vechiul rezultat null printr-un
  test trecut și nu modificați comportamentul fără legătură al întârzierii din fabrică.
- Livrat: validarea existentă din fabrică/încărcare respinge bugetele negative de reîncercare
  înainte de executarea cererii. Regresiile existente verifică opțiunile și configurația
  negative, o singură cerere inițială cu zero reîncercări și comportamentul valid de reîncercare.
  Perechea Living Architecture documentează același contract.
- Stare: VERIFIED prin execuțiile finale locală și agregată de mai sus.

<a id="g-004"></a>
<a id="g-004-english-authored-source-comments-and-diagnostics"></a>
## G-004: Surse, comentarii și diagnostice redactate în engleză

- Responsabil: JavaSourceScanner din atf-tests și
  [regresiile sale existente de guvernanță lingvistică](../../atf-governance/src/test/java/com/ai/atf/governance/JavaSourceScannerTest.java),
  împreună cu [ENG-001](../governance/Code_And_Documentation_Policy.md#english-source-and-comments)
  și guvernanța documentației ca responsabili ai contractului public.
- Cerință: toți identificatorii de cod redactați, comentariile/Javadoc și mesajele framework-ului/testelor
  TREBUIE scrise în engleză (ENG-001 în politica canonică).
  Datele externe/localizate rămân date. Cerința însăși și responsabilul de test legat
  sunt întreținute ca documente contractuale.
- Rezultat așteptat: regiunile de cod executabil, comentarii și literali sunt verificate separat;
  comentariile nu se pot ascunde în spatele excepțiilor pentru date de test. Responsabilii exacți ai fixture-urilor de politică
  și motivele lor sunt explicite, excepțiile învechite eșuează, iar fișierele de producție
  nu pot primi excepții generale pentru date de test.
- Acceptanță: inspectarea reală a depozitului, plus fixture-uri negative pentru identificatori
  românești, comentarii, Javadoc, diagnostice și markeri cu escapare Unicode;
  cazuri pozitive pentru delimitatori de literali/comentarii și date localizate cu responsabilitate strictă.
  Diagramele de arhitectură README și exemplele concrete de consum Playwright/Selenium
  sunt obligatorii; exemplele declarate complete se compilează față de API-urile publice efective.
- Livrat: controalele depozitului disting identificatorii, comentariile/Javadoc și
  mesajele literale după traducerea Unicode Java. Cele trei excepții exacte ale fixture-urilor de politică/date
  păstrează motivele și verificările de excepții învechite; identificatorii
  și comentariile lor rămân guvernate. Cele trei exemple README obligatorii controlează și
  identificatorii, comentariile și mesajele literale redactate și se compilează față de
  API-ul public actual. Compilarea independentă a celor trei exemple și execuțiile cu câte un test per motor
  sunt înregistrate mai sus; nu s-a adăugat nicio clasă exemplu Java nouă urmărită.
- Limită: verificările finite ale markerilor cunoscuți completează revizia manuală în engleză; nu
  stabilesc detectarea semantică universală a limbii și nu justifică traducerea datelor externe.
- Stare: VERIFIED prin guvernanța finală locală/agregată și QA-ul README independent.

<a id="f-010"></a>
<a id="f-010-repeated-configuration-changes-lose-an-absent-original-value"></a>
## F-010: Schimbările repetate de configurație pierd valoarea originală absentă

- Responsabil:
  [AtfRuntimeConfigSandbox](../../atf-core/src/main/java/com/ai/core/support/contract/ids/AtfRuntimeConfigSandbox.java),
  [regresiile de configurație](../../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java)
  existente și regresiile sesiunilor de raportare legate în T-002.
- Observație originală: putIfAbsent tratează un instantaneu null existent ca absent. Pornind fără
  proprietate, setarea ei la A și apoi B înlocuiește instantaneul original null
  cu A; close restaurează A în locul absenței. Valorile de sistem și cele ale framework-ului
  folosesc ambele acest tipar. Noua regresie pentru motor invalid a expus valoarea scăpată
  la terminarea suitei TestNG.
- Rezultat așteptat: ambele mapări de instantanee își păstrează prima valoare observată exact o dată,
  inclusiv null, pe parcursul operațiilor repetate de setare/ștergere.
- Acceptanță: regresiile directe de configurație și scenariul original al sesiunii de raportare
  trec fără o soluție de ocolire cu sandbox imbricat sau o schimbare a mecanismului de rezervă al motorului.
- Livrat: capturarea bazată pe containsKey păstrează primul instantaneu în ambele mapări.
  Fixture-urile directe pentru valoare de sistem absentă/prezentă/goală și scenariul original al sesiunii de raportare
  cu motor invalid au trecut fără soluția de ocolire. Semantica de căutare combinată a ConfigManager
  este neschimbată; aceasta nu dovedește precedența brută a surselor sau
  proveniența pentru fiecare combinație sistem/configurație.
- Stare: VERIFIED prin execuțiile finale locală și agregată de mai sus.

<a id="t-003"></a>
<a id="t-003-input-value-success-regression-depends-on-scheduler-latency"></a>
## T-003: Regresia de succes a valorii de intrare depinde de latența planificatorului

- Responsabil:
  [WaitUtilsContractTest](../../atf-tests/src/test/java/com/ai/atf/ui/component/WaitUtilsContractTest.java)
  existent;
  WaitUtils de producție este neschimbat.
- Reproducere: modules-phase2-aggregate-readme-final.log înregistrează un eșec după
  o singură citire de valoare goală. Fixture-ul de succes necesită două observații, interoghează la
  250 ms și avea un buget total de numai 600 ms; invocarea a durat 898 ms.
- Rezultat așteptat: o regresie de succes observă a doua valoare și verifică exact
  două citiri. Întârzierea planificatorului nu trebuie să fie oracolul său de succes. Testul independent
  fără potrivire verifică în continuare diagnosticul de expirare și o citire observată.
- Corecție: s-au schimbat numai bugetul fixture-ului de succes și comentariul său explicativ în engleză.
  Limita de cinci secunde păstrează secvența de două valori, predicatul de potrivire
  și aserțiunea de exact două citiri. Cazul separat de expirare de 10 ms fără potrivire este neschimbat.
- Stare: VERIFIED prin execuțiile finale locală și agregată de mai sus. Înregistrarea agregată
  eșuată înaintea corecției rămâne în registrul istoric.
