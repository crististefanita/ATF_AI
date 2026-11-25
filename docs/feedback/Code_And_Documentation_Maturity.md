<a id="code-governance-readable-testing-strategy-and-complete-romanian-edition"></a>
# Guvernanța codului, strategie de testare lizibilă și ediție română completă

Baza reviziei: main `3663419b34ec6adc96120b419738b77afe145e9e` și main_RO
`aebbbcfaaf32f91b87e86fdf7d7923e040202728`. J0 este inspectat numai în citire la
`96f50e5f277771ca9dfac3b7c959b47188afd0b0`; cele două modificări locale de feedback
rămân intacte. Revizia întărește guvernanța surselor, testele locale existente și
documentația. Nu adaugă fluxuri de produs, funcționalități runtime sau emulatoare.

<a id="g-017"></a>
<a id="g-017-existing-regressions-must-remain-executable-and-bounded"></a>
## G-017: Regresiile existente trebuie să rămână executabile și limitate

- Responsabil: validatorul AST existent al suitelor și ConsoleSignatureContractTest.
- Prioritate: 84; impact/risc/urgență 90/80/75.
- Calitatea inițială: 76; corectitudine/verificare/claritate 80/65/80.
- Reproducere: o adnotare menținută schimbată în `@Test(enabled=false)`
  rămâne numărată ca executabilă de inventarul vechi bazat pe tipul adnotării.
  Selecția XML singură nu protejează acest contract al sursei.
- Defect independent de stabilitate: un contract de consolă selectat apelează
  `worker.join()` fără limită. Un fir de jurnalizare blocat putea bloca poarta locală.
- Corecție: inspectează atributele adnotării TestNG prin AST-ul compilatorului existent.
  Atributul enabled omis și literalul true sunt acceptate; indirectarea eșuează explicit,
  fără a pretinde că parsarea rezolvă constante Java externe.
- Fixture-urile imbricate/manuale existente își păstrează limitele revizuite. Comentariile,
  șirurile, adnotările fără legătură și tipurile Test locale sunt controale pozitive.
- Limitează așteptarea și curățarea firului existent, propagă eșecul original
  și păstrează resetarea destinației de test în finally.
- Calitate livrată: 93; corectitudine/verificare/claritate 94/92/92.
- Stare: VERIFIED pentru selecția locală observată și contractul limitat al firului;
  dovezile native local-02 de mai jos definesc afirmația de execuție.

<a id="d-010"></a>
<a id="d-010-strategy-first-complete-translation-and-source-grounded-review"></a>
## D-010: Strategie întâi, traducere completă și revizie bazată pe surse

- Responsabil: regulile documentației, testele de componentă pentru lizibilitate și politica edițiilor.
- Prioritate: 88; impact/risc/urgență 90/85/85.
- Calitatea inițială: 68; corectitudine/verificare/claritate 65/60/80.
- Defect observat de utilizator: main_RO/docs/Testing.md era în engleză la începutul reviziei. Politica veche
  permite deliberat traducerea numai pentru README-uri; rezultatul ei verde de paritate
  certifică astfel o ediție română incompletă a documentației.
- Semnal inițial executat: noul control local al limbii identifică exact acea cale
  Testing și alte 26 de documente englezești înaintea traducerii. Dovada inițială
  este păstrată extern; reprezintă un defect reprodus, nu o rulare finală eșuată.
- Strategia testării: păstrează selecția, ariile dovezilor API, identitatea browserului,
  observațiile independente și limitele dovezilor în Testing.md. Mută cele zece
  blocuri originale de execuție neschimbate în Testing_Commands.md, în secțiuni
  pliate cu rezumate utile. Ancorele de intrare existente se păstrează.
- Lizibilitate: adoptă principiul J0 al deciziilor clare, distingând explicit rolurile.
  Strategia folosește limita de 3 propoziții/100 de cuvinte pe paragraf logic;
  referințele operaționale detaliate folosesc 100 de cuvinte și limita de 45 pe propoziție.
  Acestea declanșează revizia; nu sunt note, cote de diagrame sau lungimi de rând.
- Toate ghidurile operaționale primesc controalele de proză acceptate; înregistrările
  istorice își păstrează forma dovezilor. Blocurile Maven executabile rămân supuse
  guvernanței plierii în ambele zone. Comenzile inline de diagnostic rămân vizibile.
- Traducere: fiecare document Markdown menținut și proiecția HTML Living primesc
  un corespondent românesc la calea identică, inclusiv feedback-ul istoric.
  Codul, comenzile, datele de cunoștințe, identificatorii, stările și literalii
  canonici citați ai politicilor rămân exacți. Rapoartele generate originale își păstrează limba.
- Controalele deterministe identifică markerii românești absenți, blocurile englezești
  semnificative neschimbate, abaterile structurale/tehnice și amprentele de revizie expirate.
  Nu pot certifica fiecare propoziție tradusă sau detecta orice inversare de sens.
- Revizie asistată de RAG: păstrează dovada recuperării deterministe existente a politicilor,
  adaugă responsabilii reali ai edițiilor/politicilor și citește fiecare pereche EN/RO completă.
  Verifică condițiile, negațiile, cronologia, actorii, limitele dovezilor și legendele.
  XML-ul comun leagă fiecare document de ambele amprente normalizate și declară
  revizorul/dimensiunile. Actualizarea amprentelor fără lectură nu înseamnă acceptanță.
- Limita parserului: paritatea structurală cere încă etichete de legătură pe un
  singur rând și poziționare canonică în listele reașezate. Două corecții ale
  traducerilor păstrează forma acceptată; nu se pretinde un parser Markdown complet.
- Calitate livrată: 92; corectitudine/verificare/claritate 94/90/92.
- Stare: VERIFIED pentru inventarul documentelor menținute, revizuirea completă
  declarată, controalele native între ediții și domeniul desktop capturat de mai jos.

<a id="g-016"></a>
<a id="g-016-review-removal-of-an-existing-regression-method"></a>
## G-016: Revizuiește eliminarea unei metode de regresie existente

- Responsabil: politica de mentenanță a testelor și modulul testului modificat.
- Prioritate: 75; impact/risc/urgență 80/70/70.
- Calitate: 69; corectitudine/verificare/claritate 75/50/80.
- Observație: eliminarea unei metode dintr-o clasă încă selectată poate lăsa
  inventarul claselor neschimbat. Iterația nu introduce o poartă Git pentru
  schimbarea testelor și nu pretinde protecția eliminării metodelor.
- Referință J0: TestChangeGovernanceTest și GitTestChanges separă baza imuabilă,
  calea exactă modificată, raționamentul revizorului și dovada verificării. Păstrează
  principiul fără a copia cataloage de consumatori sau a inventa cereri de aprobare.
- Acceptanță: un control limitat viitor trebuie să reproducă eliminarea reală a unei metode,
  să distingă fixture-urile/refactorizările de pierderea dovezii, să acopere schimbările
  comise și locale, să respingă intrări expirate/străine și să ofere un indiciu de remediere.
- Stare: TODO. Inspecția surselor dovedește lacuna, nu o mutație executată.

<a id="comparable-source-review-scores"></a>
## Note comparabile ale reviziei surselor

Acestea sunt aprecieri 1–100 ale maturității surselor pe aceleași axe inspectate.
J0 nu a fost compilat sau executat; notele nu stabilesc repetabilitatea sau acoperirea.
Diagnosticele și limitele existente sunt evaluate independent de numărul testelor.

| Axă | Pondere | AI inițial | Sursa AI corectată | Sursa J0 inspectată |
|---|---:|---:|---:|---:|
| Limitele modulelor și responsabilitățile | 25% | 92 | 92 | 94 |
| Acoperirea guvernanței executabile | 25% | 88 | 90 | 92 |
| Căi negative și criterii independente de verificare | 20% | 90 | 91 | 94 |
| Așteptări limitate și curățare | 20% | 85 | 87 | 92 |
| Diagnostice utile pentru corecții | 10% | 94 | 94 | 88 |
| Evaluarea ponderată a surselor | 100% | 89.4 | 90.5 | 92.5 |

AI îmbunătățește selecția executabilă și un responsabil al concurenței limitate.
J0 păstrează controale mai puternice ale schimbărilor și agregării ciclului de viață;
AI păstrează observații emise mai bune, distincția cauză/remediere și reverificări exacte.
F-011 rămâne deschis. Notele mari cer dovezi mai puternice, nu afirmații mai largi nesusținute.

<a id="acceptance-evidence"></a>
## Dovezi de acceptanță

Rădăcina externă a reviziei este `${ATF_WORK_ROOT}/reviews/20261125-code-and-documentation-governance`.
Rulările native și manifestele surselor folosesc `${ATF_WORK_ROOT}/runs/` și reactoare
izolate separate. Publicația curentă ignorată existentă își păstrează aria capturată.

- Baza: baseline-translation-language.log consemnează documentul Testing realmente
  netradus înaintea corecției. Contraexemplele de lizibilitate păstrează proza densă
  a strategiei, comenzile deschise implicit și etichetele false din literali.
- Selecțiile finale implicite local-02 ale reactorului: engleză 690/690
  și română 690/690 trecute, cu 0 eșecuri,
  0 erori și 0 cazuri omise în fiecare ediție. Fiecare include 315 cazuri de guvernanță. Toate cele cinci module s-au executat;
  selecțiile atf-tests conțin 678 și 678
  cazuri. XML-urile Surefire originale păstrate, jurnalele Maven finalizate,
  manifestele sursei și înregistrările ieșirii definesc numărătorile, nu rezumatele copiate ale rapoartelor.
- Surse executate: engleză 1b939695c7a29355bf5e67f50ccbaa0121314c9e și română
  bfa1bcc341cd4540b8f5ef70457b8483695f0d6c. Rezumatul nativ local-02 finalizat leagă înregistrările
  local-02 finalizate și rezultatele reale ale cazurilor. 29 de cazuri
  API locale existente și cazul firului de consolă limitat au trecut în fiecare ediție;
  observațiile API locale nu stabilesc acceptanța aplicației GoRest.
- Controale Node de mentenanță existente: 182/182 trecute în fiecare ediție capturată la
  a064e9f18f3528c17e109f2119b81808c32ce2e8 și
  669c2baecd3552a71651283c9eb44021e264e3de. Fiecare manifest păstrat leagă 23 de fișiere:
  19 instrumente și 4 intrări de contract, neschimbate față de sursele native finale.
  Aceasta păstrează domeniul Node capturat; nu afirmă o execuție Node nouă la commit-ul final.
- Revizuire completă a documentelor: translation-review.xml comun leagă 51 de
  documente revizuite, cuprinzând 50 de căi Markdown menținute și perechea HTML Living.
  Dovada reală a regăsirii lexicale a politicii păstrează 16 înregistrări obligatorii
  și 18 selectate. Lectura EN/RO completă, bazată pe surse, acoperă limba, sensul,
  condițiile, faptele, legăturile, diagramele și plierile; nu se afirmă recuperare semantică.
- Revizuirea nativă desktop finală a documentelor combină visual02 cu remedierea
  Architecture delimitată din visual03: 32 de înregistrări, 56 de observații SVG și
  98 de capturi selectate la lățimi 1440/900. Hash-urile surselor capturate corespund;
  constatările geometrice, erorile de pagină/consolă, cererile externe și depășirea
  documentului sunt 0. Figurile Living late folosesc derulare conținută. Randarea
  dovedește componentele documentului, nu acceptanța aplicației în framework/browser
  sau accesibilitatea universală.
- Baza inițială netradusă și prima execuție completă rămân păstrate mai jos.
  Nu s-au adăugat scenarii E2E, emulatoare sau funcționalități runtime. G-016, G-014
  și F-011 rămân TODO; sincronizarea se dovedește separat prin identitățile reale ale
  ramurilor, remote și live după verificarea închiderii exclusiv documentare.

<a id="retained-first-complete-execution"></a>
## Prima execuție completă păstrată

Prima selecție completă implicită a reactorului trece în engleză. Modulul atf-tests
consemnează 678 de cazuri trecute. Rularea română consemnează 677 de cazuri trecute
și un control documentar pozitiv eșuat; niciun caz nu este omis. Controlul caută
un titlu HTML englezesc înainte de a insera un exemplu HTML codificat ca text, astfel că nu
modifică titlul românesc tradus integral.

Testul existent localizează acum ID-ul stabil al secțiunii retained-principles,
inserează același exemplu literal și păstrează aceeași aserțiune de proiecție.
Aserțiunile booleene ale precondițiilor evită afișarea întregului HTML la eșecul
inserării. Ambele rulări native originale rămân intacte la locațiile local-01.
Execuțiile local-02 acceptate, consemnate în dovezile de acceptanță, păstrează eșecul
anterior și stabilesc sursele și rezultatele proprii capturate separat.
