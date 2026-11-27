<a id="report-archives"></a>
# Arhive de rapoarte

<a id="overview"></a>
## Prezentare generală

Păstrează rezultatele ATF încheiate după scopul lor specific, fără a modifica octeții
rapoartelor sau a pierde resursele relative. [Colectorul](../../tools/collect-artifacts.mjs)
folosește funcțiile incluse în Node 20+ și nu depinde de Maven sau de bibliotecile de execuție.
Rulează-l după terminarea comenzilor producătoare, din orice copie care conține unealta.

| Categorie | Producător/rezultat observat | Interpretare |
|---|---|---|
| test-results | target/surefire-reports sau target/failsafe-reports ale modulelor | XML-ul original al testelor, paginile TestNG și resursele lor |
| extent | Arborii obișnuiți target/reports | Paginile și datele existente ale rapoartelor; fără reconstrucție |
| allure | allure-results nativ/de modul, target/allure-results sau un allure-report existent | Rezultate brute sau un site deja generat |
| aggregate | target/atf-core-aggregate-report | Site-ul agregat existent complet și resursele sale |
| browser | Rezultate de capturi, urme, video și atașamente | Dovezi păstrate, fără a demonstra că fiecare fixture provine dintr-un browser real |
| build | JAR/ZIP și metadate Maven de construcție | Artefacte existente de construcție; fără a implica o versiune publicată |
| execution | Jurnale/proveniență explicite și diagnostice de proces | Contextul original al execuției și revizia declarată a sursei |
| unclassified | Alte rezultate păstrate | Dovezi vizibile care necesită revizuirea responsabilului, niciodată eliminate implicit |

Căile dovezilor sunt ordonate ordinal după unitățile UTF-16, independent de
colaționarea gazdei. Perechile cale/amprentă rămân comparabile între colaționări;
această ordine nu face identice marcajele temporale sau rezultatele execuției.

Colectorul parcurge rădăcina furnizată și modulele imediate care au POM.

- Copiază rezultatele target, exceptând directoarele compilate/temporare declarate și fișierele Java/class generate din afara arborilor de raportare.
- Sursele atașate în rapoarte rămân dovezi.
- Sunt incluse allure-results și .codex-artifacts de la nivel de modul.
- Această clasificare nu schimbă configurația producătorilor și nu adaugă o selecție E2E.
- Arborii aggregate/Allure își păstrează categoria producătorului; celelalte fișiere media sunt indexate ca dovezi de browser.
- Clasificarea descrie locația și formatul, nu un verdict.

<a id="ownership"></a>
Execuțiile ATF curente publică Extent. Formatul Allure din tabelul formatelor identifică date păstrate sau produse extern; nu este o implementare activă de raportare ATF.

## Responsabilități

Apelantul furnizează rădăcina validării încheiate, o rădăcină distinctă de arhivare și un ID de rulare portabil, unic.

- Rezultatul trebuie să stea în afara fiecărei rădăcini colectate.
- Nu colecta cât timp există scrieri active.
- Căile sunt rezolvate la execuție; unealta nu include unitatea autorului, directoare fixe de cache sau căi de conturi.

Colecțiile complete și încercările eșuate aparțin unei rădăcini externe de istoric.

- Copia live publică dovezile originale selectate în reports/current, conform [contractului publicației limitate](../README.md).
- Compilarea, cache-urile, celelalte site-uri complete de raportare și investigațiile temporare rămân în afara copiei live.
- Păstrează pachetele istorice intacte, inclusiv resursele relative și identitatea surselor.

```text
external-history/artifacts/reports/runs/<run-id>/
  collection-status.json
  manifest.json
  index.html
  catalog/<category>.json
  files/<original source-relative path>
  evidence/<input-number>/<original filename>
```

Arborele original cu căi relative la sursă păstrează legăturile dintre rapoarte și resurse.

- Indexul și cataloagele oferă vederi după scopul raportului, fără a aplatiza fișierele.
- Manifestul înregistrează dimensiuni, SHA-256, locații originale, categorii și căi de construcție ignorate. collectionStatus descrie copierea; executionVerdict rămâne NOT_INFERRED.
- Citește XML-ul și jurnalele păstrate pentru rezultatele reale ale testelor.
- Un producător absent este NOT_FOUND; colectorul nu fabrică un raport Extent/Allure.

ID-urile existente sunt respinse fără suprascriere.

- Un eșec de copiere/legătură/hash lasă collection-status.json în starea FAILED, fără manifest complet.
- Păstrează acele dovezi parțiale și folosește un ID nou pentru colectarea corectată.
- Colectorul nu șterge surse, nu urmărește arbori de raportare legați și nu rescrie dovezi vechi.

<a id="collection"></a>
## Colectare

Rezolvă rădăcina de lucru conform ghidului [Dezvoltare](../../docs/Development.md).
Intrările de mai jos indică o rulare existentă încheiată, jurnalul și manifestul surselor sale:

```powershell
$snapshotRoot = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_SOURCE -ErrorAction Stop).Path
$runLog = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_LOG -ErrorAction Stop).Path
$sourceManifest = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_MANIFEST -ErrorAction Stop).Path
$provenance = Get-Content -LiteralPath $sourceManifest -Raw | ConvertFrom-Json
$historyRoot = (Resolve-Path -LiteralPath $env:ATF_ARTIFACT_HISTORY_ROOT -ErrorAction Stop).Path
$archiveRoot = Join-Path $historyRoot 'artifacts/reports/runs'
$runId = $env:ATF_ARTIFACT_RUN_ID
node tools/collect-artifacts.mjs --source "$snapshotRoot" --output "$archiveRoot" --run-id "$runId" --revision "$($provenance.sourceCommit)" --evidence "$runLog" --evidence "$sourceManifest"
```

Revizia este declarată de apelant, iar manifestul surselor este păstrat; colectarea singură nu poate demonstra ce sursă a produs un raport vechi.

- Pentru un reports.dir personalizat, rezultate Surefire externe sau altă destinație, repetă --include cu category=existing-path.
- Include părintele comun al unui site HTML personalizat și al resurselor sale.
- Fișierele din intrările explicite își păstrează subarborele sub files/_included.
- Rădăcinile suplimentare și fișierele de dovezi trebuie să existe deja.

Deschide index.html pentru navigare pe categorii. Unele site-uri producătoare necesită
un server HTTP local; servește întregul pachet când formatul o cere. Colectorul nu
instalează un server, nu regenerează rapoarte și nu transformă eșecurile în succese.

<a id="current-selection-and-history"></a>
## Selecția curentă și istoricul

Selectează numai XML/TAP native, manifestele surselor executate și confirmările finale de verificare necesare rezumatului curent.

- Leagă-le în publication.json.
- Fișierele generate reports/current/index.html și README.md precizează reviziile capturate, selecțiile reale, rezultatele și limitele.
- Verificarea structurii nu execută teste.
- Un raport mai vechi rămâne istoric, iar o selecție eșuată rămâne eșuată.

Păstrează arborii compleți Allure, aggregate, browser și ai fixture-urilor corupte deliberat în istoricul extern.

- Nu duplica fiecare colecție originală în live.
- Identifică istoricul prin ID-ul instantaneului și digestul inventarului; apelantul rezolvă rădăcina externă prin ATF_ARTIFACT_HISTORY_ROOT.
- Instrucțiunile menținute nu conțin căi fixe ale calculatorului autorului.
- Căile originale rezolvate rămân proveniență, fără a deveni cerințe pentru locația copiei proiectului.

Mută împreună un instantaneu complet de istoric.

- Intrările Atlas pot referi mai multe rulări de rapoarte; mutarea rulărilor izolate rupe aceste dependențe.
- Păstrează contextul surselor și indexurile originale.
- O copie a surselor fără metadate Git este context de lectură, fără a substitui verificarea Git canonică.
- Retrage o selecție curentă veche numai după verificarea arhivei sale externe intacte.

<a id="retained-ui-report-publication"></a>
## Publicarea rapoartelor UI păstrate

Selecția explicită de teste de produs păstrate poate publica un document original reports/current/e2e/playwright/ExtentReport.html și un document original reports/current/e2e/selenium/ExtentReport.html.

- Capturile de ecran rămân încorporate în raportul lor nativ.
- Selectează trei fișiere suplimentare de dovezi pentru fiecare motor: XML-ul Surefire original, manifestul original al surselor executate și JSON-ul de audit guvernat.
- Rulările complete, jurnalele, încercările anterioare și celelalte site-uri de raportare rămân în istoric.

Fiecare fișier selectat declară uiEngine, runId și sourceRevision în publication.json.

- Intrarea Extent are kind extent și referințe nativeResultsPath, executedSourcePath și auditPath.
- Referințele trebuie să selecteze fișiere declarate distincte test-results/execution/execution din același director al motorului, cu aceeași identitate de rulare/sursă.
- Documentul declară și executionScope.
- Nu sunt valide dovezile orfane, un al doilea document, un HTML generic de execuție sau o referință între motoare diferite.

[Verificatorul de audit](../../tools/verify-ui-e2e-audit.mjs) reconciliază subiecții și rezultatele XML-ului original cu nodurile native Extent pentru metode/pași, dovezile de validare așteptate/observate, metadatele capturilor și identitatea sursei/motorului.

- Rezultatul său reprezintă consistența limitată a auditului.
- Un FAIL sau SKIP nativ al aplicației rămâne vizibil; copierea unui raport sau trecerea unei verificări de structură/audit nu îl poate transforma în PASS.
- Un PASS al acțiunii nu poate înlocui validarea separată a regulii de afaceri.

[Ghidul de execuție](../../docs/Testing.md#retained-product-ui-audit) gestionează
aceleași metode păstrate și cele două comenzi de browser.
[Principiile vii](../../docs/Living_Architecture_UI_API_doc_v1_0.md#retained-principles)
gestionează responsabilitățile și limitele dovezilor, inclusiv lacuna privind curățarea datelor demonstrative.

<a id="verification"></a>
## Verificare

Din rădăcina reactorului, cu Node 20+ disponibil:

```powershell
$artifactRoot = Join-Path (Get-Location).Path 'artifacts'
node --test tools/collect-artifacts.test.mjs
node --test tools/verify-ui-e2e-audit.test.mjs
node tools/collect-artifacts.mjs --check-layout "$artifactRoot"
```

Aceste regresii de sistem de fișiere și CLI acoperă fidelitatea octeților/resurselor,
rapoarte reale eșuate, categorii goale, refuzul suprascrierii, ID-uri invalide, destinații
recursive, arbori legați, modificări în timpul copierii, rădăcini personalizate și căi cu spații.
Sunt verificări ale uneltelor de mentenanță, nu teste E2E de produs. Suita Java locală
guvernează ghidurile menținute, vizibilitatea surselor și paritatea edițiilor;
nu înlocuiește această selecție explicită a uneltei.

Fixture-urile publicației acoperă și inventare malformate, proiecte duplicate, HTML-uri nedeclarate, fișiere orfane, dovezi modificate/lipsă, căi nesigure, legături și directoare vechi de istoric/construcție/temporare.

- Verificările care resping structura păstrează fișierele și marcajele lor temporale.
- Aceste controale nu adaugă scenarii E2E de produs sau funcționalități la execuție.
- Controalele UI păstrate resping în plus motoarele, rulările, reviziile și referințele la dovezi nealiniate, păstrând rezultatele originale de eșec/omitere.
