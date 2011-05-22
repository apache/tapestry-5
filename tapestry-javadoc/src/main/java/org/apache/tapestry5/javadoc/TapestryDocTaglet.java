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
            ClassDoc cd = firstSeen.findClass(className);
            result = new ClassDescription(cd, this);
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

        writer.write("<dt><b>Parameters:</b></dt><dd>");

        writer.write("<table border='1' cellpadding='3' cellspacing='0'>"
                + "<tr><th>Name</th><th>Type</th><th>Flags</th><th>Default</th><th>Default Prefix</th><th>Since</th><th>Description</th></tr>");

        for (String name : InternalUtils.sortedKeys(cd.parameters))
        {
            ParameterDescription pd = cd.parameters.get(name);

            writerParameter(pd, writer);
        }

        writer.write("</table></dd>");
    }

    private void writerParameter(ParameterDescription pd, Writer writer) throws IOException
    {
        writer.write("<tr>");

        element(writer, "td", pd.name);
        element(writer, "td", pd.type);

        List<String> flags = CollectionFactory.newList();

        if (pd.required)
            flags.add("Required");

        if (!pd.cache)
            flags.add("NOT Cached");

        if (!pd.allowNull)
            flags.add("NOT Allow Null");

        element(writer, "td", InternalUtils.join(flags));
        element(writer, "td", pd.defaultValue);
        element(writer, "td", pd.defaultPrefix);
        element(writer, "td", pd.since);

        writer.write("<td>");

        pd.writeDescription(writer);

        writer.write("</td></tr>");
    }

    private void writeEvents(ClassDescription cd, Writer writer) throws IOException
    {
        if (cd.events.isEmpty())
            return;

        writer.write("<dt><b>Events:</b></dt><dd><dl>");

        for (String name : InternalUtils.sortedKeys(cd.events))
        {
            element(writer, "dt", name);

            String value = cd.events.get(name);

            if (value.length() > 0)
            {
                element(writer, "dd", value);
            }
        }

        writer.write("</dl></dd>");
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
