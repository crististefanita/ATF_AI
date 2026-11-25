# atf-tests

<a id="overview"></a>
## Prezentare generală

Acest modul gestionează guvernanța, regresiile între module, suportul produsului și
scenariile de aplicație explicite. Coordonatele sunt `com.ai:atf-tests:1.0.0`.
Dependențele sale de framework și codul de suport sunt destinate testelor; modulul
nu este o bibliotecă de execuție pentru producție.

<a id="ownership"></a>
## Responsabilități

| Zonă | Locație |
|---|---|
| Verificări de framework și guvernanță | [com.ai.atf](src/test/java/com/ai/atf) |
| Pagini, fluxuri și aserțiuni UI ale produsului | [com.ai.ui.support](src/test/java/com/ai/ui/support) |
| Endpoint-uri API, modele și fabrici ale produsului | [com.ai.api.support](src/test/java/com/ai/api/support) |
| Selecții TestNG menținute | [suite](src/test/resources/suites) |
| Date de fixture și setări de raportare | [resurse de test](src/test/resources) |

Guvernanța protejează graful de dependențe al celor cinci module, limitele furnizorilor,
documentația menținută și responsabilitatea surselor. Verificările native ale bibliotecilor
rămân în modulele lor.

Modulul folosește [atf-core](../atf-core/README.md),
[atf-playwright](../atf-playwright/README.md),
[atf-selenium](../atf-selenium/README.md) și
[atf-reporting](../atf-reporting/README.md) ca dependențe de test.

<a id="execution"></a>
## Execuție

Pentru o verificare eșuată a surselor sau documentației, urmează
[procedura de diagnostic](../docs/governance/Failure_Diagnostics.md): păstrează
constatarea observată, inspectează contractul responsabil și verifică remedierea
propusă prin selecția menținută. Un indiciu de remediere nu dovedește cauza.

Dintr-o copie izolată a reactorului:

```powershell
mvn -B -ntp test
mvn -B -ntp -pl atf-tests -am test
mvn -B -ntp -Pbrowser test
mvn -B -ntp -Paggregate test
```

Execuția implicită selectează framework-local, inclusiv guvernanța. Profilul browser
selectează framework-browser numai în acest modul. Profilul aggregate execută
suita menținută de agregare și publică ambele formate de raport.
[Testare](../docs/Testing.md) definește pregătirea browserelor, comenzile filtrate,
configurația aplicației și acceptanța artefactelor.

Scenariile aplicației rămân selecții explicite de clase. Au nevoie de URL-uri și
conturi configurate și pot crea date. Rezultatele locale de framework nu demonstrează
acceptanța pe un site public.

<a id="evidence-and-maintenance"></a>
## Dovezi și mentenanță

Verificările existente de guvernanță impun catalogul menținut al suitelor, excluderile
explicite pentru teste live și fixture-uri de eșec, documentația responsabililor și ciclul feedback-ului.
Ghidul viu UI/API are o sursă Markdown canonică și o proiecție HTML verificată;
Atlasul de documentație rămâne în afara reactorului de execuție.

Rezultatele aparțin directoarelor target ale acestui modul din copia temporară
de validare. Păstrează sursa fiecărei rulări, selecția exactă, numărătorile și
artefactele în afara arborilor de surse de dezvoltare și live.
[Feedback-ul](../docs/feedback/README.md) păstrează TODO-urile active și dovezile istorice;
rezultatele noi trebuie să își precizeze limitele execuției.

Păstrează regresiile focalizate lângă comportamentul protejat. Actualizează
[responsabilitățile testelor de raportare](src/test/java/com/ai/atf/core/reporting/README.md)
când modifici acea zonă de teste.
