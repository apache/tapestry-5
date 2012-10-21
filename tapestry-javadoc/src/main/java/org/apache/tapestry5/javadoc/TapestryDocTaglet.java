// Copyright 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.javadoc;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * An inline tag allowed inside a type; it produces Tapestry component reference and other information.
 */
public class TapestryDocTaglet implements Taglet, ClassDescriptionSource
{
    /**
     * Map from class name to class description.
     */
    private final Map<String, ClassDescription> classDescriptions = CollectionFactory.newMap();

    private ClassDoc firstSeen;

    private static final String NAME = "tapestrydoc";

    @SuppressWarnings("unchecked")
    public static void register(Map paramMap)
    {
        paramMap.put(NAME, new TapestryDocTaglet());
    }

    public boolean inField()
    {
        return false;
    }

    public boolean inConstructor()
    {
        return false;
    }

    public boolean inMethod()
    {
        return false;
    }

    public boolean inOverview()
    {
        return false;
    }

    public boolean inPackage()
    {
        return false;
    }

    public boolean inType()
    {
        return true;
    }

    public boolean isInlineTag()
    {
        return false;
    }

    public String getName()
    {
        return NAME;
    }

    public ClassDescription getDescription(String className)
    {
        ClassDescription result = classDescriptions.get(className);

        if (result == null)
        {
            // System.err.printf("*** Search for CD %s ...\n", className);

            ClassDoc cd = firstSeen.findClass(className);

            // System.err.printf("CD %s ... %s\n", className, cd == null ? "NOT found" : "found");

            result = cd == null ? new ClassDescription() : new ClassDescription(cd, this);

            classDescriptions.put(className, result);
        }

        return result;
    }

    public String toString(Tag tag)
    {
        throw new IllegalStateException("toString(Tag) should not be called for a non-inline tag.");
    }

    public String toString(Tag[] tags)
    {
        if (tags.length == 0)
            return null;

        // This should only be invoked with 0 or 1 tags. I suppose someone could put @tapestrydoc in the comment block
        // more than once.

        Tag tag = tags[0];

        try
        {
            StringWriter writer = new StringWriter(5000);

            ClassDoc classDoc = (ClassDoc) tag.holder();

            if (firstSeen == null)
                firstSeen = classDoc;

            ClassDescription cd = getDescription(classDoc.qualifiedName());

            writeClassDescription(cd, writer);

            streamXdoc(classDoc, writer);

            return writer.toString();
        }
        catch (Exception ex)
        {
            System.err.println(ex);
            System.exit(-1);

            return null; // unreachable
        }
    }

    private void element(Writer writer, String elementName, String text) throws IOException
    {
        writer.write(String.format("<%s>%s</%1$s>", elementName, text));
    }

    private void writeClassDescription(ClassDescription cd, Writer writer) throws IOException
    {
        writeParameters(cd, writer);

        writeEvents(cd, writer);
    }

    private void writeParameters(ClassDescription cd, Writer writer) throws IOException
    {
        if (cd.parameters.isEmpty())
            return;

        writer.write("</dl>"
        		+ "<table width='100%' cellspacing='0' cellpadding='3' border='1' class='parameters'>"
        		+ "<thead><tr class='TableHeadingColor' bgcolor='#CCCCFF'>"
        		+ "<th align='left' colspan='7'>"
        		+ "<font size='+2'><b>Component Parameters</b></font>"
        		+ "</th></tr>"
        		+ "<tr class='columnHeaders'>"
        		+ "<th>Name</th><th>Description</th><th>Type</th><th>Flags</th><th>Default</th>"
                + "<th>Default Prefix</th><th>Since</th>"
        		+ "</tr></thead><tbody>");

        for (String name : InternalUtils.sortedKeys(cd.parameters))
        {
            ParameterDescription pd = cd.parameters.get(name);

            writerParameter(pd, writer);
        }

        writer.write("</tbody></table></dd>");
    }

    private void writerParameter(ParameterDescription pd, Writer writer) throws IOException
    {
        writer.write("<tr>");

        element(writer, "th", pd.name);

        writer.write("<td>");
        pd.writeDescription(writer);
        writer.write("</td>");

        element(writer, "td", addWordBreaks(shortenClassName(pd.type)));

        List<String> flags = CollectionFactory.newList();

        if (pd.required)
            flags.add("Required");

        if (!pd.cache)
            flags.add("Not Cached");

        if (!pd.allowNull)
            flags.add("Not Null");

        element(writer, "td", InternalUtils.join(flags));
        element(writer, "td", addWordBreaks(pd.defaultValue));
        element(writer, "td", pd.defaultPrefix);
        element(writer, "td", pd.since);

        writer.write("</tr>");
    }

    private void writeEvents(ClassDescription cd, Writer writer) throws IOException
    {
        if (cd.events.isEmpty())
            return;

        writer.write("<p><table width='100%' cellspacing='0' cellpadding='3' border='1' class='parameters'>"
        		+ "<thead><tr class='TableHeadingColor' bgcolor='#CCCCFF'>"
        		+ "<th align='left'>"
        		+ "<font size='+2'><b>Events:</b></font></th></tr></thead></table></p><dl>");

        for (String name : InternalUtils.sortedKeys(cd.events))
        {
            element(writer, "dt", name);

            String value = cd.events.get(name);

            if (value.length() > 0)
            {
                element(writer, "dd", value);
            }
        }

        writer.write("</dl>");
    }
    
    /**
	 * Insert a <wbr/> tag after each period and colon in the given string, to
	 * allow browsers to break words at those points. (Otherwise the Parameters
	 * tables are too wide.)
	 * 
	 * @param words
	 *            any string, possibly containing periods or colons
	 * @return the new string, possibly containing <wbr/> tags
	 */
    private String addWordBreaks(String words)
    {
		return words.replace(".", ".<wbr/>").replace(":", ":<wbr/>");
    }
    
    /**
     * Shorten the given class name by removing built-in Java packages
     * (currently just java.lang)
     * 
     * @param className name of class, with package
     * @return potentially shorter class name
     */
    private String shortenClassName(String name)
    {
    	return name.replace("java.lang.", "");
    }

    private void streamXdoc(ClassDoc classDoc, Writer writer) throws Exception
    {
        File sourceFile = classDoc.position().file();

        // The .xdoc file will be adjacent to the sourceFile

        String sourceName = sourceFile.getName();

        String xdocName = sourceName.replaceAll("\\.java$", ".xdoc");

        File xdocFile = new File(sourceFile.getParentFile(), xdocName);

        if (xdocFile.exists())
        {
            try
            {
                // Close the definition list, to avoid unwanted indents. Very, very ugly.

                new XDocStreamer(xdocFile, writer).writeContent();
                // Open a new (empty) definition list, that HtmlDoclet will close.
            }
            catch (Exception ex)
            {
                System.err.println("Error streaming XDOC content for " + classDoc);
                throw ex;
            }
        }
    }
}
