package com.ai.atf.core.support.html;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.html.HtmlTable;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.HTML_TABLE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.HTML_TABLE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class HtmlTableContractTest {
    @Test
    public void tableEscapesCellsAndRendersExpectedShape() {
        String html = HtmlTable.withHeadings("Name", "Value")
                .row("unsafe <tag>", "A&B")
                .codeRow("id", "x > y")
                .row(HtmlTable.Cell.classed("pass", "PASS"), HtmlTable.Cell.code("artifact.html"))
                .render();

        Assert.assertTrue(html.contains("<th>Name</th>"));
        Assert.assertTrue(html.contains("unsafe &lt;tag&gt;"));
        Assert.assertTrue(html.contains("A&amp;B"));
        Assert.assertTrue(html.contains("<code>x &gt; y</code>"));
        Assert.assertTrue(html.contains("<td class=\"pass\">PASS</td>"));
        Assert.assertTrue(html.contains("<td><code>artifact.html</code></td>"));
        Assert.assertTrue(html.endsWith("</tbody></table>"));
    }

    @Test
    public void tableRejectsRowsThatDoNotMatchHeadingCount() {
        HtmlTable table = HtmlTable.withHeadings("A", "B");

        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> table.row("only-one"));

        Assert.assertTrue(error.getMessage().contains("1 cells"));
        Assert.assertTrue(error.getMessage().contains("2 headings"));
    }

    @Test
    public void tableRejectsTypedRowsThatDoNotMatchHeadingCount() {
        HtmlTable table = HtmlTable.withHeadings("A", "B");

        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> table.row(HtmlTable.Cell.code("only-one")));

        Assert.assertTrue(error.getMessage().contains("1 cells"));
        Assert.assertTrue(error.getMessage().contains("2 headings"));
    }
}
