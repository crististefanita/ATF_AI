<a id="framework-maturity-and-readme-editions"></a>
# Maturitatea frameworkului și edițiile README

<a id="comparison-scope-and-rating-method"></a>
## Domeniul comparației și metoda de notare

Baza ATF_AI: `79fec10bc516ac1b32d19fe8afd99a4496382bd1`.
Referința ATF_J0: `287c7ae8c8906f669ea278c68d51e5f507a44ae7`, inclusiv
schimbările locale de documentație/guvernanță inspectate. J0 a fost doar citit;
schimbările sale în curs au fost păstrate, iar reactorul său nu a fost executat.
Starea referinței și inventarul fișierelor modificate sunt păstrate în dovezile externe ale revizuirii.

Notele sunt evaluări inginerești 1–100 ale unor preocupări comparabile ale
frameworkurilor. Nu sunt procente de acoperire, echivalență funcțională sau
disponibilitate măsurată. Ambele proiecte folosesc aceleași ponderi; numerele
de module/teste nu aduc puncte. Totalul ponderat este rotunjit la cel mai apropiat întreg, cu jumătățile în sus.

| Axă | Pondere | ATF_AI inițial | ATF_AI schimbări revizuite | ATF_J0 revizuire sursă |
|---|---:|---:|---:|---:|
| Arhitectură și limite | 20 | 90 | 90 | 92 |
| Guvernanță executabilă | 30 | 91 | 93 | 94 |
| Calitatea testelor existente | 25 | 89 | 90 | 88 |
| Documentație și navigare | 15 | 90 | 93 | 89 |
| Responsabilitatea și ciclul feedback-ului | 10 | 80 | 93 | 85 |
| Total ponderat | 100 | **89** | **92** | **90** |

Totalurile nerotunjite sunt 89.05, 91.65 și 90.45. Coloana livrării evaluează
schimbările sursă revizuite; dovezile execuției reale sunt înregistrate mai jos.
Nota J0 are încredere bazată doar pe revizuirea sursei; o diferență mică nu
trebuie interpretată drept benchmark runtime sau dovadă de fiabilitate superioară.

<a id="findings-and-selective-adoption"></a>
## Constatări și adoptare selectivă

| Preocupare | Comparație observată | Limită livrată |
|---|---|---|
| Arhitectură | AI are cinci module Maven și furnizori de browser independenți; J0 separă mai amplu bibliotecile de consumatori | Păstrează graful existent al modulelor AI și Core neutru față de furnizori; fără funcționalitate runtime sau modul nou |
| Structură README | J0 verifică mai profund Markdown semantic, blocurile pliabile și explicațiile apropiate diagramelor | Întărește controlul existent de documentație și cazurile sale cu eșec intenționat |
| Ediții lingvistice | J0 verifică configurația ediției și structura sursă/traducere | main rămâne în engleză; main_RO traduce fiecare README la aceeași cale, păstrând exemplele, legăturile, ancorele și topologia |
| Calitatea testelor existente | Ambele proiecte beneficiază de căi de eșec observate independent, nu de numărarea aserțiunilor | Cazurile de guvernanță existente resping acum fișiere lipsă, comenzi/legături/muchii schimbate, aliasuri lipsă și derivă a sursei non-README |
| Ciclul feedback-ului | AI păstra înregistrări vechi de îmbunătățire a fluxurilor de produs și acceptanțe retrase lângă lucrul la framework | Elimină aceste intrări și referințe din zona curentă de feedback; păstrează istoricul lor original în afara ei |

Responsabilii J0 inspectați includ DocumentationReadmeStructureGovernanceTest,
DocumentationDiagramGovernanceTest, DocumentationLifecycleGovernanceTest,
DocumentationLanguagePolicy, documentation-editions.mjs,
ConsumerSuiteCoverageGovernanceTest și ghidurile sale de bune practici pentru
documentație/cod. Principiile utile au fost adaptate la contractele TestNG/Java 11
existente ale AI. Cataloagele de module specifice J0, bugetele numerice de stil
și implementarea runtime Java 21 nu au fost copiate.

<a id="g-006--readme-governance-and-romanian-edition"></a>
## G-006 — guvernanța README și ediția română

- Responsabil: guvernanța documentației din atf-tests și docs/governance.
- Prioritate inițială: 84, din impact/risc/urgență 90/80/75.
- Calitate inițială: 78, din corectitudine/verificare/claritate 85/65/80.
- Reproduceri: un bloc details incomplet sau o diagramă neexplicată putea trece;
  nu exista o ediție README română declarată sau o verificare de identitate între ediții.
- Așteptat: setările de ediție malformate sau nesuportate eșuează în mod închis.
  Numai README-urile românești primesc excepția lingvistică; sursa/comentariile
  și celelalte documente păstrează politica engleză. Ambele ediții păstrează
  exact aceleași căi README, structură, exemple executabile, destinații ale legăturilor, ancore de intrare și cod.
- Stare: VERIFIED la 2026-11-24. Ambele ediții materializate au trecut suita locală
  completă cu verificarea explicită a perechii activă: câte 526 de cazuri,
  inclusiv 187 de guvernanță și 24 API ale frameworkului, fără eșecuri/erori/omiteri.
- Calitate livrată: 94, din corectitudine/verificare/claritate 94/94/94.
  Prioritatea și calitatea inițiale de mai sus rămân evaluări istorice ale acestei intrări.
- Toate cele 18 căi README, exemplele executabile, ancorele de intrare în engleză,
  destinațiile legăturilor și topologia Mermaid corespund. Sursa non-README
  este identică, exceptând unica setare de ediție declarată. Nu s-au introdus
  clase Java, schimbări de producție/POM, teste E2E sau emulatoare noi.

Procesul durabil aparține [edițiilor README](../governance/Readme_Editions.md)
și [politicii pentru cod/documentație](../governance/Code_And_Documentation_Policy.md).
Verificările stabilesc structura și identitatea suportate, nu calitatea semantică
automată a traducerii sau toate gramaticile posibile Markdown/Mermaid.
Revizuirea umană rămâne responsabilă pentru sens și lizibilitatea diagramelor.
Controalele de sursă nu înlocuiesc rezultatele regresiilor runtime.

<a id="evidence-ownership"></a>
## Responsabilitatea dovezilor

| Selecție | Revizie sursă | Rezultat | Numele rulării externe |
|---|---|---|---|
| Subsetul inițial de documentație/scanner | Sursă pregătită din baza înregistrată | 102 trecute | modules-maturity-governance-01 |
| Primele selecții complete engleză și română | c2463f3 / ba314ee | Fiecare a avut un eșec nou de paritate cauzat de rezultate Allure generate; păstrate drept dovezi nereușite | modules-maturity-main-local-01 / modules-maturity-ro-local-01 |
| Selecția completă engleză acceptată, cu pereche | c65db3e | 526 trecute; incluse 187 de guvernanță și 24 API | modules-maturity-main-local-02 |
| Selecția completă română acceptată, cu pereche | 059d6dc | 526 trecute; incluse 187 de guvernanță și 24 API | modules-maturity-ro-local-02 |

Verificarea inițială separată a perechii a expus și diferențe CRLF/LF în
gitignore și descriptorii SPI. Normalizarea se aplică acum acestor fișiere text
întreținute; o declarație de furnizor schimbată continuă să eșueze.
Eșecul suitei complete a expus rezultate Allure generate în afara src.
Inventarul comun corectat exclude directoarele de rezultate cunoscute,
păstrând resursele deținute cu nume identice. Cazurile existente de arhitectură și ediție demonstrează ambele părți ale acestei limite.

Revizuirea reală Mermaid a folosit Chromium desktop la 1440 și 520 px, fără
emulare de dispozitiv: 28 de înregistrări document/ediție/fereastră, nouă diagrame
per ediție, zero depășiri ale paginii și o înălțime minimă măsurată a etichetelor
de 17 px. Diagramele secundare mici din prima previzualizare au fost corectate
cu aranjamente verticale în ambele ediții; topologia aprobată a rădăcinii și
exemplele executabile rămân intacte. Previzualizările și metricile acceptate
sunt în diagram-review-02 sub rădăcina revizuirii; diagram-review-01 păstrează observația originală.

Rădăcina externă a revizuirii este `${ATF_WORK_ROOT}/reviews/20261124-maturity`.
Instantaneele noi, comenzile rezolvate exact, manifestele sursei și jurnalele
aparțin directoarelor validation/runs din rădăcina externă de lucru selectată.
Dovada API anterioară își păstrează domeniul original de 24 de cazuri; nu se
pretinde execuția scrierilor GoRest fără token furnizat. Dovezile finale trebuie
să păstreze pornirile nereușite și să distingă cazurile controlate de execuția aplicației reale.

Verificarea finală sursă/dovadă folosește selecția completă de guvernanță existentă
în fiecare ediție, cu perechea selectată explicit. Comenzile exacte, commiturile
finale și totalurile XML per modul sunt păstrate în
`${ATF_WORK_ROOT}/runs/maturity-final-evidence.json`; numerele se suprapun între rulări.
