<a id="governance-and-existing-test-review"></a>
# Revizuirea guvernanței și a testelor existente

Revizia de bază a revizuirii: `d35e69d`. Referință: spațiul sursă atf_j0 curent
și arhiva `main_mcp` la `70a7279`. Notele și evaluarea comparativă aparțin
revizuirii externe din ${ATF_WORK_ROOT}/reviews, nu ghidurilor stabile ale responsabililor.
Schimbările permise privesc documentația și fișierele existente de teste/guvernanță;
Java de producție și graful dependențelor Maven cu cinci module rămân neschimbate.

<a id="g-001"></a>
<a id="g-001-documentation-and-feedback-contracts"></a>
## G-001: Contractele documentației și feedback-ului

- Responsabil: atf-tests, DocumentationGovernanceTest și
  MarkdownDocumentationRules existente; ghidurile responsabile din docs.
- Observat: ștergerea întregului arbore de feedback putea lăsa validarea
  documentației verde. Secțiunile README responsabile și afirmațiile concrete
  despre build nu erau impuse. Constatările închise păstrau defecte la prezent și formulări TODO.
- Așteptat: documente obligatorii pentru responsabili, index și feedback,
  date din POM-ul curent, înregistrări active acționabile și ID-uri unice.
  Dovezile închise își păstrează domeniul istoric real. Arhitectura vie esențială
  rămâne sincronizată în Markdown și HTML, cu hash al sursei, hartă a secțiunilor
  și navigare verificată. Guvernanța proiectului rămâne în documentația responsabilă;
  AGENTS.md este eliminat din main, iar reintroducerea sa este respinsă de un test existent.
- Reproducere: elimină docs/feedback/README.md; schimbă coordonatele părintelui
  sau ținta compilatorului documentate; duplică un ID activ; păstrează o copie HTML învechită.
- Acceptanță: validatorul de documentație existent respinge aceste cazuri și
  acceptă ghidurile întreținute, exemplele literale și textul Windows normalizat.
- Stare: VERIFIED la 2026-11-24. Toate cele 24 de cazuri de documentație au trecut
  în setul final de 74 de cazuri de guvernanță; ghidul pereche a trecut și revizuirea vizuală.

<a id="t-001"></a>
<a id="t-001-observable-bounded-existing-regressions"></a>
## T-001: Regresii existente observabile și limitate

- Responsabil: atf-tests, StepLoggerContractTest, FailureAttachmentRegistryTest,
  ConfigManagerFileContractTest, FinalStateAttachmentRegistryTest,
  AtfCoreAggregatePublishedManifestContractTest, UiCoreConfigOverrideDisciplineTest
  și clasele existente de guvernanță pentru scanner și arhitectura frameworkului.
- Observat: așteptările nelimitate între fire puteau bloca acceptanța; aserțiunile
  fără efect și rezultatele TestNG imbricate neverificate puteau trece fals.
  Testul manifestului compara rezultatul cu el însuși și lipsea din toate suitele întreținute.
- Așteptat: efecte ordonate observabile, totaluri exacte ale rezultatelor,
  responsabilitate și curățare limitate pentru lucrători, restaurarea exactă
  a proprietăților de sistem anterioare, numere explicite în manifest și
  înregistrare în controalele locale/agregate întreținute.
- Acceptanță: rulează cele patru clase existente, repetă izolarea configurației
  cu valori de proprietăți preexistente și treci cazurile negative ale catalogului suitelor.
  Aplicațiile live și cazurile cu eșec intenționat rămân excepții explicite;
  excepțiile învechite și selectarea accidentală a suitelor trebuie să pice guvernanța.
- Stare: VERIFIED la 2026-11-24. Reactorul complet a trecut 393 de cazuri;
  izolarea configurației a trecut și patru cazuri cu valori preexistente.
  Marcajele statice de restaurare sunt descrise drept convenție de sursă,
  nu dovadă a executării curățării. Testele de distribuire prin registre nu mai sugerează integrarea backendului.

<a id="p-001"></a>
<a id="p-001-recovered-policies-and-documentation-atlas"></a>
## P-001: Politici recuperate și Atlas de documentație

- Responsabil: docs/governance și zona atf-atlas exclusiv de documentație,
  verificate prin controalele de documentație/arhitectură existente în atf-tests.
- Observat: reguli utile din arhivă pentru lizibilitatea codului, responsabilitate
  și dovezi erau cuplate la stiva retrasă de skills/generare Atlas. Vechiul Atlas
  necesită dependențe Java ample, eliminate, și căi de sursă monolitice.
- Decizie: recuperează principiile și contractele esențiale de arhitectură în engleză;
  întreține un Atlas separat pentru sursă/teste/dovezi. Nu restaura skills runtime,
  integrarea MCP, rapoarte istorice generate sau un al șaselea modul Maven runtime.
- Acceptanță: legăturile către sursa și suitele curente se rezolvă, dovezile
  locale/de browser/de produs rămân distincte, Atlasul nu conține Java sau POM,
  iar arhitectura vie este prezentă și aliniată în HTML și Markdown.
- Stare: VERIFIED la 2026-11-24. Atlasul este exclusiv de documentație; perechea
  recuperată are 34 de titluri corespunzătoare, șapte diagrame statice și niciun script.
  Legăturile, metadatele, limitele sursei și randarea reală desktop/mobil au trecut.

<a id="verification-receipt"></a>
## Dovada verificării

Revizia sursă: `9a0e95d8a5d768787fff1f7436a70040c5bc6757`.
Rădăcina dovezilor: ${ATF_WORK_ROOT}/runs. Rulările de mai jos folosesc copii
de validare separate; numerele se suprapun și nu constituie un total unic.

| Selecție | Rezultat | Dovadă relativă la rădăcină |
|---|---|---|
| `mvn -B -ntp verify` | 382 de cazuri locale/de guvernanță și 11 native; 393 trecute, zero eșecuri/erori/omiteri | modules-review-local-01.log |
| `mvn -B -ntp -Paggregate test` | 393 trecute; rezultate Extent publicate și 383 de rezultate Allure trecute, inclusiv sumarul | modules-review-aggregate-01.log |
| ConfigManagerFileContractTest existent cu `ui.engine=selenium` și `atf.config.fixture=sentinel` | 4 trecute, zero eșecuri/erori/omiteri | modules-review-config-01.log |
| Inspecția prezentării Extent publicate | Zero încălcări | governance-review-aggregate-overview.log |
| Auditul limitelor sursei | Fără Java adăugat, schimbări de producție/resurse/POM sau AGENTS.md; doar două linii HTML exclusiv cu spații diferă față de instantaneul testat | governance-review-source-audit-9a0e95d.json |

Cele 74 de cazuri de guvernanță includ 32 de arhitectură/suite, 24 de documentație
și 18 de scanner. Rulările focalizate anterioare sunt păstrate ca diagnostic;
reactorul complet final validează sursele finale ale testelor. Această etapă
nu pretinde o execuție nouă a browserelor sau a reactorului J0.

Hash-ul Markdown normalizat este
`c48007d48728740e8e9b738d99f3183774296b73606671f174cb919802ab96cf`.
Dovezile statice și de randare reală a ghidului sunt în
${ATF_WORK_ROOT}/tools/living-guide-preview-20261124, inclusiv final-static-audit.json
și final-render/visual-qa.txt cu capturi desktop/mobil. Notele revizuirii și
grila comparativă explicită rămân în afara documentelor stabile responsabile.

La această revizie istorică, matricea browserelor desktop încă necesita verificare
separată. [Registrul curent](README.md) înregistrează dovezile F-008 ulterioare.
