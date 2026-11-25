<a id="project-map-template"></a>
# Șablonul hărții proiectului

Copiază șablonul într-un rezultat nou de analiză; nu completa șablonul menținut
cu rezultatele datate ale execuției unui proiect. Păstrează căile sursă relative
la rădăcina țintei și identifică revizia. Folosește UNKNOWN pentru un fapt neobservat.

Publică README.md, project-map.md, architecture.md, testing-and-governance.md,
open-questions.md, source-index.json și analysis.json ca intrări auditabile într-un
nou director de rulare. Livrabilul cititorului este exact un HTML complet pentru
un proiect selectat: delivery/atlas.html. Niciun alt HTML nu poate apărea sub delivery/.
Păstrează HTML-ul intermediar, Markdown-ul, manifestele și rezultatele auditului/randării
în afara acelui director. Directorul livrării conține exact un fișier obișnuit,
atlas.html; fișierele auxiliare non-HTML și fișierele imbricate sunt de asemenea respinse.
Un index cu pagini multiple nu este raportul final de înțelegere.
Consultă [contractul raportului complet](../../atf-atlas/Report_Contract.md).

Leagă intrările de sursa reală și de documentele complete:

- analysis.json schemaVersion 1 fixează projectId/revision, sourceIndexSha256,
  documents cu hash-uri, claims și evidence. Fiecare document declarat trebuie să existe.
- Indexul sursei schemaVersion 1 identifică același proiect/revizie și consemnează
  fiecare cale capturată, SHA-256 și rol. Distinge fișierele revizuite de capturile
  numai de stare; verificatorul controlează căile capturate, nu domeniul semantic neindexat.
- Folosește UNVERSIONED când lipsesc metadatele Git. Consemnează limba reală, sistemul
  de construire, testele absente și domeniul inspectat; nu copia valorile implicite ale altui proiect.
- Același contract al livrării se aplică intrărilor asistate de cunoștințe.
- sourceDocuments enumeră documentele complete menținute ale țintei necesare domeniului.
  Fiecare intrare este relativă la țintă și legată de indexul sursei. Include README-ul,
  arhitectura, testarea și ghidurile reale de utilizare/contract, fără extrase parțiale
  sau navigare spre pagini HTML separate.

<a id="target-and-proof-boundary"></a>
## Ținta și limitele dovezii

| Câmp | Valoare observată | Dovadă |
|---|---|---|
| Proiect/revizie și starea arborelui de lucru | UNKNOWN | Identitatea sursei sau manifest păstrat |
| Domeniul cerut și excluderile | UNKNOWN | Cererea apelantului |
| Limbi și instrumente de construire/împachetare | UNKNOWN | Declarații reale |
| Documentație de intrare și puncte publice de intrare | UNKNOWN | Căi relative ale sursei |
| Zone inspectate și neinspectate | UNKNOWN | source-index.json |
| Teste executate pentru această analiză | NOT EXECUTED dacă nu există dovadă | Selecția exactă, rezultatul și artefactele |

Atribuie fiecărei afirmații un ID unic cu majuscule, textul exact și starea:
SOURCE_REVIEWED, EXECUTED, NOT_EXECUTED sau UNKNOWN. Referințele sursă declară path,
startLine, endLine și citatul exact. Declară governanceStatus MAPPED cu governanceRefs
concrete sau NOT_MAPPED, păstrând lacuna vizibilă. Fiecare ID/text al afirmației
trebuie să apară în project-map.md. Nu înlocui o referință cu numărarea cuvintelor-cheie
sau cu o cale copiată din alt proiect.

Dovezile de execuție declară projectId, calea relativă la artefacte, SHA-256,
selecția, identitatea originală a sursei și starea.

- O afirmație EXECUTED referă XML Surefire sau TAP Node plat, numărătorile observate
  tests/failures/errors/skipped și un sourceManifest care asociază căi relative cu
  hash-uri. sourceIdentity este SHA-256 al acelui manifest păstrat.
- Referințele implementării și guvernanței trebuie să corespundă octeților sursei executate.
- HTML-urile/rapoartele istorice rămân istorice; nu le reeticheta ca rulare nouă.
- Verdictul EXECUTED este PASSED sau FAILED și corespunde rezultatelor cazurilor
  referite. Antetele numai cu numărători nu stabilesc o selecție executată.

Legarea implicită a sursei este exactă. Dacă publicarea schimbă numai finalurile
de linie ale textului, declară sourceNormalization CRLF_LF, păstrează fiecare sha256
brut live și normalizedSha256 calculat și un normalizedManifest verificat lângă
manifestul original al execuției. Verificatorul respinge în continuare conținutul
schimbat. Normalizarea nu se aplică artefactelor lib publicate. Consemnează proveniența
derivată și digest-ul manifestului sursă original fără a rescrie dovezile rulărilor vechi.

<a id="architecture-and-ownership"></a>
## Arhitectură și responsabilități

Descrie cea mai mică hartă utilă de module/componente. Adaugă o diagramă cu noduri
și săgeți justificate de declarații/apeluri reale, urmată de legendă. Identifică
starea, configurația, dependențele externe și responsabilitatea resurselor/curățării.
Reprezintă harta prin dependencyGraphs cu id, title, kind DEPENDENCY, nodes și edges.
Fiecare nod are id, label, x, y și kind MODULE, COMPONENT, SERVICE sau BOUNDARY.
Fiecare muchie are from, to, label și claimId. O singură componentă susținută de
sursă poate avea zero muchii când limita grafului explică absența dependențelor declarate.

| Aspect | Responsabil | Contract public | Referință sursă |
|---|---|---|---|
| Intrare/compoziție | UNKNOWN | UNKNOWN | UNKNOWN |
| Comportament de bază și model de date | UNKNOWN | UNKNOWN | UNKNOWN |
| Adaptoare externe și eșecuri | UNKNOWN | UNKNOWN | UNKNOWN |
| Configurație și rezultate | UNKNOWN | UNKNOWN | UNKNOWN |

<a id="behavior-verification-and-proof-limits"></a>
## Comportament, verificare și limitele dovezii

Explică operațiile existente ale proiectului selectat și schimbările observate de
stare, nu numai topologia pachetelor sau dependențelor. Pentru un framework,
identifică consumatorul, cererea, valoarea/artefactul, contractul de ciclu/eșec și
rezultatul observat. Pentru o aplicație, folosește actorul real, obiectul și operația acceptată.

| Câmp behaviorFlows | Sens obligatoriu |
|---|---|
| id și title | Identitate locală stabilă și comportament existent lizibil |
| actor, intent, object și operation | Consumator/actor real, scop, subiect și acțiune |
| outcome | Rezultat observat sau susținut de sursă, cu limitele dovezii |
| testOracle și oracleStatus | Criteriu de verificare existent real sau domeniu căutat și lacună NOT_FOUND |
| oracleClaimId | Afirmație de test separată SOURCE_REVIEWED/EXECUTED sau afirmație UNKNOWN pentru NOT_FOUND |
| claimIds | Afirmații sursă existente revizuite/executate care susțin explicația |
| limits | Zone neexecutate, mapare parțială și limite de interpretare |
| graph | kind BEHAVIOR, noduri cu id/label/x/y/kind, muchii cu from/to/label/claimId |

Fiecare afirmație de muchie și flux se rezolvă la o afirmație de analiză SOURCE_REVIEWED
sau EXECUTED. Un criteriu de test absent nu creează o afirmație executabilă. O singură
stare comportamentală susținută poate să nu aibă muchii când graph.limits declară
motivul lipsei unei tranziții stabilite. Nu inventa fluxuri și nu cere teste noi de
produs/E2E pentru a umple lacuna.

| Comportament sau invariant | Implementare | Verificare sau revizuire existentă | Limitele dovezii și lacuna |
|---|---|---|---|
| UNKNOWN | UNKNOWN | UNKNOWN | Separă inspecția sursei de execuția reală |

Consemnează comenzile exacte numai după rezolvarea lor față de configurația reală
a țintei. Leagă rezultatele păstrate, inclusiv eșecurile și zonele neexecutate.
Nu deduce calitatea din numărul fișierelor, cuvinte ale aserțiunilor sau un raport copiat.

<a id="declared-breadth-record"></a>
## Înregistrarea amplorii declarate

Pachetul cititorului folosește renderVersion 4 pentru o captură publică nouă.
Adaugă analysis.understanding cu schemaVersion 1, scope PROJECT sau SELECTED,
scopeStatement, inventoryClaimIds și units. PROJECT aliniază inventarele unităților
revizuite și arhitecturii. SELECTED explică responsabilii omiși. Niciun domeniu nu
înseamnă acoperire semantică completă. Afirmațiile inventarului citează declarațiile reale.

| Câmp al unității | Sens obligatoriu |
|---|---|
| id, label și kind | Responsabil local real; MODULE, COMPONENT, SERVICE sau BOUNDARY |
| scopePaths | Domenii normalizate de fișiere/directoare sursă relative la țintă, prezente în index |
| claimIds | Afirmații revizuite cu referințe sursă în acest responsabil |
| sharedClaimIds | Referințe explicite revizuite către alt responsabil al sursei |
| flowIds | ID-uri de fluxuri existente care leagă invers unitatea prin unitIds |
| facets | Toate cele șapte fațete de lectură, cu stări, claimIds și explicații |
| limits | Ce nu stabilește revizuirea acestei unități |

| Fațetă | Stare | ID-uri de afirmații | Explicație și limită rămasă |
|---|---|---|---|
| purpose | UNKNOWN | Niciuna observată | Responsabilitate, apelanți și domeniu |
| structure | UNKNOWN | Niciuna observată | Intrări, limite și dependențe |
| behavior | UNKNOWN | Niciuna observată | Operație existentă, obiect și rezultat |
| data | UNKNOWN | Niciuna observată | Valori, stare, scheme și rezultate |
| configuration | UNKNOWN | Niciuna observată | Valori implicite, proprietăți și selecții reale |
| failure | UNKNOWN | Niciuna observată | Erori, limite, întrerupere și curățare |
| verification | UNKNOWN | Niciuna observată | Aserțiuni existente, revizuire și dovezi executate |

Înlocuiește UNKNOWN numai după inspecție. REVIEWED și PARTIAL cer claimIds susținute;
NOT_APPLICABLE cere motiv și nu poate elimina fiecare fațetă. Comportamentul revizuit
leagă un flux existent. Nodurile de dependență declară unitId. Fluxurile comportamentale
declară unitIds cu flowIds inverse corespunzătoare. Tipurile de graf rămân BEHAVIOR și
DEPENDENCY; o muchie comportamentală poate cita numai claimIds ale propriului flux.

Pentru o afirmație EXECUTED de comportament/criteriu, adaugă executionSubjects:

```json
{
  "evidenceId": "actual-retained-run",
  "testClass": "actual.retained.ClassName",
  "testName": "actual retained case name",
  "sourcePath": "relative/existing-oracle.ext",
  "sourceRefIndices": [0]
}
```

Leagă subiecți exacți și fragmente sursă selectate:

- Folosește classname/name din XML-ul păstrat; classname absent este șirul gol.
  TAP Node plat folosește testName exact și testClass gol/omis.
- sourcePath trebuie să fie deja citat în afirmație. Exemplul selectează intrarea
  0 cu indexare de la zero din sourceRefs, nu o linie sursă sau poziția unui caz;
  înlocuiește-o cu fragmentele reale corespunzătoare.
- Fiecare subiect al unui criteriu EXECUTED de caz declară acești indici, iar subiecții
  păstrați acoperă fiecare intrare oracleRefIndices selectată. Un hash de fișier sau totalul suitei este insuficient.
- Leagă fiecare ID de dovadă. Subiecții absenți, ambigui, duplicați, omiși sau TODO
  nu stabilesc execuția; păstrează eșecurile și proveniența originală.
- Revizuiește separat sensul aserțiunii. Identitatea cazului nu demonstrează fiecare propoziție.

<a id="behavior-case-witnesses"></a>
## Martori ai cazurilor comportamentale

Fiecare flux declară caseAnalysis schemaVersion 1, entryNodeId, pathScope,
negativeScope și cases. Fiecare muchie comportamentală are un id unic. Toate nodurile
comportamentale sunt accesibile din intrare și pot ajunge la un OUTCOME, fiecare cu
martor de caz. Topologia dependențelor are scop separat și poate avea module neconectate.

| Câmp al cazului | Conținut revizuit în sursă |
|---|---|
| id, kind și title | Caz existent unic POSITIVE/NEGATIVE și scopul său |
| precondition, trigger și expected | Pregătire reală, operație/intrare și observație |
| nodeIds și edgeIds | Parcurs direcționat contiguu de la intrare la un OUTCOME |
| claimIds | Afirmații sursă revizuite locale fluxului care susțin cazul |
| oracleClaimId și oracleStatus | Criteriu local existent exact sau lacună UNKNOWN/NOT_FOUND |
| oracleRefIndices | Indici sourceRefs selectați în afirmația acelui criteriu |
| assertion și limits | Comparație/respingere reală și limitele dovezii |

NOT_FOUND declară oracleGap cu searchedClaimIds și explanation. Consemnează dovezi
reale ale căutării fără a inventa un test sau o rulare. negativeScope consemnează
status REVIEWED, PARTIAL, UNKNOWN sau NOT_APPLICABLE, claimIds revizuite nevide
pentru domeniul inspectat și explanation. REVIEWED cere martor negativ;
NOT_APPLICABLE nu îl poate contrazice. Cazurile negative existente cer martori în
sursă și aserțiuni; lacunele rămân explicite.

REPRESENTATIVE descrie căi de caz selectate. ALL_DECLARED_EDGES cere acoperirea
setului de muchii declarat; nu afirmă fiecare ramură de implementare sau planificare
a buclelor. Nodurile repetate sunt valide într-un parcurs ciclic finit. Cazurile
pozitive și negative pot avea aceeași stare finală când observațiile susținute de
sursă diferă. Nu clasifica un caz dintr-un cuvânt al aserțiunii și nu reeticheta
revizuirea sursei ca execuție.

Intrarea poate avea orice tip real de nod comportamental; actor/intent sunt câmpuri
separate ale fluxului, deci nu inventa un nod de intenție pentru o intrare reală de
stare/acțiune. Buclele comportamentale către sine acceptate cer un martor finit,
o ieșire spre rezultat și un traseu randat clar. Buclele dependențelor către sine
rămân invalide. O intrare delimitată cu un singur OUTCOME rămâne validă cu o lacună
explicită a criteriului căutat.

Intervalele selectate ale sursei criteriului nu se pot suprapune niciunui interval
al implementării din același fișier; schimbarea unei limite cu o linie nu stabilește
independența. Fragmentele fără suprapunere din același fișier rămân valide. Pentru
criterii EXECUTED de caz, fiecare intrare executionSubjects declară sourceRefIndices
unici nevizi în sourceRefs, toți corespunzători sourcePath. Subiecții reali trebuie
să acopere fiecare intrare oracleRefIndices selectată și să satisfacă poarta
identității/rezultatului XML/TAP păstrate.

O fațetă de eșec REVIEWED necesită un flux propriu cu martor NEGATIVE și
negativeScope REVIEWED sau PARTIAL. O afirmație sursă negativă singură este insuficientă.

<a id="optional-evidence-bound-assessment"></a>
## Evaluare opțională susținută de dovezi

Dacă este cerută, adaugă analysis.assessment cu method EVIDENCE_BOUND_REVIEW,
reviewer, scope, limits și groups. Fără evaluare, păstrează NOT_ASSESSED.

- Fiecare grup declară id, label, scope, limits și axes. Separă grupurile project-quality
  și atlas-quality.
- Fiecare axă declară id, label, correctness, verification, clarity, score,
  weight, claimIds, evidenceIds, reason și limits.
- Câmpurile numerice sunt întregi 1–100. score este evaluarea ponderată rotunjită
  40/30/30 a correctness/verification/clarity. Ponderile axelor însumează 100 per grup,
  iar scorul grupului este media ponderată rotunjită.
- Citează afirmații revizuite și ID-uri de dovezi păstrate. O revizuire onestă numai
  din sursă poate avea evidenceIds gol.

Nu confunda axele de calitate cu urgența feedback-ului, procentele testelor trecute,
acoperirea codului sau completitudinea semantică automată. Declară limitele și explică
fiecare judecată. Nu copia scorul altui proiect.

<a id="complete-reader-artifact-and-knowledge"></a>
## Artefactul complet al cititorului și cunoștințe

Regăsește politica citată pentru domeniul cerut și păstrează digest-ul corpusului,
ID-urile întoarse și fiecare cerință obligatorie. Regăsirea oferă context unui revizor
ancorat în sursă; nu demonstrează executarea unui comportament al proiectului.
Faptele necunoscute rămân explicite. HTML-ul complet include toate secțiunile cerute,
CSS-ul canonic încorporat, SVG inline, fragmente exacte de sursă/guvernanță,
Markdown-ul declarat complet și corpurile originale XML/TAP/manifest-sursă.
Navigarea internă și plierea nativă trebuie să funcționeze când atlas.html este copiat
singur. Nu se deduce niciun scor; o zonă neevaluată rămâne NOT_ASSESSED.

Păstrează întreaga dovadă canonică a regăsirii și octeții compleți ai sursei cunoștințelor,
inclusiv înregistrările opționale, clasarea, citările și proveniența. Verificarea numai
a înregistrărilor obligatorii sau a hash-ului corpusului nu autentifică o dovadă editată.

<a id="maintenance-and-open-questions"></a>
## Mentenanță și întrebări deschise

| Întrebare sau lacună reproductibilă | Dovadă | Responsabil | Verificare următoare |
|---|---|---|---|
| UNKNOWN | UNKNOWN | UNKNOWN | Cea mai mică verificare necesară pentru clarificarea faptului |

Păstrează utilizarea stabilă la responsabilul sursei. Păstrează constatările
schimbătoare în feedback și analiza/dovezile generate în rădăcina artefactelor
selectată. Declară încrederea și incertitudinea rămasă fără a inventa un scor global
sau o extindere E2E.
