// Copyright 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.mojo;

import org.codehaus.doxia.sink.Sink;
import org.apache.maven.doxia.module.xhtml.XhtmlSink;

/**
 * Decorates an {@link XhtmlSink} so that it can be used in place of a
 * {@link Sink}.<p/>
 * Since {@link Sink} is currently deprecated, this decoratar may not be
 * needed in the future - this will happen when the signature of
 * org.apache.maven.reporting.MavenReport#generate(org.codehaus.doxia.sink.Sink, java.util.Locale)
 * (from the org.apache.maven.reporting:maven-reporting-impl artifact) changes to
 * org.apache.maven.reporting.MavenReport#generate(org.apache.maven.doxia.Sink, java.util.Locale) 
 */
public class DoxiaXhtmlSinkDecorator implements Sink {
    private XhtmlSink _xhtmlSink;

    public DoxiaXhtmlSinkDecorator(XhtmlSink xhtmlSink) {
        _xhtmlSink = xhtmlSink;
    }

    public void head() {
        _xhtmlSink.head();
    }

    public void head_() {
        _xhtmlSink.head_();
    }

    public void title() {
        _xhtmlSink.title();
    }

    public void title_() {
        _xhtmlSink.title_();
    }

    public void author_() {
        _xhtmlSink.author_();
    }

    public void date_() {
        _xhtmlSink.date_();
    }

    public void body() {
        _xhtmlSink.body();
    }

    public void body_() {
        _xhtmlSink.body_();
    }

    public void section1() {
        _xhtmlSink.section1();
    }

    public void section2() {
        _xhtmlSink.section2();
    }

    public void section3() {
        _xhtmlSink.section3();
    }

    public void section4() {
        _xhtmlSink.section4();
    }

    public void section5() {
        _xhtmlSink.section5();
    }

    public void section1_() {
        _xhtmlSink.section1_();
    }

    public void section2_() {
        _xhtmlSink.section2_();
    }

    public void section3_() {
        _xhtmlSink.section3_();
    }

    public void section4_() {
        _xhtmlSink.section4_();
    }

    public void section5_() {
        _xhtmlSink.section5_();
    }

    public void sectionTitle1() {
        _xhtmlSink.sectionTitle1();
    }

    public void sectionTitle1_() {
        _xhtmlSink.sectionTitle1_();
    }

    public void sectionTitle2() {
        _xhtmlSink.sectionTitle2();
    }

    public void sectionTitle2_() {
        _xhtmlSink.sectionTitle2_();
    }

    public void sectionTitle3() {
        _xhtmlSink.sectionTitle3();
    }

    public void sectionTitle3_() {
        _xhtmlSink.sectionTitle3_();
    }

    public void sectionTitle4() {
        _xhtmlSink.sectionTitle4();
    }

    public void sectionTitle4_() {
        _xhtmlSink.sectionTitle4_();
    }

    public void sectionTitle5() {
        _xhtmlSink.sectionTitle5();
    }

    public void sectionTitle5_() {
        _xhtmlSink.sectionTitle5_();
    }

    public void list() {
        _xhtmlSink.list();
    }

    public void list_() {
        _xhtmlSink.list_();
    }

    public void listItem() {
        _xhtmlSink.listItem();
    }

    public void listItem_() {
        _xhtmlSink.listItem_();
    }

    public void numberedList(int i) {
        _xhtmlSink.numberedList(i);
    }

    public void numberedList_() {
        _xhtmlSink.numberedList_();
    }

    public void numberedListItem() {
        _xhtmlSink.numberedListItem();
    }

    public void numberedListItem_() {
        _xhtmlSink.numberedListItem_();
    }

    public void definitionList() {
        _xhtmlSink.definitionList();
    }

    public void definitionList_() {
        _xhtmlSink.definitionList_();
    }

    public void definedTerm() {
        _xhtmlSink.definedTerm();
    }

    public void definedTerm_() {
        _xhtmlSink.definedTerm_();
    }

    public void definition() {
        _xhtmlSink.definition();
    }

    public void definition_() {
        _xhtmlSink.definition_();
    }

    public void paragraph() {
        _xhtmlSink.paragraph();
    }

    public void paragraph_() {
        _xhtmlSink.paragraph_();
    }

    public void verbatim(boolean b) {
        _xhtmlSink.verbatim(b);
    }

    public void verbatim_() {
        _xhtmlSink.verbatim_();
    }

    public void horizontalRule() {
        _xhtmlSink.horizontalRule();
    }

    public void table() {
        _xhtmlSink.table();
    }

    public void table_() {
        _xhtmlSink.table_();
    }

    public void tableRows(int[] ints, boolean b) {
        _xhtmlSink.tableRows(ints, b);
    }

    public void tableRows_() {
        _xhtmlSink.tableRows_();
    }

    public void tableRow() {
        _xhtmlSink.tableRow();
    }

    public void tableRow_() {
        _xhtmlSink.tableRow_();
    }

    public void tableCell() {
        _xhtmlSink.tableCell();
    }

    public void tableHeaderCell() {
        _xhtmlSink.tableHeaderCell();
    }

    public void tableCell(boolean b) {
        _xhtmlSink.tableCell(b);
    }

    public void tableCell(String s) {
        _xhtmlSink.tableCell(s);
    }

    public void tableHeaderCell(String s) {
        _xhtmlSink.tableHeaderCell(s);
    }

    public void tableCell(boolean b, String s) {
        _xhtmlSink.tableCell(b, s);
    }

    public void tableCell_() {
        _xhtmlSink.tableCell_();
    }

    public void tableHeaderCell_() {
        _xhtmlSink.tableHeaderCell_();
    }

    public void tableCell_(boolean b) {
        _xhtmlSink.tableCell_(b);
    }

    public void tableCaption() {
        _xhtmlSink.tableCaption();
    }

    public void tableCaption_() {
        _xhtmlSink.tableCaption_();
    }

    public void figure() {
        _xhtmlSink.figure();
    }

    public void figure_() {
        _xhtmlSink.figure_();
    }

    public void figureCaption() {
        _xhtmlSink.figureCaption();
    }

    public void figureCaption_() {
        _xhtmlSink.figureCaption_();
    }

    public void figureGraphics(String s) {
        _xhtmlSink.figureGraphics(s);
    }

    public void anchor(String s) {
        _xhtmlSink.anchor(s);
    }

    public void anchor_() {
        _xhtmlSink.anchor_();
    }

    public void link(String s) {
        _xhtmlSink.link(s);
    }

    public void link(String s, String s1) {
        _xhtmlSink.link(s, s1);
    }

    public void link_() {
        _xhtmlSink.rawText("</a>");
        //_xhtmlSink.link_();
    }

    public void italic() {
        _xhtmlSink.italic();
    }

    public void italic_() {
        _xhtmlSink.italic_();
    }

    public void bold() {
        _xhtmlSink.bold();
    }

    public void bold_() {
        _xhtmlSink.bold_();
    }

    public void monospaced() {
        _xhtmlSink.monospaced();
    }

    public void monospaced_() {
        _xhtmlSink.monospaced_();
    }

    public void lineBreak() {
        _xhtmlSink.lineBreak();
    }

    public void nonBreakingSpace() {
        _xhtmlSink.nonBreakingSpace();
    }

    public void text(String s) {
        _xhtmlSink.text(s);
    }

    public void rawText(String s) {
        _xhtmlSink.rawText(s);
    }

    public void flush() {
        _xhtmlSink.flush();
    }

    public void close() {
        _xhtmlSink.close();
    }

    public void definitionListItem() {
        _xhtmlSink.definitionListItem();
    }

    public void definitionListItem_() {
        _xhtmlSink.definitionListItem_();
    }

    public void author() {
        _xhtmlSink.author();
    }

    public void date() {
        _xhtmlSink.date();
    }

    public void sectionTitle() {
        _xhtmlSink.sectionTitle();
    }

    public void sectionTitle_() {
        _xhtmlSink.sectionTitle_();
    }

    public void pageBreak() {
        _xhtmlSink.pageBreak();
    }
}
