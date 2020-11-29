// Copyright 2011-2013 The Apache Software Foundation
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

import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Taglet;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An inline tag allowed inside a type; it produces Tapestry component reference and other information.
 */
public class TapestryDocTaglet implements Taglet, ClassDescriptionSource
{
    private DocletEnvironment env;
    private Doclet doclet;

    /**
     * Map from class name to class description.
     */
    private final Map<String, ClassDescription> classDescriptions = CollectionFactory.newMap();

    private final Set<Location> allowedLocations = CollectionFactory.newSet(Location.TYPE);

    private Element firstSeen;

    private static final String NAME = "tapestrydoc";

    @SuppressWarnings({"unused", "unchecked"})
    public static void register(Map paramMap)
    {
        paramMap.put(NAME, new TapestryDocTaglet());
    }

    @Override
    public void init(DocletEnvironment env, Doclet doclet)
    {
        this.env = env;
        this.doclet = doclet;
    }

    @Override
    public Set<Location> getAllowedLocations()
    {
        return allowedLocations;
    }

    @Override
    public boolean isInlineTag()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String toString(List<? extends DocTree> tags, Element element)
    {
        if (tags.size() == 0)
            return null;

        // This should only be invoked with 0 or 1 tags. I suppose someone could put @tapestrydoc in the comment block
        // more than once.

        DocTree tag = tags.get(0);

        try
        {
            StringWriter writer = new StringWriter(5000);

            TypeElement classDoc = (TypeElement) element;

            if (firstSeen == null)
                firstSeen = classDoc;

            ClassDescription cd = getDescription(classDoc.getQualifiedName().toString());

            writeClassDescription(cd, writer);

            streamXdoc(classDoc, writer);

            return writer.toString();
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err);
            System.exit(-1);

            return null; // unreachable
        }
    }

    @Override
    public ClassDescription getDescription(String className)
    {
        ClassDescription result = classDescriptions.get(className);

        if (result == null)
        {
            // System.err.printf("*** Search for CD %s ...\n", className);

            TypeElement cd = env.getElementUtils().getTypeElement(className);

            // System.err.printf("CD %s ... %s\n", className, cd == null ? "NOT found" : "found");

            result = cd == null ? new ClassDescription(env) : new ClassDescription(cd, this, env);

            classDescriptions.put(className, result);
        }

        return result;
    }

    private void writeElement(Writer writer, String elementSpec, String text) throws IOException
    {
        String elementName = elementSpec;
        int idxOfSpace = elementSpec.indexOf(' ');
        if (idxOfSpace != -1)
        {
            elementName = elementSpec.substring(0, idxOfSpace);
        }
        writer.write(String.format("<%s>%s</%s>", elementSpec,
                InternalUtils.isBlank(text) ? "&nbsp;" : text, elementName));
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
                + "<table class='parameters'>"
                + "<caption><span>Component Parameters</span><span class='tabEnd'>&nbsp;</span></caption>"
                + "<tr class='columnHeaders'>"
                + "<th class='colFirst'>Name</th><th>Type</th><th>Flags</th><th>Default</th>"
                + "<th class='colLast'>Default Prefix</th>"
                + "</tr><tbody>");

        int toggle = 0;
        for (String name : InternalUtils.sortedKeys(cd.parameters))
        {
            ParameterDescription pd = cd.parameters.get(name);

            writerParameter(pd, alternateCssClass(toggle++), writer);
        }

        writer.write("</tbody></table></dd>");
    }

    private void writerParameter(ParameterDescription pd, String rowClass, Writer writer) throws IOException
    {
        String description = pd.extractDescription();

        writer.write("<tr class='values " + rowClass + "'>");
        writer.write("<td" + (StringUtils.isEmpty(description) ? "" : " rowspan='2'") + " class='colFirst'>");
        writer.write(pd.name);
        writer.write("</td>");

        writeElement(writer, "td", addWordBreaks(shortenClassName(pd.type)));

        List<String> flags = CollectionFactory.newList();

        if (pd.required)
        {
            flags.add("Required");
        }

        if (!pd.cache)
        {
            flags.add("Not Cached");
        }

        if (!pd.allowNull)
        {
            flags.add("Not Null");
        }

        if (InternalUtils.isNonBlank(pd.since)) {
            flags.add("Since " + pd.since);
        }

        writeElement(writer, "td", InternalUtils.join(flags));
        writeElement(writer, "td", addWordBreaks(pd.defaultValue));
        writeElement(writer, "td class='colLast'", pd.defaultPrefix);

        writer.write("</tr>");

        if (StringUtils.isNotEmpty(description))
        {
            writer.write("<tr class='" + rowClass + "'>");
            writer.write("<td colspan='4' class='description colLast'>");
            writer.write(description);
            writer.write("</td>");
            writer.write("</tr>");
        }
    }

    /**
     * Return alternating CSS class names based on the input, which the caller
     * should increment with each call.
     */
    private String alternateCssClass(int num) {
        return num % 2 == 0 ? "altColor" : "rowColor";
    }

    private void writeEvents(ClassDescription cd, Writer writer) throws IOException
    {
        if (cd.events.isEmpty())
            return;

        writer.write("<p><table class='parameters'>"
                + "<caption><span>Component Events</span><span class='tabEnd'>&nbsp;</span></caption>"
                + "<tr class='columnHeaders'>"
                + "<th class='colFirst'>Name</th><th class='colLast'>Description</th>"
                + "</tr><tbody>");

        int toggle = 0;
        for (String name : InternalUtils.sortedKeys(cd.events))
        {
            writer.write("<tr class='" + alternateCssClass(toggle++) + "'>");
            writeElement(writer, "td class='colFirst'", name);

            String value = cd.events.get(name);

            writeElement(writer, "td class='colLast'", value);

            writer.write("</tr>");
        }

        writer.write("</table></p>");
    }

    /**
     * Insert a <wbr/> tag after each period and colon in the given string, to
     * allow browsers to break words at those points. (Otherwise the Parameters
     * tables are too wide.)
     *
     * @param words
     *         any string, possibly containing periods or colons
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
     * @param name
     *         name of class, with package
     * @return potentially shorter class name
     */
    private String shortenClassName(String name)
    {
        return name.replace("java.lang.", "");
    }

    private void streamXdoc(TypeElement classDoc, Writer writer) throws Exception
    {
        JavaFileObject sourceFileObject = env.getJavaFileManager()
                .getJavaFileForInput(StandardLocation.SOURCE_PATH,
                        classDoc.getQualifiedName().toString(),
                        JavaFileObject.Kind.SOURCE);

        File sourceFile;
        if (sourceFileObject == null) {
            final String path = "./tapestry-core/src/main/java/" + 
                    classDoc.getQualifiedName().toString().replace('.', '/') +
                    ".java";
            System.err.println("[WARNING] Source file object not found for " 
                    + classDoc.getQualifiedName().toString()
                    + ", so we'll guess it's in " + path);
            sourceFile = new File(path);
        }
        else {
            sourceFile = new File(sourceFileObject.toUri());
        }

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
            } catch (Exception ex)
            {
                System.err.println("Error streaming XDOC content for " + classDoc);
                throw ex;
            }
        }
    }
}
