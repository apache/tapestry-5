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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;

/**
 * The component report generates documentation about components and parameters within the current
 * project.
 * 
 * @goal component-report
 * @requiresDependencyResolution compile
 * @execute phase="generate-sources"
 */
public class ComponentReport extends AbstractMavenReport
{
    /**
     * Identifies the application root package.
     * 
     * @parameter
     * @required
     */
    private String rootPackage;

    /**
     * The Maven Project Object
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Generates the site report
     * 
     * @component
     */
    private SiteRenderer siteRenderer;

    /**
     * Location of the generated site.
     * 
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     */
    private String outputDirectory;

    /**
     * Working directory for temporary files.
     * 
     * @parameter default-value="target"
     * @required
     */
    private String workDirectory;

    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    public String getDescription(Locale locale)
    {
        return "Tapestry component parameter reference documentation";
    }

    public String getName(Locale locale)
    {
        return "Component Reference";
    }

    public String getOutputName()
    {
        return "component-parameters";
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException
    {
        Map<String, ClassDescription> descriptions = runJavadoc();

        getLog().info("Executing ComponentReport ...");

        Sink sink = getSink();

        sink.section1();
        sink.sectionTitle1();
        sink.text("Component Index");
        sink.sectionTitle1_();
        sink.list();

        for (String className : InternalUtils.sortedKeys(descriptions))
        {
            String simpleName = InternalUtils.lastTerm(className);

            sink.listItem();

            // Something is convertin the name attribute of the anchors to lower case, so
            // we'll follow suit.

            sink.link("#" + className.toLowerCase());
            sink.text(simpleName);
            sink.link_();

            sink.listItem_();
        }

        sink.list_();

        for (String className : InternalUtils.sortedKeys(descriptions))
        {
            writeClassDescription(descriptions, sink, className);
        }

    }

    private void writeClassDescription(Map<String, ClassDescription> descriptions, Sink sink,
            String className)
    {
        ClassDescription cd = descriptions.get(className);

        Map<String, ParameterDescription> parameters = newMap(cd.getParameters());
        List<String> parents = newList();

        String current = cd.getSuperClassName();

        while (true)
        {
            ClassDescription superDescription = descriptions.get(current);

            if (superDescription == null) break;

            parents.add(current);
            parameters.putAll(superDescription.getParameters());

            current = superDescription.getSuperClassName();
        }

        Collections.reverse(parents);

        sink.section2();

        sink.sectionTitle2();
        sink.anchor(className);
        sink.text(className);
        sink.anchor_();

        sink.sectionTitle2_();

        sink.paragraph();
        sink.text(cd.getDescription());
        sink.paragraph_();

        sink.paragraph();

        String javadocURL = String.format("apidocs/%s.html", className.replace('.', '/'));

        sink.link(javadocURL);
        sink.text("[JavaDoc]");
        sink.link_();

        sink.paragraph_();

        if (!parents.isEmpty())
        {
            sink.sectionTitle3();
            sink.text("Component inheritance");
            sink.sectionTitle3_();

            sink.list();
            sink.listItem();

            for (String name : parents)
            {
                sink.link("#" + name.toLowerCase());
                sink.text(name);
                sink.link_();

                sink.list();
                sink.listItem();
            }

            sink.text(className);

            for (int i = 0; i <= parents.size(); i++)
            {
                sink.listItem_();
                sink.list_();
            }
        }

        if (!parameters.isEmpty())
        {
            List<String> flags = newList();

            sink.sectionTitle3();
            sink.text("Parameters");
            sink.sectionTitle3_();

            sink.table();
            sink.tableRow();

            for (String header : PARAMETER_HEADERS)
            {
                sink.tableHeaderCell();
                sink.text(header);
                sink.tableHeaderCell_();
            }

            sink.tableRow_();

            for (String name : InternalUtils.sortedKeys(parameters))
            {
                ParameterDescription pd = parameters.get(name);

                flags.clear();
                if (pd.getRequired()) flags.add("Required");

                if (!pd.getCache()) flags.add("NOT Cached");

                sink.tableRow();

                cell(sink, pd.getName());
                cell(sink, pd.getType());
                cell(sink, InternalUtils.join(flags));
                cell(sink, pd.getDefaultValue());
                cell(sink, pd.getDefaultPrefix());
                cell(sink, pd.getDescription());

                sink.tableRow_();

            }

            sink.table_();
        }

        sink.section2_();
    }

    private void cell(Sink sink, String value)
    {
        sink.tableCell();
        sink.text(value);
        sink.tableCell_();
    }

    private final static String[] PARAMETER_HEADERS =
    { "Name", "Type", "Flags", "Default", "Default Prefix", "Description" };

    private Map<String, ClassDescription> runJavadoc() throws MavenReportException
    {
        getLog().info("Running JavaDoc to collection component parameter data ...");

        Commandline command = new Commandline();

        try
        {
            command.setExecutable(pathToJavadoc());
        }
        catch (IOException ex)
        {
            throw new MavenReportException("Unable to locate javadoc command: " + ex.getMessage(),
                    ex);
        }

        String parametersPath = workDirectory + File.separator + "component-parameters.xml";

        String[] arguments =
        { "-private", "-o", parametersPath,

        "-subpackages", rootPackage,

        "-doclet", ParametersDoclet.class.getName(),

        "-docletpath", docletPath(),

        "-sourcepath", sourcePath(),

        "-classpath", classPath() };

        command.addArguments(arguments);

        executeCommand(command);

        return readXML(parametersPath);
    }

    @SuppressWarnings("unchecked")
    private String sourcePath()
    {
        List<String> roots = (List<String>) project.getCompileSourceRoots();

        return toArgumentPath(roots);
    }

    /**
     * Needed to help locate this plugin's local JAR file for the -doclet argument.
     * 
     * @parameter default-value="${localRepository}"
     * @read-only
     */
    private ArtifactRepository localRepository;

    /**
     * Needed to help locate this plugin's local JAR file for the -doclet argument.
     * 
     * @parameter default-value="${plugin.groupId}"
     * @read-only
     */
    private String pluginGroupId;

    /**
     * Needed to help locate this plugin's local JAR file for the -doclet argument.
     * 
     * @parameter default-value="${plugin.artifactId}"
     * @read-only
     */
    private String pluginArtifactId;

    /**
     * Needed to help locate this plugin's local JAR file for the -doclet argument.
     * 
     * @parameter default-value="${plugin.version}"
     * @read-only
     */
    private String pluginVersion;

    @SuppressWarnings("unchecked")
    private String docletPath() throws MavenReportException
    {
        File file = new File(localRepository.getBasedir());

        for (String term : pluginGroupId.split("\\."))
            file = new File(file, term);

        file = new File(file, pluginArtifactId);
        file = new File(file, pluginVersion);

        file = new File(file, String.format("%s-%s.jar", pluginArtifactId, pluginVersion));

        return file.getAbsolutePath();
    }

    @SuppressWarnings("unchecked")
    private String classPath() throws MavenReportException
    {
        List<Artifact> artifacts = (List<Artifact>) project.getCompileArtifacts();

        return artifactsToArgumentPath(artifacts);
    }

    private String artifactsToArgumentPath(List<Artifact> artifacts) throws MavenReportException
    {
        List<String> paths = newList();

        for (Artifact artifact : artifacts)
        {
            if (artifact.getScope().equals("test")) continue;

            File file = artifact.getFile();

            if (file == null)
                throw new MavenReportException(
                        "Unable to execute Javadoc: compile dependencies are not fully resolved.");

            paths.add(file.getAbsolutePath());
        }

        return toArgumentPath(paths);
    }

    private void executeCommand(Commandline command) throws MavenReportException
    {
        getLog().debug(command.toString());

        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();

        try
        {
            int exitCode = CommandLineUtils.executeCommandLine(command, new DefaultConsumer(), err);

            if (exitCode != 0)
            {
                String message = String.format(
                        "Javadoc exit code: %d - %s\nCommand line was: %s",
                        exitCode,
                        err.getOutput(),
                        command);

                throw new MavenReportException(message);
            }
        }
        catch (CommandLineException ex)
        {
            throw new MavenReportException("Unable to execute javadoc command: " + ex.getMessage(),
                    ex);
        }

        // ----------------------------------------------------------------------
        // Handle Javadoc warnings
        // ----------------------------------------------------------------------

        if (StringUtils.isNotEmpty(err.getOutput()))
        {
            getLog().info("Javadoc Warnings");

            StringTokenizer token = new StringTokenizer(err.getOutput(), "\n");
            while (token.hasMoreTokens())
            {
                String current = token.nextToken().trim();

                getLog().warn(current);
            }
        }
    }

    private String pathToJavadoc() throws IOException, MavenReportException
    {
        String executableName = SystemUtils.IS_OS_WINDOWS ? "javadoc.exe" : "javadoc";

        File executable = initialGuessAtJavadocFile(executableName);

        if (!executable.exists() || !executable.isFile())
            throw new MavenReportException(String.format(
                    "Path %s does not exist or is not a file.",
                    executable));

        return executable.getAbsolutePath();
    }

    private File initialGuessAtJavadocFile(String executableName)
    {
        if (SystemUtils.IS_OS_MAC_OSX)
            return new File(SystemUtils.getJavaHome() + File.separator + "bin", executableName);

        return new File(SystemUtils.getJavaHome() + File.separator + ".." + File.separator + "bin",
                executableName);
    }

    private String toArgumentPath(List<String> paths)
    {
        StringBuilder builder = new StringBuilder();

        String sep = "";

        for (String path : paths)
        {
            builder.append(sep);
            builder.append(path);

            sep = SystemUtils.PATH_SEPARATOR;
        }

        return builder.toString();
    }

    public Map<String, ClassDescription> readXML(String path) throws MavenReportException
    {
        try
        {
            Builder builder = new Builder(false);

            File input = new File(path);

            Document doc = builder.build(input);

            return buildMapFromDocument(doc);
        }
        catch (Exception ex)
        {
            throw new MavenReportException(String.format("Failure reading from %s: %s", path, ex
                    .getMessage()), ex);
        }
    }

    private Map<String, ClassDescription> buildMapFromDocument(Document doc)
    {
        Map<String, ClassDescription> result = newMap();

        Elements elements = doc.getRootElement().getChildElements("class");

        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);

            String description = element.getFirstChildElement("description").getValue();

            String className = element.getAttributeValue("name");
            String superClassName = element.getAttributeValue("super-class");

            ClassDescription cd = new ClassDescription(className, superClassName, description);

            result.put(className, cd);

            readParameters(cd, element);
        }

        return result;
    }

    private void readParameters(ClassDescription cd, Element classElement)
    {
        Elements elements = classElement.getChildElements("parameter");

        for (int i = 0; i < elements.size(); i++)
        {
            Element node = elements.get(i);

            String name = node.getAttributeValue("name");
            String type = node.getAttributeValue("type");

            int dotx = type.lastIndexOf('.');
            if (dotx > 0 && type.substring(0, dotx).equals("java.lang"))
                type = type.substring(dotx + 1);

            String defaultValue = node.getAttributeValue("default");
            boolean required = Boolean.parseBoolean(node.getAttributeValue("required"));
            boolean cache = Boolean.parseBoolean(node.getAttributeValue("cache"));
            String defaultPrefix = node.getAttributeValue("default-prefix");
            String description = node.getValue();

            ParameterDescription pd = new ParameterDescription(name, type, defaultValue,
                    defaultPrefix, required, cache, description);

            cd.getParameters().put(name, pd);
        }
    }
}
