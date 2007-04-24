// Copyright 2007 The Apache Software Foundation
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

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

/**
 * Generates an XML file that identifies all the classes that contain parameters, and all the
 * parameters within each component class. This XML is later converted into part of the Maven
 * generated HTML site.
 * <p>
 * To keep the -doclet parameter passed to javadoc simple, this class should not have any outside
 * dependencies.
 * <p>
 * Works in two passes: First we find any classes that have a field that has the parameter
 * annotation. Second we locate any subclasses of the initial set of classes, regardless of whether
 * they have a parameter or not.
 */
public class ParametersDoclet extends Doclet
{
    static String OUTPUT_PATH_OPTION = "-o";

    static String _outputPath;

    static class Worker
    {
        private PrintWriter _out;

        private RootDoc _root;

        private final Set<ClassDoc> _processed = new HashSet<ClassDoc>();

        private final Pattern _stripper = java.util.regex.Pattern.compile(
                "(<.*?>|&.*?;)",
                Pattern.DOTALL);

        public void run(String outputPath, RootDoc root) throws Exception
        {
            _root = root;

            File output = new File(outputPath);

            _out = new PrintWriter(output);

            println("<component-parameters>");

            for (ClassDoc cd : root.classes())
            {
                emitClass(cd, false);
            }

            for (ClassDoc potential : _root.classes())
            {
                for (ClassDoc potentialParent : _processed)
                {
                    if (potential.subclassOf(potentialParent))
                    {
                        emitClass(potential, true);
                        break;
                    }
                }
            }

            println("</component-parameters>");

            _out.close();
        }

        private void emitClass(ClassDoc classDoc, boolean forceClassOutput)
        {
            if (_processed.contains(classDoc)) return;

            if (!classDoc.isPublic()) return;

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

            boolean wroteClass = false;

            for (FieldDoc fd : classDoc.fields())
            {
                if (fd.isStatic()) continue;

                if (!fd.isPrivate()) continue;

                Map<String, String> annotationValues = findParameterAnnotation(fd);

                if (annotationValues == null) continue;

                if (!wroteClass)
                {
                    printClassDescriptionStart(classDoc);
                    wroteClass = true;
                }

                String name = annotationValues.get("name");
                if (name == null) name = fd.name().replaceAll("^[$_]*", "");

                print(
                        "<parameter name=\"%s\" type=\"%s\" default=\"%s\" required=\"%s\" cache=\"%s\" default-prefix=\"%s\">",
                        name,
                        fd.type().qualifiedTypeName(),
                        get(annotationValues, "value", ""),
                        get(annotationValues, "required", "false"),
                        get(annotationValues, "cache", "true"),
                        get(annotationValues, "defaultPrefix", "prop"));

                // Body of a parameter is the comment text.

                printDescription(fd);

                println("\n</parameter>");
            }

            if (wroteClass)
                println("</class>");
            else if (forceClassOutput)
            {
                printClassDescriptionStart(classDoc);
                println("</class>");
            }

            if (wroteClass || forceClassOutput) _processed.add(classDoc);

        }

        private void printClassDescriptionStart(ClassDoc classDoc)
        {
            println(
                    "<class name=\"%s\" super-class=\"%s\">",
                    classDoc.qualifiedTypeName(),
                    classDoc.superclass().qualifiedTypeName());
            print("<description>");
            printDescription(classDoc);
            println("</description>", classDoc.commentText());
        }

        private String get(Map<String, String> map, String key, String defaultValue)
        {
            if (map.containsKey(key)) return map.get(key);

            return defaultValue;
        }

        private Map<String, String> findParameterAnnotation(FieldDoc fd)
        {
            for (AnnotationDesc annotation : fd.annotations())
            {
                if (annotation.annotationType().qualifiedTypeName().equals(
                        "org.apache.tapestry.annotations.Parameter"))
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

            _out.print(line);
        }

        private void println(String format, Object... arguments)
        {
            print(format, arguments);

            _out.println();
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

                    if (seeTag.referencedClassName() != null)
                        builder.append(seeTag.referencedClassName());

                    if (seeTag.referencedMemberName() != null)
                    {
                        builder.append("#");
                        builder.append(seeTag.referencedMemberName());
                    }

                    continue;
                }
            }

            String text = builder.toString();

            // Fix it up a little.

            // Remove any simple open or close tags found in the text, as well as any XML entities.

            String stripped = _stripper.matcher(text).replaceAll("");

            _out.print(stripped);
        }
    }

    /** Yes we are interested in annotations, etc. */
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
            if (group[0].equals(OUTPUT_PATH_OPTION)) _outputPath = group[1];

            // Do we need to check for other unexpected options?
            // TODO: Check for duplicate -o?
        }

        if (_outputPath == null)
            reporter.printError(String.format("Usage: javadoc %s path", OUTPUT_PATH_OPTION));

        return true;
    }

    public static boolean start(RootDoc root)
    {
        // Enough of this static method bullshit. What the fuck were they thinking?

        try
        {
            new Worker().run(_outputPath, root);
        }
        catch (Exception ex)
        {
            root.printError(ex.getMessage());

            return false;
        }

        return true;
    }

}
