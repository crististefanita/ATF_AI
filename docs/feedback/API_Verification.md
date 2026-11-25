<a id="existing-api-test-verification"></a>
# Verificarea testelor API existente

<a id="scope-and-source"></a>
## Domeniu și sursă

La 2026-11-24, subsetul întreținut pentru API-ul frameworkului a fost executat
dintr-o copie de validare nouă, mutată într-o altă locație. Revizia sursă a fost
`4e80e40364dd1f59fe3507832bea7405e387dede`. Schimbarea de mentenanță asociată
actualizează documentația și evidența domeniului verificat; codul de producție,
sursele testelor și selecțiile suitelor rămân neschimbate.

Etapa curentă păstrează inventarul E2E existent și prioritizează stabilitatea,
guvernanța și documentația clară în engleză. Această verificare nu a folosit
teste E2E suplimentare, emulatoare sau modificări ale aplicației publice.

<a id="executed-selection-and-result"></a>
## Selecția executată și rezultatul

Folosește pregătirea cache-ului și a rădăcinii de lucru din
[Dezvoltare](../Development.md), apoi rulează din rădăcina reactorului temporar:

<details>
<summary>Rulează comenzile pentru selecția executată și rezultat</summary>

```powershell
mvn -B -ntp "-Dmaven.repo.local=$mavenCache" -pl atf-tests -am "-Datf.test.suite=src/test/resources/suites/framework-api.xml" test
```

</details>

| Responsabil executat | Cazuri | Eșecuri | Erori | Omise | Limita dovezii |
|---|---:|---:|---:|---:|---|
| BaseTestAPIContractTest | 8 | 0 | 0 | 0 | Ciclul de bază, pregătirea explicită pentru scrieri, separarea API/UI și politica de așteptare |
| ApiCoreExecutionArchitectureContractTest | 16 | 0 | 0 | 0 | Reîncercare reușită/epuizată, stări fără reîncercare, excepții, întrerupere, bugete negative/nule și responsabilitatea fațadei |
| Verificări native ale bibliotecilor dependente | 12 | 0 | 0 | 0 | 7 verificări Core, 1 pentru furnizorul Playwright, 2 pentru furnizorul Selenium și 2 pentru furnizorul de raportare |
| Reactorul complet selectat | 36 | 0 | 0 | 0 | 24 de verificări ale API-ului frameworkului și 12 verificări native ale bibliotecilor |

Rularea acceptată s-a încheiat cu codul de ieșire 0 și BUILD SUCCESS. Numerele
au fost citite din XML-urile Surefire noi, inclusiv pentru cele două clase API
selectate. Cazurile executorului local folosesc dubluri pentru observarea
apelurilor, răspunsurilor, întârzierilor și ordinii raportării. Nu trimit cereri
către GoRest și nu demonstrează funcționarea unei instalări externe.

<a id="existing-gorest-journeys"></a>
## Scenariile GoRest existente

[PositiveUserTests](../../atf-tests/src/test/java/com/ai/api/tests/PositiveUserTests.java)
păstrează cele două scenarii de scriere existente: creare cu validarea schemei
și creare/așteptare/ștergere. Niciunul nu a fost selectat sau executat în această
verificare. Valoarea auth.token inclusă este goală și nu s-a furnizat o suprascriere.
[BaseTestAPI](../../atf-core/src/main/java/com/ai/api/core/BaseTestAPI.java)
ridică SkipException când tokenul de scriere lipsește. Un token indisponibil sau
un scenariu de scriere omis nu trebuie prezentat drept rezultat GoRest reușit.

Verdictul observat este, așadar: subsetul API existent al frameworkului trece;
cele două scenarii externe de scriere nu au aici un rezultat nou de execuție.

<a id="external-evidence-and-unsuccessful-launch"></a>
## Dovezi externe și pornirea nereușită

Toate locațiile de mai jos folosesc ATF_WORK_ROOT selectat de apelant. La această
revizie înregistrată la 2026-11-24, copiile sursă și live nu conțineau artefacte
generate de rulări. Publicarea locală selectată ulterior respectă
[contractul selecției curente și al istoricului](../../artifacts/reports/README.md#current-selection-and-history);
nu execută și nu modifică această dovadă API originală.

- Copia acceptată: `${ATF_WORK_ROOT}/validation/portable workspace/modules-api-focused-02`.
- Jurnalul acceptat: `${ATF_WORK_ROOT}/runs/modules-api-focused-02.log`.
- Hash-urile sursei, argumentele rezolvate și mediul:
  `${ATF_WORK_ROOT}/runs/modules-api-focused-02-source.json`.
- Totalurile XML pentru fiecare modul: `${ATF_WORK_ROOT}/runs/modules-api-focused-02-results.json`.
- XML-urile originale: target/surefire-reports din fiecare modul testat al copiei acceptate.

Pornirea anterioară modules-api-focused-01 s-a oprit înaintea execuției testelor,
deoarece PowerShell a separat proprietatea suitei care nu era între ghilimele.
A executat zero teste și nu constituie dovadă de acceptanță. Jurnalul și
manifestul sursei au fost păstrate separat. Pornirea acceptată a transmis
proprietatea completă a suitei ca un singur argument între ghilimele, conform
exemplului de mai sus; nu a fost introdusă ocolirea vreunui test sau mecanism runtime.
