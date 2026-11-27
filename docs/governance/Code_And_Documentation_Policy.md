<a id="code-and-documentation-policy"></a>
# Politica de cod și documentație

<a id="ownership-and-change-scope"></a>
## Responsabilitate și domeniul schimbării

Folosește [limitele modulelor](../Architecture.md) existente. Contractele Core
rămân independente de implementările browserelor și raportării. Cazurile de
produs și verificările între module rămân în atf-tests. Zonele exclusiv de
documentație nu au POM, sursă Java, raport generat sau furnizor runtime.

Întărește testele existente și cazurile lor utile înaintea adăugării unei noi
suprafețe de execuție. Nu restaura fațade istorice de skills, cataloage de rutare,
validatori Java MCP/Atlas sau infrastructură de teste generate prin lucrul la politici.

<a id="review-a-change-at-its-owner"></a>
## Revizuiește o schimbare la responsabilul său

Folosește această listă pentru contractul afectat, fără a o copia în fiecare
README de modul. Revizuiește împreună implementarea, POM-ul, testele existente
și ghidul responsabil înaintea interpretării unui TODO vechi sau raport de rulare.

- [ ] Schimbarea are un responsabil clar de modul/document și păstrează direcția
  dependențelor; conversiile specifice furnizorului rămân în adaptorul său.
- [ ] Comportamentul public, intrarea și eșecul sunt numite clar, cu un rezultat
  așteptat independent care ar respinge o implementare plauzibilă greșită.
- [ ] Așteptările și operațiile externe sunt limitate, eșecurile păstrează
  diagnosticele, iar starea schimbată este restaurată după succes sau eșec.
- [ ] Regresiile existente rămân accesibile prin selecțiile întreținute; orice
  excludere intenționată are responsabil și limită declarată a dovezii.
- [ ] Coordonatele, configurația, comenzile, exemplele și săgețile diagramelor
  corespund sursei curente, iar legăturile trimit la un singur responsabil canonic al contractului.
- [ ] Verificările și artefactele relevante au fost inspectate în domeniul lor
  real; feedback-ul păstrează eșecurile și închide numai criteriile de acceptanță demonstrate.

Acestea sunt întrebări de revizuire. Responsabilii executabili de mai jos impun
regulile suportate; un control de sursă verde nu stabilește fiecare punct din listă.

<a id="existing-test-quality"></a>
## Calitatea testelor existente

Cerința GOV-DIAG-001: constatările de guvernanță suportate TREBUIE să păstreze
dovada observată și să ofere sensul eșecului, indiciul de reparare, responsabilul
canonic și o selecție întreținută de reverificare. Urmează
[diagnosticele eșecurilor](Failure_Diagnostics.md) și separă dovada brută de
cauzele sugerate. Cazurile negative potrivesc tokenul regulii emise și detaliul
cerut; un prefix înrudit sau un cod menționat în conținut nu stabilește eșecul
așteptat. Constatările necunoscute rămân neclasificate.

- Precizează comportamentul protejat, rezultatul observabil și un caz util
  de eșec. Aserțiunile trebuie să distingă rezultatul intenționat de unul plauzibil greșit.
- Preferă starea observată și o valoare așteptată independentă. Potrivirea
  unui șir copiat, numărarea cuvintelor de aserțiune sau apelarea implementării
  de două ori nu stabilește corectitudinea comportamentală.
- Limitează procesele externe și așteptările de rețea/browser. Expirarea trebuie
  să identifice operația eșuată și să păstreze diagnostice utile.
- Restaurează proprietățile schimbate, starea motorului și listener-ele prin
  curățare care rulează și după eșec. Nu transforma o excepție neașteptată într-un test trecut.
- Anotările TestNG întreținute din sursă trebuie să omită `enabled` sau să
  folosească literalul `true` (parantezele sunt permise). Indicatorii dezactivați
  sau indirecți pică guvernanța sursei; cazurile imbricate/manuale revizuite păstrează limita explicită.
- Atributul `invocationCount` trebuie omis sau să fie un literal întreg pozitiv
  (parantezele sunt permise). Zero, valorile negative și expresiile/constantele
  indirecte sunt respinse cu `SUITE-INVOCATIONS`; controlul fără rezolvare semantică
  nu certifică valorile constantelor. Repară adnotarea, păstrând metoda și aserțiunile.
- Păstrează clasele semnificative de test existente accesibile prin selecțiile
  de suite întreținute. Distinge testele explicite de aplicație, cazurile
  controlate și probele de mediu de regresiile omise accidental.
- Păstrează identitățile ordonate ale metodelor TestNG executabile în
  [test-method-inventory.txt](../../atf-tests/src/test/resources/governance/test-method-inventory.txt).
  Controlul AST înregistrează tipul declarant, numele metodei și tipurile
  parametrilor, independent de selecția claselor din suită.
- `TEST-METHOD-REMOVED` identifică o metodă fixată care lipsește din sursă;
  `TEST-METHOD-UNREGISTERED` identifică o metodă din sursă absentă din inventarul
  revizuit. Ambele constatări trimit la testul existent de guvernanță și includ
  un indiciu de remediere.
- Pentru adăugarea, redenumirea sau retragerea intenționată a unei metode,
  revizuiește diferența de semnături și dovada afectată, apoi actualizează
  inventarul ordonat și amprenta SHA-256 din `TestMethodInventoryRules`.
  Aceasta este o bază de identități la nivel de sursă, nu o dovadă că metoda s-a
  executat sau că o schimbare Git a fost aprobată. Revizuiește inventarul și
  amprenta împreună cu testul responsabil.
- Sigiliul metodelor executabile folosește UTF-8 cu CRLF normalizat la LF. Conversia
  Git la checkout trebuie să păstreze amprenta; metadatele, spațiile și conținutul
  metodelor rămân protejate. Revizuiește sigiliul canonic cu responsabilul existent.
- Un control de sursă verifică arhitectura sau politica. Etichetează acel
  domeniu separat de execuția runtime și acceptanța controlată a aplicației.

<a id="executable-code-contracts"></a>
## Contracte de cod executabile

<a id="explicit-imports-and-visible-dependencies"></a>
### Importuri explicite și dependențe vizibile

Numește explicit fiecare tip Java și membru static importat. Importurile
wildcard normale și statice ascund responsabilitatea dependențelor și sunt
respinse de controlul existent de arhitectură. Exemplele literale și
comentariile nu sunt declarații de import. Păstrează locale referințele din
același pachet și elimină importurile redundante când schimbi fișierul afectat.
Atribuirea de către compilator și comparația arborilor de implementare sunt
utile curățării exclusive a importurilor; o scanare textuală trecută nu este compilare.

Folosește importuri statice numite când clarifică responsabilitatea. Nu muta
logica de afaceri într-un test sau renderer pentru a scurta apelul. Limitele
modulelor și verificările responsabilității SDK-urilor furnizorilor se aplică independent de stilul importurilor.

<a id="english-source-and-comments"></a>
### Sursă și comentarii în engleză

Cerința ENG-001: `authored identifiers, comments/Javadoc and framework/test messages MUST be written in English.`

Documentația engleză este canonică pe main. Ediția documentației main_RO traduce
toată proza întreținută a documentației și etichetele explicative ale diagramelor,
conform [contractului edițiilor README](Readme_Editions.md). Excepția explicită
nu schimbă cerința ca sursa și comentariile să fie în engleză.

Păstrează conținuturile externe, numele proprii și cazurile intenționate de
localizare/testare negativă drept date; nu traduce tacit un contract extern
pentru a satisface o verificare lingvistică.

Guvernanța surselor inspectează separat codul, comentariile și literalii după
traducerea Unicode Java. Respinge markerii românești cunoscuți din identificatori,
comentarii și mesaje proprii. Vocabularul finit completează revizia umană; nu
detectează universal limba. Fișierele exacte de politică lingvistică au responsabilitate
documentată pentru datele literale; identificatorii și comentariile rămân guvernate.
Excepțiile acoperă date revizuite de politică/test, fără a permite diagnostice
proprii în alte limbi. Excepțiile nefolosite eșuează; producția nu poate primi
scutiri generale pentru date de test. Cazurile externe noi necesită revizia explicită
a responsabilului.

Responsabilul executabil este
[JavaSourceScannerTest](../../atf-tests/src/test/java/com/ai/atf/governance/JavaSourceScannerTest.java),
inclusiv inspecția depozitului și cazurile de comentarii/nume/mesaje cu eșec intenționat.
Responsabilii testelor de documentație protejează și cerința obligatorie,
diagramele README și exemplele compilabile ale consumatorilor. Clasele sunt
enumerate explicit în suitele locale și agregate. Dintr-o copie izolată a
reactorului:

<details>
<summary>Rulează comenzile pentru sursă și comentarii în engleză</summary>

```powershell
mvn -B -ntp -pl atf-tests -am "-Dtest=JavaSourceScannerTest,DocumentationGovernanceTest,DocumentationRootGovernanceTest,DocumentationEditionGovernanceTest,LivingPrinciplesGovernanceTest,DocumentationStructureGovernanceTest,FeedbackGovernanceTest,DocumentationPublicationGovernanceTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

</details>

<a id="visibility-and-execution"></a>
### Vizibilitate și execuție

Inventarele trebuie să păstreze segmentele legale de pachet sursă precum
`target` și `artifacts`; numele directoarelor identifică rezultate de build
numai în afara arborilor sursă deținuți. Un control care ignoră tacit sursa
nu este o verificare de responsabilitate.

Pauzele fixe directe aparțin ApiSleeper, UiSleeper și AtfTestWaiter;
publicarea directă în consolă aparține ConsoleLogger. Restul codului folosește
fațada responsabilă. Tratarea InterruptedException restaurează indicatorul
firului curent. Inspecția structurală a acestor forme de apel completează
testele reale de întrerupere; nu demonstrează orice cale posibilă de control.
Contractul intenționat executor/future al unui API nu justifică o interdicție
textuală globală sau o excepție nouă nerevizuită.

<a id="reporting-scope"></a>
### Domeniul raportării

Extent este implementarea menținută a raportării. Opțiunea runtime reporting.engine acceptă extent; publicarea agregată acceptă extent sau dezactivare explicită. Dependențele Allure retrase, furnizorii, suitele active și rețetele nu pot reveni printr-o valoare implicită.

Controalele dependențelor/sursei și contractele comenzilor resping integrarea retrasă și valorile nesusținute ale motorului. Colectorii generici pot păstra formate istorice sau externe; aceasta nu reactivează raportarea framework-ului. Dovezile istorice își păstrează sursa, numerele și verdictele originale.

<a id="maintainability-and-exceptions"></a>
## Mentenabilitate și excepții

Revizuiește coeziunea pachetelor, metodele/clasele mari și creșterea resurselor
acolo unde are loc schimbarea. Măsurătorile sunt semnale pentru inspecția
responsabilului, nu note de calitate sau limite universale moștenite.

Când datoria existentă necesită o excepție, numește în feedback sursa sau
inventarul exact, responsabilul, problema observată, criteriul de reparare
și expirarea/declanșatorul revizuirii. Nu crește un prag doar pentru a accepta
creșterea. Elimină excepția când sursa dispare sau condiția de închidere/expirare
este îndeplinită; excepțiile învechite sau mai ample trebuie respinse la revizuire.

Compatibilitatea Java urmează contractul compilatorului din
[POM-ul părinte](../../pom.xml). Aliniază verificările dependențelor și sursei
la responsabilitatea declarată a modulelor și comportamentul real al
compilatorului; nu deduce suportul Java dintr-o căutare de nume de metodă.

<a id="documentation-and-evidence"></a>
## Documentație și dovezi

Pentru regresiile de concurență, sincronizează evenimentul observat printr-un
punct de întâlnire limitat, în locul unei pauze fixe care face probabilă
suprapunerea. Păstrează observabile eșecurile, identitățile ordonate și
curățarea deținută de finally. Testele de termen-limită își păstrează oracolele
de expirare; creșterea unui buget nu înlocuiește diagnosticarea unei presupuneri de planificare.

[Zona artefactelor](../../artifacts/README.md) deține păstrarea dovezilor și
protocolul reutilizabil Atlas pentru proiecte. Ghidurile/șabloanele întreținute
rămân sursă chiar într-un director artifacts. Numai subarborii declarați de
rulări raport/Atlas sunt date; excluderea lor nu trebuie să ascundă un pachet
sursă/resursă deținut cu același nume. Verificările CLI de mentenanță rulează
separat de suita Java și nu introduc dependență runtime de limbajul utilitarelor.

Păstrează un singur responsabil canonic pentru fiecare contract.
README-urile rădăcină/module orientează cititorul; ghidurile pachetelor explică
responsabilitățile locale; feedback-ul conține lacunele active. Leagă un
responsabil existent în locul întreținerii unor reguli paralele.

<a id="reader-orientation-and-diagrams"></a>
### Orientarea cititorului și diagrame

`DOC-ROOT-FOCUS-001: The root README routes project decisions to owner guides; keep translated branch names and URLs in the edition guide, including references in comments and encoded examples.`

README-ul principal explică scopul proiectului, compoziția și deciziile cititorului.
Păstrează numele și rutele ramurii traduse în [ghidul edițiilor](Readme_Editions.md),
inclusiv referințele plasate în comentarii, exemple sau linkuri codificate.
Aceeași structură a paginii principale se aplică ambelor ediții întreținute.

Primul ecran trebuie să răspundă ce face zona, cine o deține, ce rezultat
produce, cum se verifică și unde trebuie să continue cititorul. Folosește un
tabel scurt întrebare–responsabil când există mai multe intrări utile.
Rădăcina explică alcătuirea; ghidurile modulelor și pachetelor explică propriile contracte.

README-ul rădăcină este harta de orientare a framework-ului: explică separarea
dintre contracte, furnizori, raportare și verificare; rezumă cele cinci module
Maven; arată traseele verificărilor API/UI; apoi trimite cititorul la ghidul
responsabil. Rezumatul modulelor are două coloane. Exemplele compilabile pentru
consumatori Playwright/Selenium stau în README-urile adaptoarelor, lângă
configurația și inițializarea furnizorului.
Plasează harta dependențelor sub secțiunea despre design și diagrama execuției
API/UI sub secțiunea despre rularea verificării; guvernanța confirmă ambele poziții.

Cerința DOC-PRESENT-001: folosește o diagramă pentru o întrebare: dependență,
flux de execuție sau ciclu de viață. Fluxurile întreținute, inclusiv direcțiile explicite ale subgrafurilor, folosesc citire compactă
`LR`; diagramele de secvență, stare și clase își păstrează direcția semantică.
Simplifică etichetele sau separă întrebarea înainte de lărgirea diagramei. O hartă
verticală propusă necesită cerință cu responsabil, motiv și criteriu de acceptanță revizuit.

Cerința DOC-PRESENT-002: imediat după fiecare bloc Mermaid, pune un singur
`<details>` echilibrat, fără atribut open. Sumarul descriptiv Legendă introduce
explicația săgeților, responsabilităților, ramurilor și omisiunilor. Păstrează
scopul și precondițiile esențiale vizibile înaintea diagramei. Exemplele literale
și diagramele comentate sunt date; o legendă nevidă nu dovedește că explicația
corespunde sursei. Inspectează randarea reală.
Diagramele explică POM-ul sau implementarea; nu înlocuiesc aceste surse.

Folosește titluri Markdown semantice, tabele minimale, blocuri de cod tipizate
și etichete concise. Pune detaliile secundare în ghidul responsabil sau
într-un bloc `<details>` numit. Păstrează stilizarea rendererului în afara
Markdown-ului, fără fonturi locale sau CSS încorporat.

Cerința DOC-TABLE-001: fiecare celulă de proză dintr-un tabel Markdown normativ întreținut
TREBUIE să aibă cel mult 140 de caractere vizibile și 22 de cuvinte. Folosește
tabelele pentru corespondențe și comparații scurte; mută explicațiile în proza
alăturată sau într-un responsabil legat și separă tabelele care răspund la
întrebări diferite. Limita este un semnal determinist de revizuire, nu o
garanție de randare sau lizibilitate. Inspectează tabelul modificat la lățimea
sa randată reală. Păstrează distincte câmpuri precum Prioritate și Calitate,
chiar dacă formatul compact cere formulări mai atente.

Tabelele feedback-ului istoric își păstrează forma dovezii capturate; indexul
activ rămâne guvernat. Ghidurile scurte de pachet pot folosi liste sau tabele,
fără cotă artificială de diagrame. Limitele tabelelor nu stabilesc echilibrul
coloanelor: inspectează împărțirea textului, tăierea și proporțiile la 1440 și
900 CSS px într-un browser desktop; păstrează tabelele de referință legitim late
într-o zonă cu derulare locală.

Cerința DOC-REVIEW-001: fiecare document Markdown întreținut TREBUIE să aibă
un H1, să înceapă ierarhia titlurilor de la H1 și să coboare cel mult un nivel
odată. Revenirea la un nivel anterior este validă. Fiecare bloc de cod deschis
declară limba sau `text` pentru rezultat literal; `java compile` și
`properties defaults` își păstrează sensul executabil. Exemplele în blocuri
pot ilustra Markdown invalid fără a deveni structura documentului.

Guvernanța existentă a documentației aplică verificările de echilibru al
blocurilor pliabile, markup de prezentare și artefacte editoriale limitate
tuturor documentelor Markdown întreținute, nu numai README-urilor.
Tokenurile nerezolvate precum `@@CODE_0@@`, `ZXQ005ZQ` și resturile de
delimitare a cuvintelor produse de tokenizer aparțin intrărilor editoriale de
reparat, nu prozei vizibile sau etichetelor diagramelor. Exemplele literale
rămân date. Aceste modele finite completează editarea umană; nu pot certifica
orice problemă de ortografie, limbă sau semantică.

Fiecare diagramă are o explicație adiacentă a săgeților, responsabilităților
și omisiunilor. Revizuirea umană compară explicația cu responsabilitatea
codului/POM-ului și randarea reală. Păstrează o diagramă corectă și aprobată
în locul schimbării geometriei sale doar pentru un stil vizual preferat.

Controalele living LD-02/LD-03/LD-04 păstrează responsabilitatea UIActions, distincția așteptărilor Selenium/Playwright și limitele verdictului WaitUtils. Contraexemplele schimbă aceste roluri între rânduri. Numele sunt semnale limitate de consistență; revizuiește condițiile și negațiile față de sursă.

<a id="apply-the-documentation-review-in-practice"></a>
### Aplică revizuirea documentației în practică

| Afirmație schimbată | Inspectează sursa reală | Revizuire și dovadă necesare |
|---|---|---|
| O opțiune de browser sau raportare | Responsabilul opțiunii tipizate și `framework.properties` | Separă valorile implicite din sursă de suprascrierile apelantului; reverifică comanda documentată |
| O săgeată de dependență | POM-urile responsabile și limita publică | Explică dacă săgeata înseamnă dependență, invocare sau dovadă; randează graful schimbat |
| O validare eșuată | Aserțiunea existentă și XML/HTML originale | Păstrează așteptat/observat, rezultatul real, responsabilul și indiciul de reparare; indiciul nu este cauza observată |
| Un README tradus | Paragraful, tabelul, codul și diagrama canonice englezești | Păstrează literalii tehnici și cantitățile; revizuiește în română negațiile, condițiile, domeniul și limitele dovezilor |
| Un succes sau o notă istorică | Revizia sursă și dovada originale | Păstrează data și selecția; leagă utilizarea curentă de responsabilul canonic |

Folosește opțiunea reală `ui.engine` ca exemplu: numele ei rămâne identic în
ambele ediții README, în timp ce propoziția explicativă este tradusă.
O valoare implicită schimbată trebuie reconciliată cu responsabilul tipizat/al
sursei; o suprascriere JVM nu demonstrează schimbarea valorii implicite din
pachet. Un oracol Atlas `EXECUTED` trebuie să citeze fragmentele sursă reale
și subiectele executate; o etichetă corespunzătoare nu este un martor independent al aserțiunii.

Revizuiește dinspre sursă spre exterior: afirmație, implementare/POM, ghid
responsabil, contraexemplu protejat, verificare reală, apoi închiderea
feedback-ului. Rulează mai întâi cea mai mică selecție relevantă cu responsabil.
Păstrează cazul eșuat, repară defectul identificat și reverifică; nu relaxa
o regulă doar pentru obținerea unui raport verde.

<a id="portable-paths-and-executable-facts"></a>
### Căi portabile și date executabile

Căile și legăturile urmărite ale proiectului sunt relative la documentul
responsabil sau rădăcina reactorului. Locațiile externe folosesc o suprascriere
explicită furnizată de apelant sau un substituent declarat precum
`${ATF_WORK_ROOT}/runs/...`, conform [Dezvoltării](../Development.md#working-roles).
Nu publica în sursă, configurație sau documentație un disc al stației de lucru,
o partajare de rețea, URI de fișier local sau o rădăcină fixă a unui cont numit.
Intrările dinamice ale directorului utilizatorului și intrările runtime explicite
pot fi rezolvate la căi absolute pe calculatorul apelantului; derivă valorile
din rădăcina reactorului curent sau o suprascriere numită înaintea transmiterii către procesele derivate.

Coordonatele, căile, comenzile, profilurile, valorile implicite și intrările
publice documentate trebuie să corespundă POM-ului, resurselor și surselor
descrise. Întreține proză în engleză și diagrame ale căror săgeți explică
responsabilități reale. Exemplele literale sunt exemple, nu dovadă că există un comportament.

Exemplele Maven executabile folosesc selecții care se rezolvă față de POM-ul/
reactorul real. Blocurile de configurare marcate explicit `properties defaults`
descriu valori implicite verificate ale sursei; exemplele de suprascriere nu
pretind valori implicite. Exemplele publice complete ale consumatorilor,
marcate `java compile` în README-urile adaptoarelor de browser, se compilează
față de API-ul public curent cu Java 11 și procesarea anotărilor dezactivată.
README-ul rădăcină trimite cititorul la exemplele furnizorilor; exemplele
obișnuite parțiale/literale nu sunt executate ca teste.

<a id="contracts-gaps-and-execution-evidence"></a>
### Contracte, lacune și dovezi de execuție

Păstrează notele revizuirii și dovezile de execuție în afara documentației
stabile responsabile. Închiderea feedback-ului trebuie să identifice
reproducerea, responsabilul, rezultatul așteptat și dovada observabilă.
Păstrează revizia sursă și domeniul exact al rulării. Mutarea sursei, copierea
reușită, compilarea sau o suită locală verde nu demonstrează un scenariu de
aplicație controlat. Rezultatele istorice trebuie să-și păstreze domeniul
original. Promovează comportamentul reutilizabil în ghidul responsabil și
leagă înregistrarea închiderii, fără a copia în README un sumar de livrare
datat. Păstrează rulările eșuate când explică reproducerea sau limita unui succes ulterior.

Evaluările feedback-ului declară criteriul, domeniul afectat și referința
susținătoare de sursă/dovadă. Prioritatea și calitatea evaluează întrebări
diferite și urmează [grila feedback-ului](../feedback/README.md); numărul
testelor trecute nu stabilește singur acoperirea sau calitatea zonei.

<a id="selective-adoption-from-the-archive-and-j0"></a>
## Adoptare selectivă din arhivă și J0

Referințele arhivate de mai jos sunt căi Git la commitul `main_mcp`
`70a7279c2a9560d39e6bd2554cd2f96a88626692`; nu sunt căi curente în copia de
lucru sau skills active.

| Sursă arhivată | Decizie | Principiu păstrat |
|---|---|---|
| Politica pentru cod | Adaptează | Datorie cu responsabil, criterii de reparare și eliminarea excepțiilor învechite; bugetele, rădăcinile și permisiunile vechi rămân arhivate |
| README pentru cod | Adaptează | Coeziunea, compatibilitatea Java și responsabilitatea dependențelor aparțin revizuirii și testelor existente de guvernanță |
| Scoruri cu dovezi | Adaptează | Notele revizuirii necesită axe declarate, dovezi și calcul reproductibil; numărarea cuvintelor-cheie nu este un oracol |
| Limita completitudinii | Adoptă principiul | Succesul suitei și acoperirea produsului sunt afirmații diferite, cu dovezi separate de acceptanță |
| Testul densității aserțiunilor | Respinge ca oracol de calitate | Densitatea indiciilor de aserțiune nu poate înlocui inspecția aserțiunilor și a cazurilor negative semnificative |
| Pachete de execuție retrase | Nu restaura | Refolosește regulile utile ca documentație/guvernanță fără registrul, fațadele sau stiva runtime retrase |

<details>
<summary>Căi arhivate și proveniența exactă a regulilor</summary>

Politica pentru cod:

```text
src/main/resources/skills/codequality/contracts/code-quality.yml
```

README pentru cod:

```text
src/main/java/com/ai/skills/codequality/README.md
```

Scoruri cu dovezi:

```text
src/main/java/com/ai/skills/atlas/rules/scoring/AtlasScoreEvidenceTraceRule.java
AtlasScoreOverallCalculationRule.java
```

Limita completitudinii:

```text
src/main/java/com/ai/skills/atlas/rules/scoring/limits/AtlasSuiteCompletenessSeparationRule.java
```

Testul densității aserțiunilor:

```text
src/test/java/com/ai/skills/codequality/tests/AtfCodeQualityAssertionDensityContractTest.java
```

Pachete de execuție retrase:

```text
src/main/java/com/ai/core/skills
src/main/java/com/ai/skills
```

</details>

Referințele J0 susțin aceeași separare: `AtfPackageCohesionGovernanceTest`
folosește inventare revizuite și respinge excepțiile învechite;
`AtfSourceReadabilityGovernanceTest` verifică importurile duplicate și
păstrează vizibile căile legale ale pachetelor sursă. Controalele curente de
pauză/consolă/întrerupere adaptează principiul responsabilității din arhivă
la contractele documentate ale execuției ATF_AI, fără a pretinde o regulă
veche copiată literal. `DocumentedBuildContracts` verifică documentația
factuală de build; `DocumentationLifecycleGovernanceTest` ține istoricul
livrărilor în afara contractelor stabile. Adaptează principiile la cele cinci
module, TestNG și responsabilitatea acestui depozit. Cataloagele de spații de
lucru, regulile consumatorilor și aliasurile lingvistice J0 nu sunt contracte ATF_AI.
<a id="reader-oriented-guides"></a>
## Ghiduri orientate către cititor

Cerința DOC-READ-001: păstrează vizibilă decizia cititorului înaintea detaliilor
de implementare sau execuție. Testing.md deține strategia și dovezile;
[ghidul de comenzi](../Testing_Commands.md) legat deține rețetele reproductibile.

Cerința DOC-STRATEGY-001: Testing.md folosește diagrame compacte `flowchart LR`
pentru căile de execuție și secvența reviziei. Păstrează o întrebare per diagramă
și o legendă adiacentă care explică ramurile, sensul săgeților și responsabilitățile
omise. Cerința explicită pentru strategie nu schimbă diagramele aprobate ale altor
responsabili; o hartă arhitecturală grupată poate avea altă direcție semantică.

Cerința DOC-STRATEGY-002: sub ancora stabilă test-design-contracts, compară API,
Playwright și Selenium exact o dată într-un tabel cu patru coloane: direcție,
responsabil de proiectare cu legătură, oracol observabil și limita dovezii.
Identificatorii direcțiilor rămân identici în ambele ediții. Păstrează comparația în afara details. Controlul verifică
forma susținută a tabelului și prezența câmpurilor de responsabilitate/observație/limită;
nu poate certifica sensul lor. Revizuiește fiecare afirmație față de executor,
furnizori, suitele selectate și aserțiuni.

Păstrează matricele de versiuni, rezolvarea cache-ului și precedența configurației
în ghidul comenzilor. Strategia explică de ce o selecție oferă o anumită dovadă
și trimite la acele operații. Cazurile de componentă pentru lizibilitate resping
fluxuri verticale, direcții lipsă sau duplicate și comparații incomplete, apoi
exersează corecția reală cu diagnostice care au responsabil.

| Sarcina cititorului | Structură preferată | Ce trebuie să rămână explicit |
|---|---|---|
| Alegerea între selecții sau opțiuni | Tabel comparativ | Precondițiile și dovada oferită de fiecare alegere |
| Urmărirea lucrului ordonat sau a ciclului de viață | Pași numerotați sau diagramă | Direcție, actori, ramuri și legendă adiacentă |
| Inspectarea verificărilor sau constrângerilor paralele | Liste cu puncte | Condiții independente fără ascunderea excepțiilor |
| Copierea comenzilor de execuție | Details închis implicit, cu sumar descriptiv | Scop, precondiții și dovezi așteptate |
| Înțelegerea unui motiv sau a unei consecințe | Proză scurtă și coerentă | Condiții, negații și limite |

Fragmentele canonice recuperate rămân literali de politică citați în engleză
în ambele ediții; traduce explicația din jurul lor. Aceste alegeri se bazează
pe conținut, nu pe cote de diagrame/tabele/puncte. O diagramă explică relații
reale; legenda sa precizează sensul săgeților, responsabilitatea și omisiunile.
Nu ascunde o precondiție într-un bloc pliabil doar pentru scurtarea paginii.

Controlul de sursă măsoară blocurile logice de proză dincolo de împărțirea
fizică pe linii. Orientarea README-urilor și Testing.md permite cel mult
3 propoziții și 100 de cuvinte vizibile per paragraf. Referințele operaționale
detaliate folosesc limita de paragraf de 100 de cuvinte; toată proza inspectată
folosește limita de 45 de cuvinte per propoziție. Acestea sunt declanșatoare
ale revizuirii, nu note de lizibilitate sau dovezi că textul mai scurt este corect.

Listele, tabelele, codul literal și conținutul pliabil au roluri distincte
de lectură. Proza înregistrărilor istorice își păstrează forma de dovadă;
feedback/README rămâne ghid operațional. Datele sursei, legăturile, codul
și traducerea lor rămân supuse celorlalte controale. Revizuiește manual proza
din blocurile pliabile ca parte a revizuirii întregului document.

Cerința DOC-RUN-001: blocurile shell Maven executabile din documentația
întreținută TREBUIE să fie în details echilibrat, fără niciun atribut open.
Numește în sumar selecția sau scopul real. Păstrează vizibile strategia și
limitele dovezilor; reverificările de diagnostic inline rămân direct lizibile.
Cazurile literale de text/cod nu sunt rețete shell executabile. Controlul
inspectează taguri reale în afara blocurilor/comentariilor și recunoaște
invocări Maven/wrapper, nu un cuvânt-cheie din proză.

Reconcilierea feedback-ului urmează [responsabilul ciclului de viață](../feedback/README.md#recording-and-closing-feedback): un registru activ gol cere o declarație explicită, iar domeniul închis, extern sau retras își păstrează limitele reale ale dovezii. Un exemplu delimitat nu poate declara lista goală.

Inventarele sursei exclud directoarele generate înaintea inspectării descendenților. Aceeași politică păstrează numele de rezultate sub rădăcinile surselor/resurselor deținute. Rezolvă rădăcina canonică o singură dată per scanare, păstrează limita sa și califică rădăcini relative, cu spații și Unicode. Observațiile directoarelor verifică domeniul parcurgerii fără praguri temporale dependente de planificare.

Cititorul Atlas actual respectă [limita publicului](../../atf-atlas/Report_Contract.md#reader-and-audit-responsibilities):
explicații business numerotate, cazuri și limite în HTML; datele tehnice de construire
în auditul adiacent. Controalele [cititorului](../../tools/atlas-reader.test.mjs)
completează verificările de sursă, fără a certifica automat sensul propozițiilor.
