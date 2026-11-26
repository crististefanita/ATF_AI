<a id="framework-documentation"></a>
# Documentația framework-ului

Alege ghidul în funcție de decizia necesară. Documentul principal explică framework-ul;
ghidurile responsabililor păstrează contractele detaliate, iar feedback-ul descrie lacune reproductibile.

<a id="find-the-owner"></a>
## Găsește responsabilul

| Întrebarea cititorului | Începe aici |
|---|---|
| Ce este ATF și de ce bibliotecă am nevoie? | [Prezentarea principală](../README.md) |
| Cine gestionează un API, furnizor sau o limită de dependență? | [Arhitectură](Architecture.md) |
| Unde găsesc rețete executabile fără să ascundă strategia? | [Ghidul comenzilor pliate](Testing_Commands.md) |
| Ce verificări existente demonstrează comportamentul modificat? | [Testare](Testing.md) și [harta de acoperire Atlas](../atf-atlas/Coverage_Map.md) |
| Unde trebuie să stea modificările, copiile de validare, cache-urile și dovezile? | [Dezvoltare](Development.md) |
| Cum se revizuiesc codul, testele, README-urile și diagramele? | [Politicile de guvernanță](governance/README.md) |
| Cum funcționează ciclul UI/API, pașii și contractele de raportare? | Arhitectura vie în [Markdown](Living_Architecture_UI_API_doc_v1_0.md) și [HTML](Living_Architecture_UI_API_doc_v1_0.html) |
| Ce criterii de acceptanță sunt încă deschise? | [Feedback și TODO-uri](feedback/README.md) |
| Cum se păstrează rapoartele și se descrie alt proiect? | [Artefacte](../artifacts/README.md) și [protocolul Atlas reutilizabil](../artifacts/atlas/README.md) |

<a id="module-owners"></a>
## Responsabilii modulelor

| Modul | Document responsabil |
|---|---|
| Contracte neutre, configurație, ciclu UI și execuție API | [atf-core](../atf-core/README.md) |
| Implementarea Playwright | [atf-playwright](../atf-playwright/README.md) |
| Implementarea Selenium | [atf-selenium](../atf-selenium/README.md) |
| Raportare Extent și ciclu de viață | [atf-reporting](../atf-reporting/README.md) |
| Guvernanță, verificări de framework și fixture-uri de produs | [atf-tests](../atf-tests/README.md) |

Documentele responsabililor descriu contractele curente. Înregistrările de feedback păstrează
puncte de control ale sursei, comenzi și dovezi externe; rezultatele istorice își păstrează
limitele originale ale execuției.

<a id="documentation-boundaries"></a>
## Limitele documentației

[Politica pentru cod și documentație](governance/Code_And_Documentation_Policy.md) definește regulile de revizuire.

- README-urile modulelor descriu utilizarea și verificarea; ghidurile pachetelor explică responsabilitățile locale.
- [Atlasul](../atf-atlas/README.md) separat leagă capabilitățile de sursele și testele existente și de limitele dovezilor.
- Trimite către acești responsabili în loc să copiezi contractele într-un alt ghid.
