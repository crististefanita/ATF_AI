<a id="atf--ui-and-api-test-automation"></a>
# ATF — automatizarea testelor UI și API

ATF rulează teste API REST și UI în browser, scrise în Java. Cele șapte module Maven separă contractele comune, execuția REST, adaptoarele de browser, raportarea, guvernanța și verificările. Proiectul părinte este `com.ai:atf-parent:1.0.0`; bibliotecile compilate țintesc Java 11.

<a id="the-design-in-one-view"></a>
## Designul într-o singură imagine

Un test consumator declară ce observă. Core deține configurarea, contractele UI și raportarea neutră; atf-api deține execuția REST.

Adaptoarele implementează contractul UI, iar raportarea este opțională. Testele depozitului protejează aceste limite.

| Modul | Responsabilitate |
|---|---|
| [atf-core](atf-core/README.md) | Configurare, contracte UI, ciclu de viață și SPI de raportare |
| [atf-api](atf-api/README.md) | Execuție REST Assured reutilizabilă, reîncercări și aserțiuni ale răspunsului |
| [atf-playwright](atf-playwright/README.md) | Implementarea contractului UI prin Playwright |
| [atf-selenium](atf-selenium/README.md) | Implementarea contractului UI prin Selenium |
| [atf-reporting](atf-reporting/README.md) | Adaptoare de raportare TestNG și publicare agregată |
| [atf-governance](atf-governance/README.md) | Reguli pentru surse, dependențe și documentație, cu contracte independente |
| [atf-tests](atf-tests/README.md) | Helpere de framework/produs în main; verificări adnotate existente în test |

```mermaid
flowchart LR
    API["atf-api"] --> Core["atf-core"]
    PW["atf-playwright"] --> Core
    SE["atf-selenium"] --> Core
    Reporting["atf-reporting"] --> Core
    Tests["atf-tests"] --> API
    Tests --> PW
    Tests --> SE
    Tests --> Reporting
    Tests --> Governance["atf-governance"]
    Governance -. auditează .-> Core
```

<details>
<summary>Legendă: relații și limita dovezii</summary>

Săgețile continue arată dependențe de compilare selectate; cele punctate arată inspecția sursei. Guvernanța are și dependențe de test spre bibliotecile de execuție, omise aici. Core nu expune tipuri REST Assured, Playwright, Selenium sau din SDK-ul de raportare. [Atlas](atf-atlas/README.md) este exclusiv documentație, în afara Maven, și leagă sursele de teste și limitele dovezilor.

</details>

<a id="how-a-check-runs"></a>
## Cum rulează o verificare

Alege API REST când nu este necesar un browser. Alege UI când verificările folosesc un furnizor de browser prin acțiunile comune Core. Raportarea poate publica ciclul de viață TestNG printr-un backend opțional.

```mermaid
flowchart LR
    Runner["TestNG"] --> Test["Test consumator"]
    Test --> Route["API sau UI"]
    Route --> Api["Execuție API"]
    Route --> Ui["Furnizor UI"]
    Runner -. ciclu de viață .-> Reporting["Backend raportare"]
```

<details>
<summary>Legendă: relații și limita dovezii</summary>

Săgețile continue arată calea contractului ales. Cele punctate arată raportarea opțională a ciclului de viață TestNG. Calea API nu pornește un browser; furnizorul UI se alege înainte de inițializare. Paginile și aserțiunile produsului aparțin consumatorului. [Arhitectura](docs/Architecture.md) explică detaliile contractelor.

</details>

<a id="how-to-choose-a-consumer-route"></a>
## Cum alegi calea consumatorului

- **API REST:** adaugă `atf-api`; baza și clientul API nu necesită un furnizor de browser.
- **UI în browser:** adaugă un adaptor și setează `ui.engine` înaintea inițializării TestNG. Folosește exemplul concret [Playwright](atf-playwright/README.md#consumer-example) sau [Selenium](atf-selenium/README.md#consumer-example).
- **Rapoarte Extent:** adaugă `atf-reporting` când consumatorul are nevoie de raportare HTML.

Exemplele consumatorului se află în ghidurile furnizorilor. Această pagină rădăcină rămâne axată pe alcătuirea framework-ului și următoarea decizie a cititorului.

<a id="start"></a>
<a id="build-and-verify"></a>
## Construiește și verifică

Folosește JDK 17+ și Maven; rezultatul țintește Java 11. Pregătește o copie izolată de validare conform [Dezvoltării](docs/Development.md), apoi rulează din rădăcina reactorului.

<details>
<summary>Rulează selecțiile implicite, browser și agregare</summary>

```powershell
mvn -B -ntp test
mvn -B -ntp -Pbrowser test
mvn -B -ntp -Paggregate test
```

</details>

Selecția implicită rulează verificări native ale modulelor și regresiile existente de framework și guvernanță.

- Profilul browser selectează fixture-uri locale și necesită binare pregătite; profilul aggregate publică și dovezi Extent.
- [Testare](docs/Testing.md) descrie precondițiile, comenzile selective și limitele dovezilor.
- Scenariile existente ale produsului rămân opționale; acest ghid nu adaugă scenarii.

<a id="configuration"></a>
<a id="consumer-migration"></a>
<a id="configure-and-migrate"></a>
## Configurează și migrează

[framework.properties](atf-core/src/main/resources/framework.properties) furnizează valorile implicite din classpath; proprietățile JVM le suprascriu individual. Proprietatea JVM `framework.properties` poate indica un fișier existent, deținut de consumator. Setează `ui.engine` și `reporting.engine`; [configurația Core](atf-core/README.md#configuration) enumeră opțiunile.

Înlocuiește vechiul `com.ai:test-framework` cu `com.ai:atf-core:1.0.0`, `com.ai:atf-api:1.0.0` pentru REST, adaptorul de browser necesar și opțional `com.ai:atf-reporting:1.0.0`. Fluxurile API nu au nevoie de browser sau modul de raportare. `atf-tests` conține verificările depozitului și fixture-urile produsului; nu este un API runtime pentru consumatori.

<a id="read-by-task"></a>
## Citește în funcție de sarcină

| Nevoie | Continuă cu |
|---|---|
| Înțelegerea limitelor modulelor și furnizorilor | [Arhitectură](docs/Architecture.md) |
| Selectarea verificărilor și interpretarea dovezilor | [Testare](docs/Testing.md) |
| Configurarea unui browser pentru consumator | [Playwright](atf-playwright/README.md) · [Selenium](atf-selenium/README.md) |
| Revizuirea unei schimbări de cod, test sau documentație | [Guvernanță](docs/governance/README.md) · [Indicii de depanare](docs/governance/Failure_Diagnostics.md) |
| Înțelegerea contractelor detaliate de ciclu de viață UI/API | Arhitectura vie în [Markdown](docs/Living_Architecture_UI_API_doc_v1_0.md) · [HTML](docs/Living_Architecture_UI_API_doc_v1_0.html) |
| Trasarea capabilităților sau păstrarea rapoartelor | [Harta de acoperire Atlas](atf-atlas/Coverage_Map.md) · [Artefacte](artifacts/README.md) |
| Consultarea hărții complete a documentației | [Indexul documentației](docs/README.md) |
