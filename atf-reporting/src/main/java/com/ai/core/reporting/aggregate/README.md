<a id="aggregate-reporting"></a>
# Raportare agregată

<a id="overview"></a>
## Prezentare generală

Publicarea agregată din [atf-reporting](../../../../../../../../README.md)
construiește un raport din înregistrările cazurilor executate, numărători,
temporizări și un descriptor gestionat de apelant.

<a id="entry-points"></a>
## Puncte de intrare

- [AggregateReportDescriptor](AggregateReportDescriptor.java): identitatea și metadatele raportului furnizate de apelant.
- [AggregateExtentReportWriter](AggregateExtentReportWriter.java): punctul de intrare pentru agregarea Extent.
- [AggregateExtentPublicationPipeline](AggregateExtentPublicationPipeline.java): publicarea ordonată a secțiunilor raportului și a dovezilor.

Biblioteca publică înregistrările observate.
[Infrastructura de agregare a testelor](../../../../../../../../../atf-tests/src/test/java/com/ai/atf/core/reporting/aggregate)
colectează cazurile framework-ului prin ascultătorul său TestNG și furnizează descriptorul.
Publicarea păstrează dovezile rezultatelor și temporizării; nu deduce acceptanța
din rapoarte istorice.

<a id="verification"></a>
## Verificare

Rulează `mvn -B -ntp -Paggregate test` dintr-o copie izolată a reactorului. Profilul
selectează suita menținută de agregare din atf-tests și publică atât Extent,
cât și Allure. Locația implicită a rezultatului agregat este
atf-tests/target/atf-core-aggregate-report.

Verifică numărătorile, stările, temporizările și atașamentele generate. Comenzile complete,
locațiile artefactelor și regulile pentru dovezi aparțin ghidului
[Testare](../../../../../../../../../docs/Testing.md).
