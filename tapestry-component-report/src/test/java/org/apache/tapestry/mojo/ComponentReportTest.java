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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Model;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.module.xhtml.XhtmlSink;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.Assert;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.lang.reflect.Field;

/**
 * Tests {@link ComponentReport}.
 */
public class ComponentReportTest extends Assert
{
    @Test(dataProvider = "docData")
    public void doc_generation(final Map<String, ClassDescription> javadocResults, String createdFile,
                                             String[] expectedSummaryParts, String[] expectedFileParts)
            throws MojoExecutionException, IOException, MavenReportException
    {
        String tempDir = System.getProperty("java.io.tmpdir");
        final File tempFolder = new File(tempDir, "t5_tests");
        tempFolder.mkdir();

        ComponentReport report = new ComponentReport()
        {
            @Override
            protected String getOutputDirectory()
            {
                return tempFolder.toString();
            }

            @Override
            protected MavenProject getProject()
            {
                return new MavenProject(new Model());
            }

            @Override
            protected Map<String, ClassDescription> runJavadoc() throws MavenReportException
            {
                return javadocResults;
            }

            @Override
            protected List<File> createDocSearchPath()
            {
                return newList();
            }
        };
        try
        {
            initializeMojo(report, ComponentReport.class,
                    "rootPackage", "org.apache.tapestry5.corelib",
                    "apidocs", "apidocs",
                    "tapestryJavadoc", "http://tapestry.apache.org/tapestry5/apidocs",
                    "generatedDocsDirectory", tempFolder
            );
        }
        catch (NoSuchFieldException e)
        {
            fail("Cannot initialize mojo");
        }
        catch (IllegalAccessException e)
        {
            fail("Cannot initialize mojo");
        }

        StringWriter writer = new StringWriter();

        RenderingContext context = new RenderingContext(tempFolder, "test.html");
        XhtmlSink sink = new XhtmlSink(writer, context, newMap());

        report.generate(new DoxiaXhtmlSinkDecorator(sink), Locale.US);

        String summaryOutput = writer.toString();
        for (String summaryPart : expectedSummaryParts)
        {
            assertTrue(summaryOutput.contains(summaryPart));
        }

        File formReport = new File(tempFolder, createdFile);
        String formOutput = FileUtils.fileRead(formReport);

        for (String filePart : expectedFileParts)
        {
            assertTrue(formOutput.contains(filePart));
        }

        FileUtils.forceDeleteOnExit(tempFolder);
    }

    private void initializeMojo(Object mojo, Class clazz, Object... propertyValues)
            throws NoSuchFieldException, IllegalAccessException
    {
        for (int i = 0; i < propertyValues.length; i++)
        {
            String property = (String) propertyValues[i++];
            Object value = propertyValues[i];

            Field field = clazz.getDeclaredField(property);
            field.setAccessible(true);

            field.set(mojo, value);
        }
    }

    @DataProvider(name = "docData")
    private Object[][] testData() {
        return new Object[][] {
                {
                    javadocDescriptionForForm(),
                    "ref/org/apache/tapestry5/corelib/components/Form.xml",
                    new String[]{"org.apache.tapestry5.corelib.components.Form"},
                    new String[]{"<title>Component Reference: org.apache.tapestry5.corelib.components.Form</title>",
                    "<a href=\"http://tapestry.apache.org/tapestry5/apidocs/org/apache/tapestry5/EventConstants.html#PREPARE\">"}
                },
        };
    }

    private Map<String, ClassDescription> javadocDescriptionForForm()
    {
        Map<String, ClassDescription> results = newMap();
        ClassDescription classDesc = new ClassDescription(
                "org.apache.tapestry5.corelib.components.Form",
                "java.lang.Object",
                "When it renders, it fires a org.apache.tapestry5.EventConstants#PREPARE_FOR_RENDER\n" +
                " notification, followed by a org.apache.tapestry5.EventConstants#PREPARE",
                false
        );

        ParameterDescription paramDesc = new ParameterDescription(
                "validationId", "String", "", "prop", false, false, true,
                "Prefix value used when searching for validation messages and constraints. " +
                "The default is the Form component's\n" +
                " id. This is overriden by org.apache.tapestry5.corelib.components.BeanEditForm."
        );
        classDesc.getParameters().put(paramDesc.getName(), paramDesc);

        results.put(classDesc.getClassName(), classDesc);

        return results;
    }
}
