<a id="atlas-evidence-and-local-report-publication"></a>
# Dovezi Atlas și publicarea locală a rapoartelor

Referință inițială: `e82c9a734c03480be7f7fd14330a370b4bd3d2d3`. La acest reper înregistrat,
responsabilul a selectat explicit directorul de artefacte ATF_AI live pentru copiile generate
ale rapoartelor și analizele Atlas. Compilările rămân izolate sub rădăcina externă de lucru. Păstrați
schimbările existente ale surselor J1; hărțile generate rămân în afara perimetrului său întreținut
docs/lib. Nu se adaugă parcursuri E2E, emulatoare sau funcționalități Java de execuție.

<a id="d-004--discoverable-local-test-evidence"></a>
## D-004 — dovezi locale de testare ușor de găsit

- Responsabil: ghidurile artefactelor, documentația de testare și publicarea arhivei locale.
- Prioritate: 85, din impact/risc/urgență 90/80/80.
- Calitate inițială: 58, din corectitudine/verificare/claritate 70/45/55.
- Observație inițială: rapoartele finalizate existau într-o zonă externă de pregătire, în timp ce
  zona live de artefacte solicitată conținea doar ghiduri sursă. Formularea ei de atunci
  descuraja orice copie de raport generat în copia de lucru live.
- Acceptanță: păstrarea compilărilor izolate, susținerea explicită a copiilor de arhivă live
  selectate de apelant, păstrarea resurselor relative la sursă și a SHA-256, furnizarea unui index
  local al rapoartelor și a README-urilor de testare actualizate. Păstrarea dovezilor eșuate/vechi și
  distingerea copierii de execuția nouă.
- Calitate livrată: 95, din corectitudine/verificare/claritate 95/94/97.
- Stare: VERIFIED pentru publicarea arhivei finalizate selectate.
- Dovezi: publicarea inițială a copiat 19 pachete finalizate și 3,660 de fișiere păstrate în
  zona locală de rapoarte ignorată selectată și a recalculat hash-urile față de manifestele lor
  originale. Indexul generat și README-ul de testare distingeau rezultatele noi,
  încercările eșuate și istoricul păstrat. Resursele rapoartelor relative la sursă rămân
  împreună. Acel reper folosea artifacts/reports/runs/index.html în copia de lucru
  selectată; structura istorică completă este acum păstrată extern.
  [Responsabilul selecției curente și al istoricului](../../artifacts/reports/README.md#current-selection-and-history)
  descrie publicația de astăzi pentru cititor. Înregistrările ulterioare își păstrează propriile pachete
  și numere fără a rescrie rezultatele originale ale acestui reper.

<a id="g-009--verify-generated-atlas-against-its-actual-target"></a>
## G-009 — verificarea Atlas-ului generat față de ținta sa reală

- Responsabil: colectarea inițială/șablonul Atlas, verificarea de întreținere și guvernanța existentă.
- Prioritate: 89, din impact/risc/urgență 95/85/80.
- Calitate inițială: 55, din corectitudine/verificare/claritate 65/35/60.
- Observație: ghidurile Atlas întreținute sunt guvernate, dar datele generate ale rulărilor sunt
  intenționat în afara guvernanței surselor. O hartă de proiect formatată nu
  verifică singură identitatea sursei, legătura cu testele, citatele sau divergența live. Perimetrul bibliotecilor binare
  J1 nu trebuie înlocuit cu arhitectura modulelor sursă AI.
- Acceptanță: generarea unor hărți distincte, legate de țintă, a unor indici de surse și a unor limite
  de dovadă pentru AI/J1; validarea hash-urilor declarate ale surselor/documentelor/dovezilor și
  a referințelor exacte la surse. Rezultatele guvernanței existente își păstrează ținta/domeniul
  real. Respingerea surselor schimbate, a identității între proiecte și a afirmațiilor de execuție
  nesusținute. Păstrarea schimbărilor J1 în așteptare și a necunoscutelor explicite.
- Calitate livrată: 93, din corectitudine/verificare/claritate 95/92/92.
- Stare: VERIFIED pentru consistența declarată sursă/artefact și revizia cu domeniu delimitat.
- Dovezi: hărțile AI/J1 separate leagă propriile grafuri de module POM, rădăcini de surse,
  limite de responsabilitate, fragmente exacte de surse/guvernanță și dovezi de execuție.
  Fiecare include cinci documente Markdown/HTML și o diagramă statică a modulelor.
  AI deține cinci module de biblioteci sursă/teste; J1 are cinci module locale diferite
  și consumă șase perechi de biblioteci binare versionate. Hărțile J1 rămân în afara arborelui său
  întreținut docs/lib; lucrul J1 existent a fost păstrat.
- Limita dovezii: o mapare de guvernanță asociată nu reprezintă acoperirea completă a fiecărei
  clauze. Declarațiile nemapate sunt explicite. Compararea surselor înregistrează
  schimbările de la execuție; nu certifică modificările ulterioare. Verificatorul validează
  fișierele, căile, citatele, hash-urile, rezultatele XML/TAP și legăturile cu sursele declarate,
  în timp ce completitudinea semantică necesită revizie.

<a id="validation-receipt"></a>
## Înregistrarea validării

- AI EN și RO: `modules-atlas-main-governance-02` și
  `modules-atlas-ro-governance-02`, fiecare cu 211 teste, zero eșecuri/erori/omiteri,
  din ediții sursă/pereche înghețate. Sursa EN: `17afeed7af4952d10fd12094a216ea2da5d01f1c`.
  Încercările anterioare sunt păstrate: schimbarea instrumentelor ediției pereche în timpul execuției
  a declanșat corect constatări de paritate a surselor între ediții. Nicio regulă nu a fost relaxată.
- J1: `j1-atlas-governance-02`, 205 teste, zero eșecuri/erori/omiteri, din
  sursa capturată cu modificări locale, bazată pe `4e3cac10061c95ea2b9b32cc18a6d4add1007a1e`.
  Încercarea sa anterioară a păstrat un eșec de izolare a descoperirii Git deoarece TMP
  era în interiorul depozitului clonat; rularea corectată a plasat TMP în afara Git.
  Modificările locale J1 ulterioare sunt înregistrate separat de acest instantaneu reușit.
- Întreținere Atlas: `atlas-verifier-06`, 12 teste trecute. Controalele negative resping
  divergența surselor/documentelor, citatele/intervalele modificate, confuzia între ținte, căile care ies din domeniul permis,
  dovezile de execuție nesusținute, contoarele inconsistente și octeții executați schimbați.
  Structura Surefire reală, mixtă, cu elemente autoînchise și elemente cu corp, găsită în timpul colectării,
  are un test de regresie. Echivalența explicită CRLF/LF nu poate normaliza artefactele lib
  publicate și nu poate ascunde o schimbare de conținut.
  Rezultatele TAP individuale, plate, resping și un test eșuat ale cărui contoare de rezumat
  au fost ambele modificate pentru a pretinde succesul. Cele opt verificări existente ale colectorului au fost
  rerulate cu succes ca `atlas-collector-01`.
- Revizia documentației statice a randat zece documente HTML plus ambii indici în
  Chromium desktop real, fără emulare de dispozitiv. Diagramele s-au încărcat, paginile nu aveau
  depășire orizontală sau erori de script, iar detaliile dovezilor s-au deschis. Inspecția
  vizuală a corectat o săgeată directă de dependență AI care se suprapunea. Revizia interactivă TAB
  nu a fost disponibilă după două expirări ale serviciului; nu se revendică acceptanță
  interactivă. Un instantaneu sursă ulterior necesită revalidarea propriei hărți.
- HTML-ul de componente J1 păstrat este istoric: verificările sale selectate și observația agregată
  necalificată își păstrează înregistrarea originală de calificare. Copierea nu
  stabilește o execuție nouă API/rețea sau de browser. Suitele AI anterioare de 550 de verificări
  și pachetele de calificare a browserelor sunt, de asemenea, istorice în această sarcină.

Atlas rămâne un protocol de documentație/guvernanță. Orice verificator adăugat este un instrument
opțional de întreținere, nu un analizor la execuție sau o dovadă semantică universală.
