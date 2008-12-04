// Copyright 2007, 2008 The Apache Software Foundation
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

import com.sun.javadoc.*;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Generates an XML file that identifies all the classes that contain parameters, and all the parameters within each
 * component class. This XML is later converted into part of the Maven generated HTML site.
 * <p/>
 * To keep the -doclet parameter passed to javadoc simple, this class should not have any outside dependencies.
 */
public class ParametersDoclet extends Doclet
{
    static String OUTPUT_PATH_OPTION = "-o";

    static String outputPath;

    static class Worker
    {
        private PrintWriter out;

        private final Pattern stripper = Pattern.compile("(<.*?>|&.*?;)", Pattern.DOTALL);

        public void run(String outputPath, RootDoc root) throws Exception
        {
            File output = new File(outputPath);

            out = new PrintWriter(output);

            println("<component-parameters>");

            for (ClassDoc cd : root.classes())
                emitClass(cd);

            println("</component-parameters>");

            out.close();
        }

        private void emitClass(ClassDoc classDoc)
        {
            if (!classDoc.isPublic()) return;

            // Components must be root classes, not nested classes.
            if (classDoc.containingClass() != null) return;

            // Check for a no-args public constructor

            boolean found = false;

            for (ConstructorDoc cons : classDoc.constructors())
            {
                if (cons.isPublic() && cons.parameters().length == 0)
                {
                    found = true;
                    break;
                }
            }

            if (!found) return;
            
            Map<String, String> annotationValues = findAnnotation(classDoc, "SupportsInformalParameters");

            println("<class name=\"%s\" super-class=\"%s\"  supports-informal-parameters=\"%s\">", classDoc.qualifiedTypeName(),
                    classDoc.superclass().qualifiedTypeName(), annotationValues!=null);
            print("<description>");
            printDescription(classDoc);
            println("</description>", classDoc.commentText());

            for (FieldDoc fd : classDoc.fields())
            {
                if (fd.isStatic()) continue;

                if (!fd.isPrivate()) continue;

                annotationValues = findAnnotation(fd, "Parameter");

                if (annotationValues == null) continue;

                String name = annotationValues.get("name");
                if (name == null) name = fd.name().replaceAll("^[$_]*", "");

                print("<parameter name=\"%s\" type=\"%s\" default=\"%s\" required=\"%s\" cache=\"%s\" default-prefix=\"%s\">",
                      name, fd.type().qualifiedTypeName(), get(annotationValues, "value", ""),
                      get(annotationValues, "required", "false"), get(annotationValues, "cache", "true"),
                      get(annotationValues, "defaultPrefix", "prop"));

                // Body of a parameter is the comment text.

                printDescription(fd);

                println("\n</parameter>");
            }

            println("</class>");
        }

        private String get(Map<String, String> map, String key, String defaultValue)
        {
            if (map.containsKey(key)) return map.get(key);

            return defaultValue;
        }

        private Map<String, String> findAnnotation(ProgramElementDoc doc, String name)
        {
        	for (AnnotationDesc annotation : doc.annotations())
            {
                if (annotation.annotationType().qualifiedTypeName().equals(
                        "org.apache.tapestry5.annotations."+name))
                {
                    Map<String, String> result = new HashMap<String, String>();

                    for (ElementValuePair pair : annotation.elementValues())
                        result.put(pair.element().name(), pair.value().value().toString());

                    return result;
                }
            }

            return null;
        }

        private void print(String format, Object... arguments)
        {
            String line = String.format(format, arguments);

            out.print(line);
        }

        private void println(String format, Object... arguments)
        {
            print(format, arguments);

            out.println();
        }

        private void printDescription(Doc holder)
        {
            StringBuilder builder = new StringBuilder();

            for (Tag tag : holder.inlineTags())
            {
                if (tag.name().equals("Text"))
                {
                    builder.append(tag.text());
                    continue;
                }

                if (tag.name().equals("@link"))
                {
                    SeeTag seeTag = (SeeTag) tag;

                    String label = seeTag.label();
                    if (label != null && !label.equals(""))
                    {
                        builder.append(label);
                        continue;
                    }

                    if (seeTag.referencedClassName() != null) builder.append(seeTag.referencedClassName());

                    if (seeTag.referencedMemberName() != null)
                    {
                        builder.append("#");
                        builder.append(seeTag.referencedMemberName());
                    }
                }
            }

            String text = builder.toString();

            // Fix it up a little.

            // Remove any simple open or close tags found in the text, as well as any XML entities.

            String stripped = stripper.matcher(text).replaceAll("");

            out.print(stripped);
        }
    }

    /**
     * Yes we are interested in annotations, etc.
     */
    public static LanguageVersion languageVersion()
    {
        return LanguageVersion.JAVA_1_5;
    }

    public static int optionLength(String option)
    {
        if (option.equals(OUTPUT_PATH_OPTION)) return 2;

        return 0;
    }

    public static boolean validOptions(String options[][], DocErrorReporter reporter)
    {
        for (String[] group : options)
        {
            if (group[0].equals(OUTPUT_PATH_OPTION)) outputPath = group[1];

            // Do we need to check for other unexpected options?
            // TODO: Check for duplicate -o?
        }

        if (outputPath == null) reporter.printError(String.format("Usage: javadoc %s path", OUTPUT_PATH_OPTION));

        return true;
    }

    public static boolean start(RootDoc root)
    {
        // Enough of this static method bullshit. What the fuck were they thinking?

        try
        {
            new Worker().run(outputPath, root);
        }
        catch (Exception ex)
        {
            root.printError(ex.getMessage());

            return false;
        }

        return true;
    }

}
