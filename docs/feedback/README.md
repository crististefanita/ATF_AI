<a id="framework-feedback-and-todos"></a>
# Feedback și TODO-uri pentru framework

Urmărește lacunele reproductibile din execuția UI/API, raportare, configurație și
documentație. Utilizarea stabilă aparține README-ului responsabil. Compilările produc
jurnale și rapoarte în zona externă; copiile selectate și verificate pot fi publicate
în arhiva locală aleasă, conform [ghidului de raportare](../../artifacts/reports/README.md).

<a id="active-todos"></a>
## TODO-uri active

| ID | Constatare | Responsabil | Prioritate (1-100) | Calitate (1-100) | Stare | Dovada închiderii |
|---|---|---|---:|---:|---|---|
| D-011 | Guvernează structura lecturii Atlas, legendele și echilibrul măsurat al tabelelor | Proprietarii documentației Atlas și ai instrumentelor de mentenanță | 88 | 58 | IN PROGRESS | [Defecte reproduse și acceptanță delimitată](Atlas_Depth_And_Assessment.md#d-011) |
| G-014 | Păstrează eșecul de control al schimbărilor J1 separat de consistența Atlas și de rezultatul reușit al catalogului în aria verificată | Responsabilul schimbărilor testelor J1; observație externă | 84 | 68 | TODO | [Sursa exactă și înregistrările de control necesare](Atlas_Depth_And_Assessment.md#g-014) |
| G-016 | Revizuiește pierderea unei metode de regresie existente dintr-o clasă încă selectată | Responsabilul mentenanței testelor | 75 | 69 | TODO | [Lacuna observată în surse și acceptanța limitată](Code_And_Documentation_Maturity.md#g-016) |
| F-011 | Păstrează observabilă curățarea urmăririi interacțiunilor când închiderea motorului UI aruncă o excepție | Responsabilul închiderii UI din Core | 82 | 66 | TODO | [Lipsa observată în surse și criteriul de componentă absent](Behavior_And_Negative_Cases.md#f-011) |

Registrul este rezervat guvernanței, stabilității și documentației framework-ului.
Înregistrările despre îmbunătățirea fluxurilor de produs sunt excluse din această listă.

<a id="current-maintenance-scope"></a>
## Limitele mentenanței curente

Prioritizează maturitatea, stabilitatea, arhitectura lizibilă, documentația în engleză
cu diagrame, guvernanța și calitatea verificărilor existente. Păstrează inventarul
E2E curent; nu adăuga scenarii de aplicație în această etapă. Folosește browsere desktop
pentru verificările păstrate și nu utiliza emulatoare.
Notele trebuie să urmeze dovezile și criteriile de mai jos. Eliminarea unei cerințe
din aria lucrării nu reprezintă o îmbunătățire de calitate sau un test reușit.

<a id="assessment-rubric-and-evidence-locations"></a>
## Criterii de evaluare și locațiile dovezilor

Cele două note răspund unor întrebări diferite. Prioritatea crește odată cu urgența
remedierii; calitatea crește odată cu soliditatea zonei afectate.
Ambele sunt numere întregi între 1 și 100. Reprezintă evaluări bazate pe dovezi,
nu procente de acoperire a testelor sau probabilități.

- Prioritate = rotunjire(0.50 × impact + 0.30 × expunere/risc + 0.20 × urgență).
- Calitate = rotunjire(0.40 × corectitudine + 0.30 × verificare + 0.30 × claritate).

Fiecare componentă folosește aceeași scară 1–100. Folosește 1–39 pentru slab/scăzut,
40–69 pentru parțial/moderat, 70–89 pentru solid/ridicat și 90–100 pentru riguros
demonstrat/critic, în funcție de axa relevantă. Rotunjește la cel mai apropiat întreg,
cu jumătățile în sus. Precizează aria, observațiile și componentele notei în
înregistrarea indicată. Un element închis își păstrează acolo evaluarea inițială;
înregistrările istorice verificate nu primesc note retrospective inventate.

Revizuiește notele când se schimbă dovezile sau aria, nu doar când crește numărul testelor.
Guvernanța existentă a documentației validează cele două coloane distincte,
intervalul întreg și câmpurile acționabile pentru responsabil, stare și dovezi.

Componentele păstrate ale evaluărilor inițiale:

| ID | Impact / risc / urgență | Corectitudine / verificare / claritate | Limitele dovezilor |
|---|---|---|---|
| D-011 | 90 / 85 / 85 | 65 / 45 / 60 | Lățimi HTML generice, blocuri sursă literale și absența verificării lizibilității măsurate |
| G-017 | 90 / 80 / 75 | 80 / 65 / 80 | Teste selectate dezactivate în surse și un fir de consolă nelimitat |
| D-010 | 90 / 85 / 85 | 65 / 60 / 80 | Documentația din afara README-urilor rămâne engleză în ediția declarată română |
| G-016 | 80 / 70 / 70 | 75 / 50 / 80 | Eliminarea unei metode poate păstra selecția clasei; observație numai din surse |
| G-006 | 90 / 80 / 75 | 85 / 65 / 80 | Verificările de structură și de ediție lipseau din versiunea inițială analizată |
| F-008 | 90 / 70 / 75 | 70 / 70 / 75 | Verificările reale de rețea locală trec; selecția SDK/browser/CDP tipizat era nealiniată |
| G-005 | 85 / 80 / 75 | 70 / 40 / 55 | Rădăcinile de execuție sunt rezolvate dinamic, dar instrucțiunile conțin căi specifice stației |
| D-001 | 65 / 40 / 50 | 90 / 80 / 65 | Exemplele API existente compilează; navigarea, legendele și ghidajul de revizuire necesită rafinare |
| T-004 | 75 / 60 / 70 | 85 / 65 / 60 | Limitele întregii metode pot masca aserțiunile în timpul închiderii procesului de browser |
| G-012 | 95 / 90 / 90 | 65 / 55 / 60 | Hash-urile sursă identice admiteau un caz executat fără legătură; confirmarea opțională a recuperării cunoștințelor și asocierile locale ale diagramelor erau incomplete |
| G-013 | 90 / 85 / 85 | 65 / 55 / 60 | Fișierele indexate și puținele fluxuri nu declarau structurat responsabilii și perspectivele revizuirii |
| D-006 | 90 / 80 / 85 | 65 / 60 / 55 | Livrarea HTML completă explica încă doar comportamente selectate AI/J1 |
| A-001 | 85 / 75 / 80 | 60 / 45 / 50 | Axele cerute pentru proiect/Atlas nu aveau un model comun de evaluare susținut de dovezi |
| G-014 | 85 / 85 / 80 | 65 / 50 / 90 | Arborele J1 capturat, cu modificări locale necomise, nu are controale eligibile pentru două schimbări de teste de guvernanță; verificarea existentă eșuează corect |
| G-015 | 95 / 90 / 90 | 65 / 55 / 65 | Identitățile valide ale capetelor admiteau căi inverse, observații izolate și un alias de implementare drept criteriu independent |
| D-007 | 90 / 80 / 85 | 65 / 55 / 55 | Afirmațiile negative susținute de surse nu aveau parcursuri explicite și fragmente selectate ale aserțiunilor |
| A-002 | 85 / 80 / 85 | 70 / 55 / 70 | Testele structurale necesitau mutații ale parcurgerii și verificări generice de componentă dincolo de prezența câmpurilor |
| F-011 | 85 / 80 / 75 | 70 / 40 / 85 | Ordinea apelurilor poate omite curățarea urmăririi interacțiunilor când închiderea motorului eșuează; nicio execuție de componentă nu demonstrează o scurgere de stare |
| D-008 | 90 / 90 / 90 | 60 / 45 / 70 | Copia live păstrează 44 de colecții de rapoarte și 14 capturi Atlas fără selecție curentă limitată sau verificare a structurii publicației |
| R-001 | 90 / 90 / 90 | 60 / 40 / 65 | Metodele de produs păstrate nu au dovezi native selectate pentru fiecare motor; validarea înregistrării și detaliile câmpurilor sensibile necesită controale explicite de audit |
| D-009 | 90 / 80 / 80 | 85 / 65 / 80 | Structura susținută nu este impusă în toate documentele Markdown; contraexemplele privind paritatea codului inline și a valorilor numerice, precum și modificarea sensului calificărilor necesită controale cu responsabili expliciți |

Locațiile dovezilor istorice folosesc `${ATF_WORK_ROOT}/...`, cu spațiul extern de lucru
selectat conform ghidului [Dezvoltare](../Development.md).
Aceste locații nu sunt legături din depozit sau cerințe pentru copii suplimentare.
Rădăcinile efective ale execuției rămân în manifestele externe arhivate;
normalizarea prezentării nu reprezintă o nouă execuție.

<a id="verified-corrections"></a>
## Corecții verificate

| ID | Comportament livrat | Verificare |
|---|---|---|
| G-017 | Testele menținute dezactivate în sursă sunt respinse, iar firul existent al consolei este limitat; calitate livrată 93 (94/92/92) | local-02 final EN/RO 690/690 și 690/690; încercarea eșuată originală rămâne păstrată; [dovezi](Code_And_Documentation_Maturity.md#g-017) |
| D-010 | Strategia, rețetele de comenzi pliate și toate cele 51 de revizuiri documentare sunt guvernate; calitate livrată 92 (94/90/92) | Controale native între ediții, 182 de cazuri Node capturate per ediție și 32 de înregistrări desktop/56 SVG/98 de capturi; [dovezi și limite](Code_And_Documentation_Maturity.md#d-010) |
| D-009 | Toate documentele Markdown menținute au controale structurale/editoriale; faptele tehnice și sensul complet în română al README-urilor au verificări și revizuire distincte | 250 de cazuri de guvernanță pe ediție, 182 de cazuri Node, revizuirea semantică a tuturor celor 21 de perechi README și diagramele modificate inspectate; [dovezi](Documentation_And_Translation_Review.md#d-009) |
| R-001 | Principii Living Documentation, validări semantice expected/actual, mascarea limitată a credențialelor și dovezi native pentru aceleași metode existente | Playwright 16/16 și Selenium 16/16 PASS; selecțiile locale EN/RO și 181 cazuri Node au trecut; încercările eșuate sunt păstrate; [dovezi](Retained_UI_Auditability.md#r-001) |
| F-002 | Pornirea folosește suite menținute și configurație explicită de execuție | 304 cazuri locale; profilul aggregate a produs artefacte Extent și Allure |
| F-003 | Codul retras de generare/audit, prompturile și rapoartele istorice sunt eliminate din main | 100 cazuri de browser; sursa și JAR-ul împachetat nu conțin componente retrase |
| F-006 | Regresiile păstrate apelează utilitarele curente și folosesc căi menținute către suite | 304 cazuri locale și 11 cazuri Allure au trecut |
| M-001 | Cinci module, furnizori independenți, suport de produs exclusiv în teste și contracte impuse de arhitectură/documentație | 372 verificări finale ale reactorului, 102 cazuri de browser, 11 Allure și artefacte agregate inspectate; [dovezi](Module_Architecture.md) |
| G-001 | Ghiduri obligatorii pentru responsabili/feedback, fapte despre construcție, pereche HTML/Markdown și excluderea fișierelor de agenți | 24 cazuri de documentație dintre 74 de guvernanță au trecut; [dovezi](Governance_Review.md#g-001) |
| T-001 | Regresiile existente folosesc așteptări limitate, criterii de verificare bazate pe observații, curățare exactă și acoperire impusă a suitelor | 393 cazuri ale reactorului și 4 de izolare a configurației au trecut; [dovezi](Governance_Review.md#t-001) |
| P-001 | Atlas de documentație separat și politici recuperate, cu arhitectura vie esențială în ambele formate | Sursa/legăturile/hash-ul și redarea desktop/la lățime mică au fost verificate; fără modificări de producție/POM/Java nou; [dovezi](Governance_Review.md#p-001) |
| G-002 | Vizibilitatea surselor, responsabilitatea pauzelor/consolei și restaurarea întreruperii firului curent sunt guvernate | 474 verificări locale și 474 agregate au trecut; [dovezi](Governance_Contracts.md#g-002) |
| G-003 | Selecțiile Maven susținute, valorile implicite declarate și ciclul documentelor stabile sunt verificate față de responsabilii curenți | Verificările locale/agregate finale și contractele ghidurilor în pereche au trecut; [dovezi](Governance_Contracts.md#g-003) |
| T-002 | Execuția API/sesiunile și rezultatele cererilor/răspunsurilor de browser local au observații independente | 2 verificări pozitive și 4 negative de rețea în cele 119 verificări browser/native au trecut; [dovezi](Governance_Contracts.md#t-002) |
| F-009 | Bugetele negative de reîncercare sunt respinse înaintea execuției; zero permite totuși cererea inițială | Regresiile API locale și agregate finale au trecut; [dovezi](Governance_Contracts.md#f-009) |
| G-004 | Politica obligatorie de engleză pentru surse/documentație, verificările de limbă limitate și exemplele compilabile din README sunt impuse | 3 exemple compilate; exemplele independente Playwright și Selenium au trecut fiecare; [dovezi](Governance_Contracts.md#g-004) |
| F-010 | Ambele hărți de instantanee păstrează prima valoare observată, inclusiv absența, după schimbări repetate | Regresiile directe de instantanee și sesiuni de raportare au trecut în suitele locale/agregate finale; [dovezi](Governance_Contracts.md#f-010) |
| T-003 | Regresia de succes pentru valoarea intrării păstrează criteriul exact cu două citiri și un buget utilizabil de planificare | 474 verificări locale și 474 agregate au trecut după corecția exclusivă a fixture-ului; [dovezi](Governance_Contracts.md#t-003) |
| F-008 | Matricea Selenium/Chrome/driver selectată folosește CDP tipizat exact v158 și executabilul configurat prin Manager | 120 verificări browser/native au trecut, inclusiv 2 pozitive și 4 negative pentru răspunsuri reale; [dovezi](Browser_Compatibility_And_Portability.md#f-008) |
| G-005 | Ghidajul proiectului și verificările limitate pentru surse/configurație susțin căi relocabile și rădăcini explicite | Selecțiile locale și agregate relocate au trecut fiecare 514 verificări; [dovezi](Browser_Compatibility_And_Portability.md#g-005) |
| D-001 | Navigarea după întrebări, ghidajul canonic de revizuire și diagramele README lizibile păstrează exemplele publice | 5 diagrame Mermaid reale inspectate la lățimi desktop și mici; 3 exemple Java neschimbate compilate prin guvernanță; [dovezi](Browser_Compatibility_And_Portability.md#d-001) |
| T-004 | Potrivirea rețelei păstrează termene proprii, iar fixture-ul limitează separat inițializarea și închiderea | Execuția celor 6 corpuri de test și închiderile aferente s-au terminat în rularea acceptată cu 120 verificări browser; [dovezi](Browser_Compatibility_And_Portability.md#t-004) |
| G-006 | Structura semantică a README-urilor, edițiile engleză/română declarate și paritatea exactă a surselor și contractelor sunt guvernate | Ambele ediții au trecut 526 verificări locale, inclusiv 187 cazuri de guvernanță; 18 perechi README și diagramele redate au fost revizuite; [dovezi](Maturity_And_Readme_Editions.md#g-006--readme-governance-and-romanian-edition) |
| G-007 | Importuri Java explicite, păstrând implementările existente | 17 arbori de implementare identici; controale pentru importuri cu wildcard obișnuite/statice și selecții complete reușite; [dovezi](Code_Stability_And_Artifacts.md#g-007--explicit-java-import-ownership) |
| T-005 | Regresia existentă observă limita lucrătorilor prin sincronizare limitată și rezultate reale | 20 de invocări consecutive și selecțiile locale/agregate au trecut; [dovezi](Code_Stability_And_Artifacts.md#t-005--deterministic-worker-limit-regression) |
| D-003 | Documentație guvernată pentru artefacte, arhive clasificate și protocol Atlas reutilizabil | 8 verificări de fișiere/CLI; rapoarte arhivate și revalidate prin hash; 21 de README-uri traduse și exemplu Atlas; [dovezi](Code_Stability_And_Artifacts.md#d-003--governed-artifacts-and-reusable-atlas-intake) |
| G-008 | Mesaje constructive cu dovezi originale, responsabil și remediere verificabilă | 550 de cazuri trecute în fiecare ediție, inclusiv 211 de guvernanță; destinațiile și selecțiile indicate există; [dovezi](Actionable_Governance_Diagnostics.md#g-008--constructive-governance-failures) |
| T-006 | Controalele negative disting regula emisă de prefixe și mențiuni în date | 17 invocări focalizate și ambele selecții complete au trecut; [dovezi](Actionable_Governance_Diagnostics.md#t-006--exact-rule-identity-in-negative-governance-fixtures) |
| D-005 | Un HTML complet pentru fiecare proiect încorporează CSS dedicat, comportament, surse și dovezi | 66 de verificări de mentenanță și redare offline izolată pentru AI/J1 la două lățimi desktop, cu zece secțiuni; [dovezi](Atlas_Complete_Report.md#d-005--one-complete-understanding-artifact-per-target) |
| G-010 | Guvernanța raportului complet păstrează identitatea, rezultatele, izolarea și remedierea constructivă | Au trecut controalele pentru pachetul complet, criteriu, JSON, biblioteci și prezentare coruptă deliberat; [dovezi](Atlas_Complete_Report.md#g-010--constructive-governance-for-the-complete-atlas-contract) |
| K-001 | Recuperarea locală citată păstrează toate cerințele obligatorii și aria țintei alese | 14 înregistrări, 12 politici obligatorii, 12 verificări ale cunoștințelor și trei obiecte Git arhivate verificate; [dovezi](Atlas_Complete_Report.md#k-001--cited-knowledge-retrieval-with-mandatory-policy) |
| G-011 | Intrările inițiale numerice rămân separate de istoricul verificat păstrat | Au trecut controalele pentru tabelul inițial malformat și ambele selecții de guvernanță cu 211 cazuri; [dovezi](Atlas_Complete_Report.md#g-011--keep-feedback-baseline-inputs-distinct-from-verified-history) |
| G-012 | Sunt guvernate identitățile exacte ale comportamentelor, afirmațiile diagramelor din fiecare flux și întreaga confirmare canonică a recuperării cunoștințelor | 111 verificări de mentenanță, dovezi negative originale și cazuri J1 PASS/FAIL distincte; [dovezi](Atlas_Depth_And_Assessment.md#g-012) |
| G-013 | Cinci unități reale de cod responsabile pentru fiecare proiect declară șapte perspective și limite explicite ale revizuirii surselor | AI 8 fluxuri/48 afirmații; J1 6 fluxuri/37 afirmații; controale pentru dovezi proprii/comune și surse exacte; [dovezi](Atlas_Depth_And_Assessment.md#g-013) |
| D-006 | Explicațiile aprofundate păstrează un singur document complet pentru fiecare proiect | 20/23 documente sursă, 4 verificări desktop/32 diagrame, câte 211 teste în ambele ediții și fără E2E noi; [dovezi](Atlas_Depth_And_Assessment.md#d-006) |
| A-001 | Axele comparabile, legate de dovezi, separă sursa proiectului de calitatea Atlas/RAG | AI 90/90 și J1 87/89; componente 40/30/30, ponderi, referințe și limite explicite; [dovezi](Atlas_Depth_And_Assessment.md#a-001) |
| G-015 | Sunt guvernate traseele accesibile, fragmentele exacte ale oracolelor independente și limitele cazurilor negative | 138 verificări de mentenanță, controale pentru aliasuri/cicluri/coruperi și selecțiile Java ale ambelor ediții trecute; [dovezi](Behavior_And_Negative_Cases.md#g-015) |
| D-007 | Comportamentele pozitive și negative existente sunt explicate în fiecare document complet | AI: 11 / J1: 10 fluxuri, 53 cazuri, 4 verificări desktop/46 diagrame și 21/23 ghiduri sursă incluse; [dovezi](Behavior_And_Negative_Cases.md#d-007) |
| A-002 | Algoritmii puri de parcurgere, componentele de publicare și dovezile de componentă API păstrează limite distincte | 138 verificări Node și cinci invocări adăugate în clasa existentă de contracte API trecute; XML/TAP originale păstrate; [dovezi](Behavior_And_Negative_Cases.md#a-002) |
| D-008 | Publicația curentă de artefacte este limitată, iar istoricul complet și imuabil rămâne în afara proiectului live | 26 fișiere generate curente, două documente Atlas neschimbate, 20 verificări de componentă și 211/211 verificări Java de guvernanță; [dovezi](Artifact_Publication_And_History.md#d-008) |

Criteriile de acceptanță pentru browser desktop și dovezile corecțiilor explicit istorice sunt în
[mentenanța framework-ului](Framework_Maintenance.md). Contractele de închidere și
dovezile execuțiilor sunt în [contractele executabile aprofundate](Governance_Contracts.md)
și [compatibilitatea browserelor și portabilitate](Browser_Compatibility_And_Portability.md).
[Revizuirea anterioară de guvernanță](Governance_Review.md) își păstrează aria originală.
F-008 este verificat pentru matricea Windows înregistrată; altă platformă sau versiune necesită
dovezi noi de acceptanță. [Înregistrarea verificării API](API_Verification.md)
distinge regresiile API ale framework-ului de scenariile GoRest de scriere existente.

<a id="recording-and-closing-feedback"></a>
## Înregistrarea și închiderea feedback-ului

1. Înregistrează cel mai mic caz reproductibil, comportamentul așteptat și cel observat.
2. Atribuie un responsabil și un TODO verificabil înainte de schimbarea contractului.
3. Păstrează oneste observațiile de eșec și numărătorile testelor; un artefact copiat nu este
   o execuție nouă.
4. Închide un element numai după ce verificările declarate trec. Înregistrează commit-ul sursei,
   comanda exactă și locația externă a dovezilor.
5. Mută comportamentul stabil în documentația responsabilului și păstrează registrul
   concentrat pe lacune active și înregistrări concise de închidere.
