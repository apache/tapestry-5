// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.benchmarks;

import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Html5MarkupModel;
import org.apache.tapestry5.dom.MarkupModel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * The second half of every request: Tapestry renders components into a {@link Document} and then
 * serialises that tree to markup. Both halves are paid per response, so both are measured here.
 *
 * {@link #build()} is dominated by allocation, {@link #render()} by string handling and by
 * {@code MarkupModel} escaping (see {@link MarkupEscapingBenchmark}). Run with
 * {@code -Pjmh.profilers=gc} to see which of the two actually drives a page's allocation
 * footprint.
 *
 * <pre>
 * ./gradlew :tapestry-core:jmh -Pjmh.include=DocumentRender -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class DocumentRenderBenchmark
{
    @Param({"10", "200"})
    public int rows;

    private MarkupModel model;

    /** Built once; {@link #render()} must not mutate it. */
    private Document prebuilt;

    @Setup
    public void setup()
    {
        model = new Html5MarkupModel();
        prebuilt = buildDocument();
    }

    @Benchmark
    public Document build()
    {
        return buildDocument();
    }

    @Benchmark
    public String render()
    {
        return prebuilt.toString();
    }

    private Document buildDocument()
    {
        Document document = new Document(model);

        Element html = document.newRootElement("html");
        Element body = html.element("body", "class", "t-page");

        Element table = body.element("table",
                "class", "table table-striped table-hover",
                "id", "grid",
                "data-container-type", "zone");

        Element head = table.element("thead").element("tr");

        for (int column = 0; column < 6; column++)
        {
            head.element("th", "scope", "col", "class", "t-sort-column").text("Column " + column);
        }

        Element tbody = table.element("tbody");

        for (int row = 0; row < rows; row++)
        {
            Element tr = tbody.element("tr", "class", row % 2 == 0 ? "t-even" : "t-odd");

            // A mix of plain text, text needing escaping, and a link with attributes, which is
            // roughly what a Grid of business objects produces.
            tr.element("td").text("Row " + row);
            tr.element("td").text("Bread & butter");
            tr.element("td").text("<not markup>");
            tr.element("td", "class", "numeric").text(Integer.toString(row * 37));
            tr.element("td").element("a",
                    "href", "/app/item/" + row,
                    "title", "Item \"" + row + "\"",
                    "class", "t-link").text("Edit");
            tr.element("td").text("");
        }

        return document;
    }
}
