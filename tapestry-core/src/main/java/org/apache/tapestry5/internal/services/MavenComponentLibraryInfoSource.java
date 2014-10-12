// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tapestry5.ioc.services.ClasspathMatcher;
import org.apache.tapestry5.ioc.services.ClasspathScanner;
import org.apache.tapestry5.services.ComponentLibraryInfo;
import org.apache.tapestry5.services.ComponentLibraryInfoSource;
import org.apache.tapestry5.services.LibraryMapping;
import org.slf4j.Logger;
import org.w3c.dom.Document;

/**
 * {@link ComponentLibraryInfoSource} implementation based on the pom.xml and pom.properties files 
 * Maven places in the /META-INF/maven/[groupId]/[artifactId] folder.
 */
public class MavenComponentLibraryInfoSource implements ComponentLibraryInfoSource
{
    
    final private Logger logger;
    
    final private Set<String> pomPaths;
    
    final private Map<String, ComponentLibraryInfo> cache = new HashMap<String, ComponentLibraryInfo>();
    
    final private Map<String, String> pomPathToRootUrl;
    
    final private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public MavenComponentLibraryInfoSource(Logger logger, ClasspathScanner classpathScanner)
    {
        super();
        this.logger = logger;
        this.pomPaths = Collections.unmodifiableSet(findPomPaths(classpathScanner));
        pomPathToRootUrl = new WeakHashMap<String, String>(pomPaths.size());
    }

    @Override
    public ComponentLibraryInfo find(LibraryMapping libraryMapping)
    {
        ComponentLibraryInfo info = null;
        if (cache.containsKey(libraryMapping.libraryName))
        {
            info = cache.get(libraryMapping.libraryName);
        }
        else
        {
            final String pomPath = getPomPath(libraryMapping);
            if (pomPath != null)
            {
                InputStream inputStream = getClass().getResourceAsStream("/" + pomPath);
                info = parse(inputStream);
                info.setLibraryMapping(libraryMapping);
                cache.put(libraryMapping.libraryName, info);
            }
            else
            {
                cache.put(libraryMapping.libraryName, null);
            }
        }
        return info;
    }

    private ComponentLibraryInfo parse(InputStream inputStream)
    {
        ComponentLibraryInfo info = null;
        if (inputStream != null)
        {
            
            Document document;
            
            try
            {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                document = documentBuilder.parse(inputStream);
            }
            catch (Exception e)
            {
                logger.warn("Exception while parsing pom.xml", e);
                return null;
            }
            
            info = new ComponentLibraryInfo();
            info.setGroupId(extractText(document, "(/project/groupId | /project/parent/groupId)[1]"));
            info.setArtifactId(extractText(document, "/project/artifactId"));
            info.setVersion(extractText(document, "/project/version"));
            info.setName(extractText(document, "/project/name"));
            info.setDescription(extractText(document, "/project/description"));
            info.setDocumentationUrl(extractText(document, "/project/properties/documentationUrl"));
            info.setHomepageUrl(extractText(document, "/project/properties/homepageUrl"));
            info.setIssueTrackerUrl(extractText(document, "/project/issueManagement/url"));
            info.setJavadocUrl(extractText(document, "/project/properties/javadocUrl"));
            info.setSourceBrowseUrl(extractText(document, "/project/scm/url"));
            info.setSourceRootUrl(extractText(document, "/project/properties/sourceRootUrl"));
            info.setTapestryVersion(extractText(document, "(/project/dependencies/dependency[./groupId='org.apache.tapestry'][./artifactId='tapestry-core']/version | /project/properties/tapestryVersion)[1]"));
            String tags = extractText(document, "/project/properties/tags");
            if (tags != null && tags.length() > 0)
            {
                info.setTags(Arrays.asList(tags.split(",")));
            }
            
        }
        
        return info;
        
    }

    private String extractText(Document document, String xpathExpression)
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String text;
        try
        {
            XPathExpression expression = xpath.compile(xpathExpression);
            text = (String) expression.evaluate(document, XPathConstants.STRING);
        }
        catch (XPathExpressionException e)
        {
            throw new RuntimeException(e);
        }
        if ("".equals(text)) 
        {
            text = null;
        }
        return text;
    }

    private String getPomPath(LibraryMapping libraryMapping)
    {
        final String rootPackageConverted = libraryMapping.getRootPackage().replace('.', '/');
        final URL rootPackageUrl = getClass().getClassLoader().getResource(rootPackageConverted);
        String path = rootPackageUrl.toString();
        String url = null;
        if (path.contains("!/"))
        {
            path = path.substring(0, path.indexOf("!/"));
        }
        for (String pomPath : pomPaths)
        {
            if (path.equals(getPomPathUrl(pomPath))) {
                url = pomPath;
                break;
            }
        }
        return url;
    }
    
    private String getPomPathUrl(String pomPath)
    {
        String url = pomPathToRootUrl.get(pomPath);
        if (url == null)
        {
            for (String path : pomPaths)
            {
                final URL resource = getClass().getResource("/" + path);
                String resourcePath = null;
                if (resource != null && resource.toString().contains("!/")) 
                {
                    resourcePath = resource.toString();
                    resourcePath = resourcePath.substring(0, resourcePath.indexOf("!/"));
                }
                pomPathToRootUrl.put(path, resourcePath);
                url = resourcePath;
            }
        }
        return url;
    }

    private static Set<String> findPomPaths(ClasspathScanner classpathScanner)
    {
        final ClasspathMatcher classpathMatcher = new ClasspathMatcher()
        {
            @Override
            public boolean matches(String packagePath, String fileName)
            {
                return fileName.equals("pom.xml");
            }
        };
        try
        {
            return classpathScanner.scan("META-INF/maven/", classpathMatcher);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Exception while finding pom.xml files in the classpath", e);
        }
    }

}
