// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import nu.xom.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The component report generates documentation about components and parameters within the current project.
 *
 * @goal component-report
 * @requiresDependencyResolution compile
 * @execute phase="generate-sources"
 */
@SuppressWarnings({ "unchecked" })
public class ComponentReport extends AbstractMavenReport
{
    /**
     * Subdirectory containing the component reference pages and index.
     */
    private static final String REFERENCE_DIR = "ref";

    private final static String[] PARAMETER_HEADERS = { "Name", "Type", "Flags", "Default", "Default Prefix",
            "Since", "Description" };

    private static final Pattern TAPESTRY5_PATTERN = Pattern.compile("(org\\.apache\\.tapestry5[#_\\w\\.]*)");

    private static final char QUOTE = '"';

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
    private Renderer siteRenderer;

    /**
     * Location of the generated site.
     *
     * @parameter default-value="${project.reporting.outputDirectory}"
     * @required
     */
    private String outputDirectory;

    /**
     * @parameter expression="${project.build.directory}/generated-site/xdoc"
     * @required
     */
    private File generatedDocsDirectory;

    /**
     * @parameter expression="${project.build.directory}/generated-site/resources"
     * @required
     */
    private File generatedResourcesDirectory;

    /**
     * Working directory for temporary files.
     *
     * @parameter default-value="target"
     * @required
     */
    private String workDirectory;

    /**
     * Relative path from the generated report to the API documentation (Javadoc). Defaults to "apidocs" but will often
     * be changed to "../apidocs" when documentation is created at the project level.
     *
     * @parameter default-value="apidocs"
     * @required
     */
    private String apidocs;

    /**
     * Where to find tapestry javadocs. This is used for generating documentation links for each parameter type.
     * <p/>
     * By default, this is set to "http://tapestry.apache.org/tapestry5/apidocs". A relative path can also be supplied
     * (a sensible value would be 'apidocs') and is resolved from the documentation root.
     *
     * @parameter default-value="http://tapestry.apache.org/tapestry5/apidocs"
     */
    private String tapestryJavadoc;

    @Override
    protected String getOutputDirectory()
    {
        return outputDirectory;
    }

    @Override
    protected MavenProject getProject()
    {
        return project;
    }

    @Override
    protected Renderer getSiteRenderer()
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
        return REFERENCE_DIR + "/index";
    }


    private final static Set<String> SUPPORTED_SUBPACKAGES = CollectionFactory.newSet("base", "components", "mixins",
                                                                                      "pages");

    /**
     * Generates the report; this consist of the index page
     *
     * @param locale
     * @throws MavenReportException
     */
    @Override
    protected void executeReport(Locale locale) throws MavenReportException
    {
        Map<String, ClassDescription> descriptions = runJavadoc();

        getLog().info("Generating reference pages ...");

        try
        {
            File refDir = new File(generatedDocsDirectory, REFERENCE_DIR);

            refDir.mkdirs();


            List<File> docSearchPath = createDocSearchPath();

            Sink sink = getSink();

            sink.head();
            sink.title();
            sink.text("Component Reference");
            sink.title_();
            sink.head_();

            sink.section1();
            sink.sectionTitle1();
            sink.text("Component Reference");
            sink.sectionTitle1_();
            sink.list();

            String currentSubpackage = null;

            for (String className : InternalUtils.sortedKeys(descriptions))
            {
                String subpackage = extractSubpackage(className);

                if (!SUPPORTED_SUBPACKAGES.contains(subpackage)) continue;

                if (!subpackage.equals(currentSubpackage))
                {
                    if (currentSubpackage != null)
                    {
                        sink.list_();
                        sink.section2_();
                    }

                    sink.section2();
                    sink.sectionTitle2();
                    sink.text(StringUtils.capitalize(subpackage));
                    sink.sectionTitle2_();


                    sink.list();

                    currentSubpackage = subpackage;
                }


                sink.listItem();

                sink.link(toHtml(toPath(className)));

                sink.text(className);
                sink.link_();

                writeClassDescription(descriptions, refDir, docSearchPath, className);


                sink.listItem_();
            }

            if (currentSubpackage != null)
            {
                sink.list_();
                sink.section2_();
            }
        }
        catch (Exception ex)
        {
            throw new MavenReportException(ex.getMessage(), ex);
        }
    }

    private String toPath(String className)
    {
        return className.replace('.', '/');
    }

    private String toHtml(String filename)
    {
        int pos = filename.lastIndexOf("#");
        if (pos < 0)
            return filename + ".html";
        else
        {
            return filename.substring(0, pos) + ".html" + filename.substring(pos);
        }
    }

    private String extractSimpleName(String className)
    {
        int dotx = className.lastIndexOf(".");

        return className.substring(dotx + 1);
    }

    private String extractSubpackage(String className)
    {
        int dotx = className.indexOf(".", rootPackage.length() + 1);

        // For classes directly in the root package.

        if (dotx < 1) return "";

        return className.substring(rootPackage.length() + 1, dotx);
    }

    protected List<File> createDocSearchPath()
    {
        List<File> result = CollectionFactory.newList();

        for (String sourceRoot : (List<String>) project.getCompileSourceRoots())
        {
            result.add(new File(sourceRoot));
        }


        for (Resource r : (List<Resource>) project.getResources())
        {
            String dir = r.getDirectory();

            result.add(new File(dir));
        }

        return result;
    }

    private void writeClassDescription(Map<String, ClassDescription> descriptions, File refDir,
                                       List<File> docSearchPath, String className) throws Exception
    {

        int dotx = className.lastIndexOf('.');
        String packageName = className.substring(0, dotx);
        File outputDir = new File(refDir, toPath(packageName));
        outputDir.mkdirs();

        File outputFile = new File(refDir, toPath(className) + ".xml");


        Element root = new Element("document");

        ClassDescription cd = descriptions.get(className);

        Map<String, ParameterDescription> parameters = CollectionFactory.newMap(cd.getParameters());
        List<String> parents = CollectionFactory.newList();

        String current = cd.getSuperClassName();

        Map<String, String> events = CollectionFactory.newCaseInsensitiveMap(cd.getEvents());

        while (true)
        {
            ClassDescription superDescription = descriptions.get(current);

            if (superDescription == null) break;

            parents.add(current);
            parameters.putAll(superDescription.getParameters());
            events.putAll(superDescription.getEvents());

            current = superDescription.getSuperClassName();
        }

        // Now, mix in the published parameters.

        for (Map.Entry<String, String> entry : cd.getPublishedParameters().entrySet())
        {
            String name = entry.getKey();
            String embeddedClassName = entry.getValue();

            // TODO: Don't handle the case where the @Component.type attribute was used!

            ParameterDescription pd = locatePublishedParameterDescription(name, embeddedClassName, descriptions);

            parameters.put(name, pd);
        }


        Collections.reverse(parents);

        // XOM is pretty verbose; it really needs a builder/fluent interface.

        Element properties = addChild(root, "properties");
        addChild(properties, "title", String.format("Component Reference: %s", className));

        Element body = new Element("body");
        root.appendChild(body);

        Element section = addSection(body, className);


        StringBuilder javadocURL = new StringBuilder(200);

        int depth = packageName.split("\\.").length;


        for (int i = 0; i < depth; i++)
        {
            javadocURL.append("../");
        }

        String pathToRefRoot = javadocURL.toString();

        javadocURL.append("../");

        String javadocHref = tapestryJavadoc.contains("://") ?
                             tapestryJavadoc : javadocURL.toString() + tapestryJavadoc;

        javadocURL.append(apidocs).append("/").append(toHtml(toPath(className)));

        addChildWithJavadocs(section, "p", cd.getDescription(), javadocHref);

        addLink(addChild(section, "p"), javadocURL.toString(), "[JavaDoc]");

        if (!parents.isEmpty())
        {
            section = addSection(body, "Component Inheritance");
            Element container = section;

            for (String name : parents)
            {

                Element ul = addChild(container, "ul");

                Element li = addChild(ul, "li");

                addLink(li, toHtml(name), name);

                container = li;
            }

            addChild(addChild(container, "ul"), "li", className);
        }

        if (!"".equals(cd.getSince()))
        {
            section = addSection(body, "Available since");

            addChild(section, "p", cd.getSince());
        }


        if (!parameters.isEmpty())
        {
            section = addSection(body, "Component Parameters");

            Element table = new Element("table");

            section.appendChild(table);

            Element headerRow = new Element("tr");
            table.appendChild(headerRow);

            for (String header : PARAMETER_HEADERS)
                addChild(headerRow, "th", header);

            List<String> flags = CollectionFactory.newList();

            for (String name : InternalUtils.sortedKeys(parameters))
            {
                ParameterDescription pd = parameters.get(name);

                flags.clear();

                if (pd.getRequired()) flags.add("Required");

                if (!pd.getCache()) flags.add("NOT Cached");

                if (!pd.getAllowNull()) flags.add("NOT Allow Null");

                Element row = new Element("tr");
                table.appendChild(row);

                addChild(row, "td", pd.getName());
                addChildWithJavadocs(row, "td", pd.getType(), javadocHref);
                addChild(row, "td", InternalUtils.join(flags));
                addChild(row, "td", pd.getDefaultValue());
                addChild(row, "td", pd.getDefaultPrefix());
                addChild(row, "td", pd.getSince());
                addChildWithJavadocs(row, "td", pd.getDescription(), javadocHref);
            }
        }

        if (cd.isSupportsInformalParameters())
            addChild(section, "p", "Informal parameters: supported");


        if (!events.isEmpty())
        {
            section = addSection(body, "Component Events");

            Element ul = addChild(section, "ul");

            for (String name : InternalUtils.sortedKeys(events))
            {
                String value = events.get(name);

                String text = value.length() > 0 ? name + ": " + value : name;

                addChild(ul, "li", text);
            }
        }

        addExternalDocumentation(body, docSearchPath, className);

        addChild(body, "hr");

        addLink(addChild(body, "p"), pathToRefRoot + "index.html", "Back to index");

        Document document = new Document(root);


        getLog().info(String.format("Writing %s", outputFile));

        FileOutputStream fos = new FileOutputStream(outputFile);

        BufferedOutputStream bos = new BufferedOutputStream(fos);

        PrintWriter writer = new PrintWriter(bos);

        writer.print(document.toXML());

        writer.close();
    }

    private ParameterDescription locatePublishedParameterDescription(String name, String componentClassName,
                                                                     Map<String, ClassDescription> descriptions)
    {
        String current = componentClassName;

        while (current != null)
        {
            ClassDescription cd = descriptions.get(componentClassName);

            if (cd == null) break;

            String indirectClassName = cd.getPublishedParameters().get(name);

            if (indirectClassName != null)
            {
                current = indirectClassName;
                continue;
            }

            ParameterDescription pd = cd.getParameters().get(name);

            if (pd != null) return pd;

            current = cd.getSuperClassName();
        }

        throw new IllegalArgumentException(
                String.format("Published parameter '%s' (from embedded component class %s) does not exist.",
                              name, componentClassName));
    }

    private void addExternalDocumentation(Element body, List<File> docSearchPath, String className)
            throws ParsingException, IOException
    {
        String classNamePath = toPath(className);

        String pathExtension = classNamePath + ".xdoc";

        for (File path : docSearchPath)
        {
            File file = new File(path, pathExtension);

            getLog().debug(String.format("Checking for %s", file));

            if (!file.exists()) continue;

            getLog().info(String.format("Reading extra documentation from %s", file));

            Builder builder = new Builder();

            Document doc = builder.build(file);

            // Transfer the nodes inside document/body into our body

            Element incomingBody = doc.getRootElement().getFirstChildElement("body");

            for (int i = 0; i < incomingBody.getChildCount(); i++)
            {
                Node incoming = incomingBody.getChild(i).copy();

                body.appendChild(incoming);
            }

            Nodes nodes = doc.query("//img/@src");

            int lastslashx = classNamePath.lastIndexOf('/');
            String packagePath = classNamePath.substring(0, lastslashx);

            File generatedRefRoot = new File(generatedResourcesDirectory, REFERENCE_DIR);
            File generatedPackageRoot = new File(generatedRefRoot, packagePath);

            for (int i = 0; i < nodes.size(); i++)
            {
                Node src = nodes.get(i);

                String srcPath = src.getValue();

                File imgFile = new File(path, packagePath + "/" + srcPath);
                File imgTargetFile = new File(generatedPackageRoot, srcPath);

                copy(imgFile, imgTargetFile);
            }


            return;
        }
    }

    private void copy(File sourceFile, File targetFile) throws IOException
    {
        getLog().info(String.format("Copying image file %s to %s", sourceFile, targetFile));

        targetFile.getParentFile().mkdirs();

        byte[] buffer = new byte[20000];

        InputStream in = new BufferedInputStream(new FileInputStream(sourceFile));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));

        while (true)
        {
            int length = in.read(buffer);

            if (length < 0) break;

            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }


    protected Map<String, ClassDescription> runJavadoc() throws MavenReportException
    {
        getLog().info("Running JavaDoc to collect component parameter data ...");

        Commandline command = new Commandline();

        try
        {
            command.setExecutable(pathToJavadoc());
        }
        catch (IOException ex)
        {
            throw new MavenReportException("Unable to locate javadoc command: " + ex.getMessage(), ex);
        }

        String parametersPath = workDirectory + File.separator + "component-parameters.xml";

        String[] arguments = { "-private", "-o", parametersPath,

                "-subpackages", rootPackage,

                "-doclet", ParametersDoclet.class.getName(),

                "-docletpath", docletPath(),

                "-sourcepath", sourcePath(),

                "-classpath", classPath() };

        String argumentsFile = writeArgumentsFile(arguments);

        command.addArguments(new String[] { "@" + argumentsFile });

        executeCommand(command);

        return readXML(parametersPath);
    }

    private String writeArgumentsFile(String[] arguments) throws MavenReportException
    {
        String fileName = workDirectory + "/component-report-javadoc-arguments.txt";

        try
        {
            PrintWriter pw = new PrintWriter(fileName);

            for (String arg : arguments)
            {
                pw.println(arg);
            }

            pw.close();
        }
        catch (IOException ex)
        {
            throw new MavenReportException(ex.getMessage());
        }

        return fileName;
    }

    @SuppressWarnings("unchecked")
    private String sourcePath()
    {
        List<String> roots = project.getCompileSourceRoots();

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

        return toArgumentPath(Arrays.asList(file.getAbsolutePath()));
    }

    @SuppressWarnings("unchecked")
    private String classPath() throws MavenReportException
    {
        List<Artifact> artifacts = project.getCompileArtifacts();

        return artifactsToArgumentPath(artifacts);
    }

    private String artifactsToArgumentPath(List<Artifact> artifacts) throws MavenReportException
    {
        List<String> paths = CollectionFactory.newList();

        for (Artifact artifact : artifacts)
        {
            if (artifact.getScope().equals("test")) continue;

            File file = artifact.getFile();

            if (file == null) throw new MavenReportException(
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
                String message = String.format("Javadoc exit code: %d - %s\nCommand line was: %s", exitCode,
                                               err.getOutput(), command);

                throw new MavenReportException(message);
            }
        }
        catch (CommandLineException ex)
        {
            throw new MavenReportException("Unable to execute javadoc command: " + ex.getMessage(), ex);
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
            throw new MavenReportException(String.format("Path %s does not exist or is not a file.", executable));

        return executable.getAbsolutePath();
    }

    private File initialGuessAtJavadocFile(String executableName)
    {
        if (SystemUtils.IS_OS_MAC_OSX)
            return new File(SystemUtils.getJavaHome() + File.separator + "bin", executableName);

        return new File(SystemUtils.getJavaHome() + File.separator + ".." + File.separator + "bin", executableName);
    }

    private String toArgumentPath(List<String> paths)
    {
        StringBuilder builder = new StringBuilder(5000).append(QUOTE);

        String sep = "";

        for (String path : paths)
        {
            builder.append(sep);
            builder.append(path);

            sep = SystemUtils.PATH_SEPARATOR;
        }

        return builder.append(QUOTE).toString();
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
        Map<String, ClassDescription> result = CollectionFactory.newMap();

        Elements elements = doc.getRootElement().getChildElements("class");

        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);

            String description = element.getFirstChildElement("description").getValue();

            String className = element.getAttributeValue("name");
            String superClassName = element.getAttributeValue("super-class");
            String supportsInformalParameters = element.getAttributeValue("supports-informal-parameters");
            String since = element.getAttributeValue("since");

            ClassDescription cd = new ClassDescription(className, superClassName, description,
                                                       Boolean.valueOf(supportsInformalParameters), since);

            result.put(className, cd);

            readParameters(cd, element);
            readPublishedParameters(cd, element);
            readEvents(cd, element);
        }

        return result;
    }

    private void readEvents(ClassDescription cd, Element classElement)
    {
        Elements elements = classElement.getChildElements("event");

        for (int i = 0; i < elements.size(); i++)
        {
            Element node = elements.get(i);

            String name = node.getAttributeValue("name");
            String description = node.getValue();

            cd.getEvents().put(name, description);
        }
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
            if (dotx > 0 && type.substring(0, dotx).equals("java.lang")) type = type.substring(dotx + 1);

            String defaultValue = node.getAttributeValue("default");
            boolean required = Boolean.parseBoolean(node.getAttributeValue("required"));
            boolean cache = Boolean.parseBoolean(node.getAttributeValue("cache"));
            boolean allowNull = Boolean.parseBoolean(node.getAttributeValue("allowNull"));
            String defaultPrefix = node.getAttributeValue("default-prefix");
            String description = node.getValue();
            String since = node.getAttributeValue("since");

            ParameterDescription pd = new ParameterDescription(name, type, defaultValue, defaultPrefix, required,
                                                               allowNull, cache, description, since);

            cd.getParameters().put(name, pd);
        }
    }

    private void readPublishedParameters(ClassDescription cd, Element classElement)
    {
        Elements elements = classElement.getChildElements("published-parameter");

        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);

            Attribute name = element.getAttribute("name");
            Attribute embeddedTypeName = element.getAttribute("component-class");

            cd.getPublishedParameters().put(name.getValue(), embeddedTypeName.getValue());
        }

    }

    private Element addSection(Element container, String name)
    {
        Element section = new Element("section");
        container.appendChild(section);

        section.addAttribute(new Attribute("name", name));

        return section;
    }

    private Element addLink(Element container, String URL, String text)
    {
        Element link = addChild(container, "a", text);

        link.addAttribute(new Attribute("href", URL));

        return link;
    }

    private Element addChild(Element container, String elementName)
    {
        Element child = new Element(elementName);
        container.appendChild(child);

        return child;
    }

    private Element addChild(Element container, String elementName, String text)
    {
        Element child = new Element(elementName);
        container.appendChild(child);

        child.appendChild(text);

        return child;
    }

    private Element addChildWithJavadocs(Element container, String elementName, String text, String javadocHref)
    {
        final String[] parts = splitWithGroup(TAPESTRY5_PATTERN, text);
        if (parts.length <= 1)
        {
            return addChild(container, elementName, text);
        }

        final Element element = addChild(container, elementName);

        for (int i = 0; i < parts.length; i++)
        {
            String part = parts[i];
            element.appendChild(part);
            i++;
            if (i < parts.length)
            {
                part = parts[i];
                if (part.endsWith("."))
                {
                    part = part.substring(0, part.length() - 1);
                    addLink(element, javadocHref + "/" + toHtml(toPath(part)), extractSimpleName(part));
                    element.appendChild(".");
                }
                else
                {
                    addLink(element, javadocHref + "/" + toHtml(toPath(part)), extractSimpleName(part));
                }
            }
        }
        return element;
    }

    /**
     * Splits a {@link CharSequence} using the given pattern while including after each part the matched group.<p/>
     * Mostly copied from {@link Pattern#split(CharSequence)}.
     */
    private String[] splitWithGroup(Pattern pattern, CharSequence input)
    {
        int index = 0;
        List<String> matchList = CollectionFactory.newList();
        Matcher m = pattern.matcher(input);

        // Add segments before each match found
        while (m.find())
        {
            String match = input.subSequence(index, m.start()).toString();
            String group = input.subSequence(m.start(), m.end()).toString();
            matchList.add(match);
            matchList.add(group);
            index = m.end();
        }

        // If no match was found, return this
        if (index == 0)
            return new String[] { input.toString() };

        // Add remaining segment
        matchList.add(input.subSequence(index, input.length()).toString());

        // Construct result
        int resultSize = matchList.size();
        while (resultSize > 0 && matchList.get(resultSize - 1).equals(""))
            resultSize--;
        String[] result = new String[resultSize];
        return matchList.subList(0, resultSize).toArray(result);
    }
}
