<a id="modular-architecture-feedback"></a>
# Feedback privind arhitectura modulară

<a id="m-001-extract-independent-runtime-artifacts"></a>
## M-001: Extragerea artefactelor de execuție independente

Stare: VERIFIED la 2026-11-24. Responsabil: arhitectura framework-ului.

Versiunea de referință de la commit-ul `6f0d5b7` împacheta SDK-urile de browser, implementările de raportare,
suportul pentru produs și contractele framework-ului într-un singur artefact. Consumatorii nu puteau
selecta un singur adaptor de browser sau rula Core fără acele dependențe de furnizor.

Reactorul conține acum `atf-core`, `atf-playwright`, `atf-selenium`,
`atf-reporting` și modulul `atf-tests`, destinat exclusiv testelor. Furnizorii de servicii Java
compun adaptoarele și raportarea la execuție. Numele publice existente ale pachetelor sunt
păstrate acolo unde este practic; utilitarele de guvernanță a surselor și scenariile aplicației
aparțin surselor de test.

<a id="acceptance-evidence"></a>
## Dovezi de acceptanță

Commit-ul sursă final: `457c4daced169a6947e5ff32e78ae690b778340b`.
Comenzile au fost rulate din copii temporare sub ${ATF_WORK_ROOT}/validation, cu cache-urile Maven
și de browser în afara copiilor de lucru pentru dezvoltare și exploatare. Fișierele temporare
au fost, de asemenea, izolate pentru fiecare rulare. Fiecare rând de acceptanță are zero eșecuri, erori
și teste omise; numerele din profiluri se suprapun și nu trebuie însumate ca teste unice.

| Verificare | Comandă la nivelul reactorului | Cazuri executate | Dovezi sub ${ATF_WORK_ROOT}/runs |
|---|---|---|---|
| Regresii locale, guvernanță și împachetare | `mvn -B -ntp verify` | 361 de cazuri de verificare plus 11 cazuri native ale bibliotecilor = 372 | modules-acceptance-02.log |
| Paritate de browser între Selenium și Playwright | `mvn -B -ntp -Pbrowser test` | 102 cazuri de browser plus 11 cazuri native = 113 | modules-browser-01.log |
| Integrare Allure nativă | `mvn -B -ntp -Datf.test.suite=src/test/resources/suites/framework-allure.xml -Dreporting.engine=allure test` | 11 cazuri de integrare plus 11 cazuri native = 22 | modules-allure-01.log |
| Publicare agregată efectivă | `mvn -B -ntp -Paggregate test` | 361 de cazuri de verificare plus 11 cazuri native = 372 | modules-aggregate-02.log |

Copiile finale pentru rulările locală și agregată corespund tuturor celor 645 de fișiere sursă urmărite la
commit-ul înregistrat. Rulările de browser și Allure au folosit `6723060`; sursele lor de execuție,
resursele, configurația de compilare și sursele de test selectate corespund versiunii
finale. Singurele modificări ulterioare au fost textul despre migrarea consumatorilor din rădăcină și două
surse de guvernanță, acoperite de rerulările finale locală și agregată. Diferențele complete
și hash-urile sunt înregistrate în modules-source-audit-457c4da.json.

Cele 57 de cazuri de guvernanță includ fixture-uri negative relevante pentru profiluri Maven,
coordonate și cicluri de dependențe; proprietatea surselor și domeniile lexicale de umbrire;
înregistrarea serviciilor; referințe Markdown, ancore, exemple literale și blocuri
reale de diagramă. Politica de limbă detectează resturi cunoscute de proză română;
documentația întreținută a fost redactată și revizuită în engleză.

<a id="artifact-inspection"></a>
## Inspectarea artefactelor

- Toate cele cinci module se împachetează cu succes. Cele patru JAR-uri de execuție conțin
  clasele și descriptorii de servicii care le aparțin; atf-tests conține zero clase de
  producție. Nu s-au găsit componente retrase sau scurgeri de suport pentru produs. Hash-urile JAR-urilor
  sunt în modules-jar-audit-6723060.json; sursele de execuție nu s-au schimbat ulterior.
- Copia pentru agregare a produs
  `atf-tests/target/atf-core-aggregate-report/extent/ATFCoreAggregateExtentReport.html`
  și 362 de rezultate Allure reușite: 361 de cazuri plus rezumatul agregat.
- Inspectorul prezentării generale Extent publicate a găsit zero încălcări. Timpii rezultatelor Allure
  au fost valizi. Dovezi: modules-aggregate-02-artifact-audit.log și
  modules-aggregate-02-allure-audit.json.

O rulare de diagnostic anterioară în sandbox, modules-local-04, a fost oprită după restricționarea
înlocuirii fișierelor temporare și blocarea unui utilitar Java. Este marcată explicit
ca abandonată și nu reprezintă dovadă de acceptanță. Rulările reușite de mai sus au folosit directoare
temporare dedicate și permisiuni normale de execuție pe gazdă.

La acest reper istoric, matricea browser desktop/CDP necesita încă
o verificare separată. [Registrul curent](README.md) consemnează dovezile
F-008 ulterioare.

<a id="m-002"></a>
<a id="m-002-explicit-api-and-verification-module-ownership"></a>
## M-002: Responsabilități explicite pentru modulele API și de verificare

Status: VERIFIED în limitele mutării surselor, componentelor, împachetării și consumatorului independent. Responsabil: API și guvernanța surselor.
Prioritate 93 (impact 95 / expunere 92 / urgență 91).
Calitate inițială 84 (corectitudine 91 / verificare 82 / claritate 77).

- Extrage implementarea API reutilizabilă existentă și dependențele REST Assured în atf-api; păstrează pachetele și comportamentul.
- Oferă regulilor de cod/documentație propriul modul atf-governance și propria suită implicită.
- Împachetează helperele din main și păstrează clasele TestNG adnotate în test în ambele module de verificare; păstrează inventarul exact al metodelor revizuite.
- Împachetează datele de produs cu helperele lor; păstrează suitele XML și inventarul de guvernanță în resursele de test.
- Păstrează manifestele istorice de audit cu identitatea sursei originale și respinge responsabilitățile de validare duplicate și ambigue.
- Actualizează ghidurile EN/RO, diagramele, responsabilii execuției și Living Markdown/HTML. Nu regenera Atlasul și nu schimba generarea ori contractele RAG.

Dovezi: ${ATF_WORK_ROOT}/reviews/module-api-governance-20261128.
Acceptanța necesită construiri curate în copii relocate pentru ambele ediții, identități păstrate ale metodelor și surselor, inspecția JAR-urilor/depedențelor și verificările existente relevante. Eșecurile de diagnostic originale sunt păstrate separat de calificarea finală.

Calitate livrată 96 (corectitudine 97 / verificare 96 / claritate 95).
Selecțiile pe sursa capturată trec 333 de cazuri de guvernanță, 373 locale ale framework-ului și 42 native ale bibliotecilor. Cele 35 de cazuri focalizate ale reparării se suprapun selecției de guvernanță și nu se adaugă acestor totaluri.

Șapte JAR-uri păstrează bytecode Java 11 și exclud toate cele 113 tipuri sursă adnotate existente. Cele 35 de fișiere sursă API își păstrează conținutul canonic original. Un consumator Maven separat încarcă doar artefactele ATF Core/API, cu REST Assured/schemă prezente și browser/Extent/Mockito absente; fără apeluri HTTP sau browser.

Refuzurile de diagnostic originale rămân în dosarul extern, inclusiv responsabili de surse vechi, proză README densă, lipsa reviziei HTML complete și coliziunea cu identificatorul istoric de feedback. Verificările au fost adaptate noilor responsabili, fără eliminarea controalelor. Rulările finale ale ambelor ediții și sincronizarea au propriile dovezi.
