<a id="actionable-governance-diagnostics-and-exact-negative-controls"></a>
# Diagnostice acționabile de guvernanță și controale negative exacte

Bază: `3edac5a1d502284469a45ef1ce3fbff0ba496a2e`. Domeniu: utilitare de test
existente, cazuri de guvernanță și documentație. Nu se solicită funcționalitate
runtime, fișier Java nou, scenariu E2E sau emulator. Ediția main_RO păstrează
același cod și aceleași documente non-README.

<a id="g-008--constructive-governance-failures"></a>
## G-008 — eșecuri constructive de guvernanță

- Responsabil: GovernanceFiles și controalele existente de arhitectură/documentație/sursă.
- Prioritate: 83, din impact/risc/urgență 90/75/75.
- Calitate inițială: 62, din corectitudine/verificare/claritate 75/45/60.
- Observație: formatatorul comun doar unește șiruri brute. O verificare eșuată
  nu identifică în mod consecvent motivul regulii, contractul responsabil,
  o corecție utilă și o selecție limitată de reverificare. Constatările brute trebuie păstrate intacte.
- Acceptanță: eșecurile existente includ dovada observată, sensul eșecului,
  indiciul de remediere, responsabilul canonic și comanda întreținută de reverificare.
  Păstrează ordinea intrării, constatările duplicate, domeniul fișierului/suitei
  și numerele originale ale liniilor; nu inventa locații și nu deduce cauza
  unei reguli necunoscute. Documentează fluxul corecției și protejează formatarea prin controale pozitive și negative.
- Stare: VERIFIED la 2026-11-25. Diagnosticele cu responsabil păstrează dovezile
  originale, ordinea, duplicatele și domeniul producătorului. Cazurile reale
  de import/legături/limbă resping intrarea incorectă și acceptă repararea;
  toate rutele responsabile cunoscute și clasele întreținute de reverificare
  se rezolvă. Constatările necunoscute păstrează incertitudinea explicită.
- Calitate livrată: 95, din corectitudine/verificare/claritate 95/95/95.

<a id="t-006--exact-rule-identity-in-negative-governance-fixtures"></a>
## T-006 — identitatea exactă a regulii în cazurile negative de guvernanță

- Responsabil: funcțiile assertFinding existente și mecanismul comun de potrivire a constatărilor.
- Prioritate: 86, din impact/risc/urgență 90/85/75.
- Calitate inițială: 68, din corectitudine/verificare/claritate 75/50/75.
- Observație: potrivirea pe subșir poate confunda DOC-EDITION-PARITY cu
  DOC-EDITION sau poate accepta un cod menționat numai în conținutul observat.
  Un caz negativ putea trece pentru regula greșită, deși controlul intenționat dispăruse.
- Acceptanță: potrivește tokenul real al regulii emise și păstrează fragmentele
  cerute de locație sau detaliu. Respinge coliziunile de prefix, mențiunile
  din conținut și textul de remediere al formatatorului drept dovezi.
  Aserțiunile cu eșec intenționat trebuie să expună regula așteptată și constatarea
  reală; intrarea reparată a cazului trebuie să treacă propriul control.
- Stare: VERIFIED la 2026-11-25. Ambele funcții existente de aserțiune potrivesc
  tokenul regulii emise și fragmentul de dovadă cerut. Controalele independente
  de prefix/conținut/locație resping constatarea greșită, inclusiv printr-o
  aserțiune realmente eșuată. Cazurile negative existente cer și îndrumare responsabilă pentru constatările emise.
- Calitate livrată: 95, din corectitudine/verificare/claritate 95/96/95.

<a id="reference-scope"></a>
## Domeniul referinței

ATF_J0 la `96f50e5f277771ca9dfac3b7c959b47188afd0b0` separă constatarea,
regula și remedierea în DocumentationGovernanceSupport. Schimbările sale în curs
în feedback-ul raportării sunt independente și rămân neatinse. J0 este inspectat,
nu executat. Arhiva imuabilă main_mcp la
`70a7279c2a9560d39e6bd2554cd2f96a88626692` demonstrează sensul eșecului,
indicii de remediere și controale negative explicite ale cauzei. Adoptă aceste
principii de diagnostic în utilitarele de test curente, fără a copia cataloagele
YAML, pachetele de skills, runtime-ul, generarea rapoartelor sau pragurile numerice
de inventar. Fișierele de referință și hash-urile rămân în dovezile externe.

<a id="focused-observations"></a>
## Observații focalizate

Prima probă din sursa pregătită a respins noul exemplu de import, deoarece
cazul său Java omitea un pachet numit. Constatarea reală era PACKAGE, astfel
încât aserțiunea exactă IMPORT-WILDCARD a eșuat corect. Corectează pachetul
cazului și calea sa; păstrează intacte regula de pachet și cea de import.
Proba corectată a trecut 17 invocări directe, inclusiv reparări reale de
import/legături/limbă și rutele canonice ale responsabililor de diagnostic.
Acestea nu sunt totaluri TestNG ale reactorului. Ambele jurnale focalizate originale
și manifestele sursei sunt păstrate sub rădăcina externă de lucru ca
modules-diagnostics-focused-01 și modules-diagnostics-focused-02.
Ulterior, ambele selecții complete de ediție au trecut; domeniul lor este înregistrat mai jos.

Primele selecții complete au executat fiecare 550 de cazuri: 549 trecute și
un caz existent de arhitectură eșuat, deoarece așteptarea sa era doar expresia
static ownership inventory. Noul contract al aserțiunii a respins corect acea
așteptare neclasificată. Leagă cazul de DEPENDENCY-COORDINATE și cere artefactul
real nerezolvat în aceeași constatare observată. Păstrează ambele rulări complete
nereușite; nu se schimbă vreo regulă runtime sau graful așteptat al modulelor.

<a id="current-comparison-and-ratings"></a>
## Comparația și notele curente

Folosește aceeași grilă inginerească și aceleași ponderi ca în comparația
anterioară. Notele sunt evaluări de revizuire, nu acoperire, disponibilitate
sau benchmark runtime. J0 rămâne revizuit doar la nivel de sursă la revizia
capturată; schimbările sale în curs au fost păstrate, iar reactorul său nu a fost rulat.

| Axă | Pondere | ATF_AI anterior | ATF_AI curent | ATF_J0 revizuire sursă |
|---|---:|---:|---:|---:|
| Arhitectură și limite | 20 | 90 | 90 | 92 |
| Guvernanță executabilă | 30 | 94 | 95 | 94 |
| Calitatea testelor existente | 25 | 91 | 92 | 88 |
| Documentație și navigare | 15 | 94 | 95 | 89 |
| Responsabilitatea și ciclul feedback-ului | 10 | 93 | 93 | 85 |
| Total ponderat | 100 | **92** | **93** | **90** |

Totalurile nerotunjite sunt 92.35, 93.05 și 90.45. Diagnosticele cu responsabil
și îndrumarea rezolvabilă îmbunătățesc guvernanța executabilă; controalele
negative exacte îmbunătățesc oracolul testului existent; noul ghid al eșecurilor
și diagrama îmbunătățesc navigarea și înțelegerea. Numerele de module/teste
nu primesc puncte. Diferența mică de note nu demonstrează superioritate runtime față de un proiect de referință neexecutat.

<a id="executed-evidence-and-boundaries"></a>
## Dovezi executate și limite

| Selecție | Revizie sursă | Rezultat | Numele dovezii |
|---|---|---|---|
| Prima probă focalizată prin metode directe | Sursă pregătită din 3edac5a | Eșuată: noul caz de import nu avea pachet numit; IMPORT-WILDCARD așteptat a fost respins corect | modules-diagnostics-focused-01 |
| Proba focalizată corectată | Sursă pregătită din aceeași bază | 17 invocări directe trecute; nu un total de suită TestNG | modules-diagnostics-focused-02 |
| Selecția locală completă engleză cu pereche explicită | 5324975 | 550 trecute; incluse 211 cazuri de guvernanță și 24 API ale frameworkului | modules-diagnostics-main-local-02 |
| Selecția locală completă română cu pereche explicită | dd3f5b7 | 550 trecute; același domeniu de guvernanță/API | modules-diagnostics-ro-local-02 |

Ambele reactoare s-au încheiat fără eșecuri, erori sau omiteri. Numerele se
suprapun între ediții. Java runtime, POM-urile, testele native ale bibliotecilor,
sursele E2E publice și perechea esențială HTML/Markdown de arhitectură sunt
neschimbate. Nu s-au introdus fișiere Java, scenarii E2E, emulatoare, module
Maven sau runtime istoric restaurat. Profilurile de browser/agregare nu au
fost rerulate pentru aceste schimbări de suport de test/documentație;
nu se pretinde acceptanță nouă de browser sau aplicație externă.

Fișierele de clase ale sursei acceptate rămân neschimbate de dovada finală de
feedback. O verificare nouă sursă/documentație în pereche validează acele editări
exclusiv de dovadă, inclusiv exemplele consumatorilor verificate de compilator
și identitatea completă a edițiilor; nu este o nouă execuție a fiecărui caz.
Toate cele 21 de căi README, ierarhia, comenzile, blocurile de cod, legăturile,
ancorele de intrare și topologia rămân aliniate. Între ramuri diferă numai proza README și proprietatea declarată a ediției.

Chromium desktop a randat 12 înregistrări document/ediție/lățime la 1440/520 px,
fără emulare sau depășire a paginii; înălțimea minimă măsurată a etichetelor a
fost 24 px. Arhitectura README-ului rădăcină și exemplele existente
Playwright/Selenium rămân neschimbate. Prima previzualizare a expirat încărcând
șablonul vechi randat; jurnalul nereușit rămâne păstrat. Previzualizarea
acceptată folosește resurse locale fixate și o pagină inițială goală, cu cererile externe blocate.

Referințele revizuirii, eșecurile focalizate originale, hash-urile sursei,
revizuirile README-urilor traduse și dovezile diagramelor aparțin
${ATF_WORK_ROOT}/reviews/20261125-diagnostics. Instantaneele exacte ale sursei,
comenzile și jurnalele aparțin directoarelor externe validation/runs.
Pachetele de rapoarte curente și dovezile finale de verificare sunt arhivate
după scop sub ${ATF_WORK_ROOT}/artifacts/reports/runs. Proba originală eșuată
este păstrată; colectarea nu este verdict de execuție. Nu se pretinde scriere GoRest autentificată.
