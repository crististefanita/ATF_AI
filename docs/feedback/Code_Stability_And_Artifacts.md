<a id="code-governance-stable-regressions-and-artifact-ownership"></a>
# Guvernanța codului, regresii stabile și responsabilitatea artefactelor

Bază: `fb39f9cf35a815aff5dee7a237b0970b77105ebb`. Această etapă schimbă
guvernanța, regresiile existente ale frameworkului, utilitarele de mentenanță
și documentația. Nu adaugă scenarii E2E, emulatoare sau funcționalități Java runtime.
Ediția română folosește main_RO la cererea explicită a responsabilului.

<a id="g-007--explicit-java-import-ownership"></a>
## G-007 — responsabilitate explicită pentru importurile Java

- Responsabil: guvernanța arhitecturii existente și pachetele sursă afectate.
- Prioritate: 80, din impact/risc/urgență 85/75/75.
- Calitate inițială: 78, din corectitudine/verificare/claritate 85/70/75.
- Observație: importurile wildcard ascund tipurile concrete și membrii statici
  folosiți de clasele runtime/de test; verificările de duplicate și module nu le interzic.
- Acceptanță: importurile explicite se compilează cu corpuri de implementare
  identice. Guvernanța existentă respinge declarațiile wildcard normale/statice,
  inclusiv variante cu spații și Unicode, ignorând exemplele literale/comentariile.
- Stare: VERIFIED la 2026-11-25. Toți cei 17 arbori de implementare schimbați
  doar prin importuri sunt identici, iar selecțiile complete locale/agregate se
  compilează și trec. Patru variante wildcard și controalele de literali/comentarii
  sunt protejate de cazurile existente de arhitectură. Nu a fost necesară schimbarea comportamentului runtime.
- Calitate livrată: 95, din corectitudine/verificare/claritate 96/94/95.

<a id="t-005--deterministic-worker-limit-regression"></a>
## T-005 — regresie deterministă a limitei lucrătorilor

- Responsabil: AggregateParallelCollectionContractTest din atf-tests.
- Prioritate: 81, din impact/risc/urgență 85/80/70.
- Calitate inițială: 70, din corectitudine/verificare/claritate 80/55/70.
- Observație: verificarea concurenței existentă folosește o pauză de 75 ms
  pentru a face probabilă suprapunerea lucrătorilor. Un lucrător întârziat poate invalida presupunerea.
- Acceptanță: folosește o sincronizare explicită, limitată; păstrează plafonul
  concurenței, cere rezultate reale reușite și păstrează curățarea în finally.
  Repetă selecția existentă fără a adăuga un test de produs/E2E.
- Stare: VERIFIED la 2026-11-25. Metoda existentă folosește o sincronizare
  limitată între doi lucrători, cere plafonul exact, rezultatele reușite ale
  lucrătorilor și ordinea declarată a rezultatelor. Douăzeci de invocări directe
  consecutive și toate selecțiile acceptate ale reactorului au trecut.
- Calitate livrată: 95, din corectitudine/verificare/claritate 95/96/95.

<a id="d-003--governed-artifacts-and-reusable-atlas-intake"></a>
## D-003 — artefacte guvernate și intrare Atlas reutilizabilă

- Responsabil: documentația artefactelor, utilitarele de mentenanță și guvernanța existentă.
- Prioritate: 85, din impact/risc/urgență 90/85/70.
- Calitate inițială: 55, din corectitudine/verificare/claritate 65/40/55.
- Observație: artifacts este exclus global din inventar, astfel că documentația
  întreținută acolo ar evita verificările de legături, limbă și ediție. Dovezile
  rapoartelor sunt dispersate între directoarele target ale modulelor și rulările externe.
- Acceptanță: guvernează README-urile/șabloanele artefactelor; exclude numai
  datele de rulare declarate din afara arborilor sursă deținuți. Arhivează
  rapoartele și resursele existente fără a le schimba conținutul, păstrând
  clasificarea, proveniența și SHA-256. Colectorul trebuie să respingă
  suprascrierile și destinațiile recursive, să raporteze eșecurile parțiale
  și să distingă colectarea de execuție. Intrarea Atlas trebuie să descrie
  orice proiect selectat de apelant folosind surse observate și necunoscute
  explicite, fără analizor runtime nou sau restaurarea infrastructurii retrase.
- Stare: VERIFIED la 2026-11-25. Ghidurile/șabloanele întreținute ale artefactelor
  sunt guvernate; subarborii exacți ai rulărilor generate rămân excluși în afara
  sursei deținute. Opt regresii reale de sistem de fișiere/CLI au trecut;
  pachetele curente și istorice au fost arhivate și reverificate prin hash.
  Protocolul generic documentat de intrare Atlas a fost aplicat ATF cu
  referințe sursă și limite explicite ale dovezilor.
- Calitate livrată: 94, din corectitudine/verificare/claritate 94/95/93.
  Pachetele generate rămân în afara sursei urmărite.

Practicile J0 utile sunt importurile explicite, curățarea cu responsabil,
separarea sursei de rezultate, rezultatele observabile ale regresiilor și un
singur responsabil canonic al documentației. J0 rămâne o referință doar pentru
citire; această lucrare nu modifică niciun build J0 sau schimbare în curs.

<a id="current-comparison-and-adopted-practices"></a>
## Comparația curentă și practicile adoptate

ATF_J0 a fost inspectat din nou la 96f50e5f277771ca9dfac3b7c959b47188afd0b0.
Copia sa de lucru era curată la capturare; nu s-a efectuat build sau modificare J0.
Comparația anterioară păstrează revizia originală și dovezile stării modificate.
Această revizuire folosește ghidurile curente de bune practici de cod/documentație
și responsabilii guvernanței sursei ca referințe, adaptând responsabilitățile lor
la structura Java 11/TestNG existentă a AI. Îndrumarea J0 pentru importuri statice
numite informează regula importurilor explicite; distincția sa contract/lacună/dovadă informează responsabilitatea artefactelor.

Se aplică aceeași grilă 1–100 și aceleași ponderi ca în revizuirea anterioară
a maturității. Acestea sunt evaluări inginerești, nu acoperire, disponibilitate sau benchmark runtime.

| Axă | Pondere | ATF_AI anterior | ATF_AI curent | ATF_J0 revizuire sursă |
|---|---:|---:|---:|---:|
| Arhitectură și limite | 20 | 90 | 90 | 92 |
| Guvernanță executabilă | 30 | 93 | 94 | 94 |
| Calitatea testelor existente | 25 | 90 | 91 | 88 |
| Documentație și navigare | 15 | 93 | 94 | 89 |
| Responsabilitatea și ciclul feedback-ului | 10 | 93 | 93 | 85 |
| Total ponderat | 100 | **92** | **92** | **90** |

Totalurile curente nerotunjite sunt 92.35 pentru AI și 90.45 pentru J0.
Nota AI rotunjită rămâne 92, deoarece aceasta este o îmbunătățire focalizată
într-o evaluare mai largă a frameworkului. Importurile guvernate și sursele
artefactelor cu responsabil îmbunătățesc limitele impuse; sincronizarea limitată
și rezultatele verificate ale lucrătorilor îmbunătățesc o regresie existentă.
Noile ghiduri ale artefactelor, navigarea pe categorii și intrarea Atlas
îmbunătățesc înțelegerea. Nu se acordă puncte pentru numărul de fișiere/teste
sau ștergerea unui domeniu. J0 păstrează încrederea bazată doar pe revizuirea
sursei. Nicio notă nu certifică proiecte arbitrare ale consumatorilor, toate
browserele suportate sau persistența aplicației publice.

<a id="delivered-evidence-and-proof-limits"></a>
## Dovezi livrate și limitele verificării

| Selecție | Revizie sursă | Rezultat observat | Numele dovezii |
|---|---|---|---|
| Prima probă focalizată de guvernanță/concurență | Sursă pregătită din fb39f9cf | 200 trecute, un eșec nou al cazului de portabilitate; păstrat drept dovadă nereușită | modules-quality-probe-01 |
| Selecția locală completă engleză cu pereche explicită | 1055ca3 | 533 trecute, inclusiv 194 cazuri de guvernanță și 24 API ale frameworkului | modules-quality-main-local-01 |
| Selecția locală completă română cu pereche explicită | 222b5ba | 533 trecute, inclusiv același domeniu de guvernanță/API | modules-quality-ro-local-01 |
| Profilul agregat complet cu pereche explicită | 1055ca3 | 533 trecute; rezultatele agregate Extent/Allure existente sunt păstrate | modules-quality-main-aggregate-01 |
| Colectorul de mentenanță | Aceeași sursă a colectorului ca la revizia acceptată | Opt verificări de sistem de fișiere/CLI trecute | quality-artifact-tooling-final.log |
| Metoda existentă de limitare a lucrătorilor | Aceeași sursă Java ca la revizia acceptată | 20 de invocări directe consecutive trecute | quality-concurrency-repeat.json |
| Domeniul exclusiv de importuri | fb39f9cf comparat cu revizia acceptată | Toți cei 17 arbori de implementare identici | quality-import-audit.json |

Cazul negativ inițial de portabilitate construia o cale completă de disc din
constante adiacente, astfel încât guvernanța sursei a identificat-o corect chiar
în test. Cazul corectat folosește modelul explicit existent de construire a
datelor de test; regula căilor și comportamentul runtime nu au fost relaxate.
XML-urile și jurnalele originale nereușite sunt păstrate. Numerele se suprapun între selecții.

Pachetele de rapoarte sunt sub `${ATF_WORK_ROOT}/artifacts/reports/runs`, grupate
după numele rulării sursă. Colectorul păstrează arborii relativi la sursă și
octeții originali, generează cataloage/indexuri pe categorii și reverifică
fiecare SHA-256 copiat. Colectarea este distinctă de execuție. Pachetul istoric
de browser modules-f008-browser-accepted păstrat își menține revizia sursă și
rezultatul originale; nu este o nouă selecție de browser în această etapă.
Nu se pretinde cerere GoRest de scriere autentificată sau acceptanță de produs.

Exemplul Atlas aplicat este sub
`${ATF_WORK_ROOT}/artifacts/atlas/runs/atf-ai/quality-01`, cu o hartă a proiectului,
index sursă și limite deschise ale dovezilor. Demonstrează protocolul întreținut
de intrare pentru ATF; nu stabilește un analizor universal automat. Contractul
reutilizabil rămâne în artifacts/atlas/README.md și în șablonul său în engleză.

Toate cele 21 de perechi README păstrează ierarhia, comenzile, blocurile de cod,
destinațiile legăturilor, ancorele de intrare și topologia Mermaid. Restul sursei
este identic, exceptând proprietatea declarată a limbii README. Chromium desktop
a randat 36 de combinații document/ediție/lățime la 1440/520 px, fără depășirea
paginii și cu înălțimea minimă măsurată a etichetelor de 17 px. Nu s-a folosit emulare de dispozitiv.

Manifestele detaliate, hash-urile sursei, copiile de referință și metricile vizuale
sunt sub `${ATF_WORK_ROOT}/reviews/20261124-quality-artifacts`. Schimbările finale
exclusiv de dovadă primesc o validare nouă sursă/documentație în pereche, folosind
același Java compilat ca selecția completă acceptată. Verificarea nu este o nouă
rulare a tuturor cazurilor controlate. Sursa completă API/runtime și implementarea
schimbată doar prin importuri rămân sursa acceptată. Dovezile finale de audit și sincronizare rămân externe.
