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
package org.apache.tapestry5.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that encapsulates information about a component library, going beyond what a library mapping
 * provides.
 * 
 * @see LibraryMapping
 * @see SourceUrlResolver
 * @since 5.4
 */
public final class ComponentLibraryInfo implements Serializable 
{
    private static final long serialVersionUID = 1L;
    
    private LibraryMapping libraryMapping;
    
    private SourceUrlResolver sourceUrlResolver;
    
    private String name, description, homepageUrl, documentationUrl, sourceBrowseUrl, issueTrackerUrl, sourceRootUrl, 
                   javadocUrl, groupId, artifactId, version, tapestryVersion;
    
    private List<String> tags = new ArrayList<String>();
    
    /**
     * Returns the actual name of the component library (not the identifier). 
     * For example, "Tapestry 5 Core Library".
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns a description of the component library. 
     * For example, "The set of components, pages and mixins provided by Tapestry out-of-the-box.".
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * Returns the URL of the homepage of the component library.
     * For example, "http://tapestry.apache.org".
     */
    public String getHomepageUrl()
    {
        return homepageUrl;
    }

    /**
     * Returns the URL of the component library's documentation.
     * For example, "http://tapestry.apache.org/documentation.html".
     */
    public String getDocumentationUrl()
    {
        return documentationUrl;
    }

    /**
     * Returns the URL where the component library's source can be browsed.
     * For example, "https://git-wip-us.apache.org/repos/asf?p=tapestry-5.git;a=summary".
     */
    public String getSourceBrowseUrl()
    {
        return sourceBrowseUrl;
    }

    /**
     * Returns the URL where the root folder of component library's source can be found.
     * For example, "https://git-wip-us.apache.org/repos/asf?p=tapestry-5.git;a=tree;f=tapestry-core/src/main/java/".
     */
    public String getSourceRootUrl()
    {
        return sourceRootUrl;
    }

    /**
     * Returns the URL of the component's library issue tracker.
     * For example, "https://issues.apache.org/jira/browse/TAP5".
     */
    public String getIssueTrackerUrl()
    {
        return issueTrackerUrl;
    }

    /**
     * Returns the URL of the component library's JavaDoc URL.
     * For example, "http://tapestry.apache.org/current/apidocs/"
     */
    public String getJavadocUrl()
    {
        return javadocUrl;
    }

    /**
     * Returns the component library's group id for dependency management tools like Maven and Gradle.
     * For example, "org.apache.tapestry".
     * @see #artifactId
     * @see #version
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Returns the component library's group id for dependency management tools like Maven and Gradle.
     * For example, "tapestry-core".
     * @see #groupId
     * @see #version
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Returns the component library version. For example, "5.4.0".
     * @see #artifactId
     * @see #groupId
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the Tapestry version used by this component library. For example, "5.4.0".
     */
    public String getTapestryVersion()
    {
        return tapestryVersion;
    }

    /**
     * Returns the tags associated which describe this component library.
     * Use just lowercase letters, numbers and dashes.
     */
    public List<String> getTags()
    {
        return tags;
    }

    /**
     * Returns an URL decribing the dependency management information for this component library.
     */
    public String getDependencyManagementInfoUrl()
    {
        String url = null;
        if (isDependencyManagementInfoPresent())
        {
            url = String.format(
                    "http://search.maven.org/#artifactdetails|%s|%s|version=%s|jar",
                    getGroupId(), getArtifactId(), getVersion());
        }
        return url;
    }

    public void setName(String name)
    {
        if (this.name != null) throwExceptionIfAlreadySet("name", name);
        this.name = name;
    }
    
    public void setDescription(String description)
    {
        if (this.description != null) throwExceptionIfAlreadySet("description", description);
        this.description = description;
    }

    public void setHomepageUrl(String homepageUrl)
    {
        if (this.homepageUrl != null) throwExceptionIfAlreadySet("homepageUrl", homepageUrl);
        this.homepageUrl = homepageUrl;
    }

    public void setDocumentationUrl(String documentationUrl)
    {
        if (this.documentationUrl != null) throwExceptionIfAlreadySet("documentationUrl", documentationUrl);
        this.documentationUrl = documentationUrl;
    }

    public void setSourceBrowseUrl(String sourceBrowseUrl)
    {
        if (this.sourceBrowseUrl != null) throwExceptionIfAlreadySet("sourceBrowseUrl", sourceBrowseUrl);
        this.sourceBrowseUrl = sourceBrowseUrl;
    }

    public void setSourceRootUrl(String sourceRootUrl)
    {
        if (this.sourceRootUrl != null) throwExceptionIfAlreadySet("sourceRootUrl", sourceRootUrl);
        this.sourceRootUrl = sourceRootUrl;
    }

    public void setJavadocUrl(String javadocUrl)
    {
        if (this.javadocUrl != null) throwExceptionIfAlreadySet("javadocUrl", javadocUrl);
        this.javadocUrl = javadocUrl;
    }

    public void setVersion(String version)
    {
        if (this.version != null) throwExceptionIfAlreadySet("version", version);
        this.version = version;
    }
    
    public void setTapestryVersion(String tapestryVersion)
    {
        if (this.tapestryVersion != null) throwExceptionIfAlreadySet("tapestryVersion", version);
        this.tapestryVersion = tapestryVersion;
    }
    
    public void setGroupId(String groupId)
    {
        if (this.groupId != null) throwExceptionIfAlreadySet("groupId", artifactId);
        this.groupId = groupId;
    }
    
    public void setArtifactId(String artifactId)
    {
        if (this.artifactId != null) throwExceptionIfAlreadySet("artifactId", artifactId);
        this.artifactId = artifactId;
    }
    
    public void setIssueTrackerUrl(String issueTrackingUrl)
    {
        if (this.issueTrackerUrl != null) throwExceptionIfAlreadySet("issueTrackingUrl", issueTrackingUrl);
        this.issueTrackerUrl = issueTrackingUrl;
    }

    public void setTags(List<String> tags)
    {
        if (this.tags != null) throwExceptionIfAlreadySet("tags", tags);
        this.tags = tags;
    }

    public void setLibraryMapping(LibraryMapping libraryMapping)
    {
        if (this.libraryMapping != null) throwExceptionIfAlreadySet("libraryMapping", libraryMapping);
        this.libraryMapping = libraryMapping;
    }
    
    public void setSourceUrlResolver(SourceUrlResolver sourceUrlResolver)
    {
        if (this.sourceUrlResolver != null) throwExceptionIfAlreadySet("sourceUrlResolver", sourceUrlResolver);
        this.sourceUrlResolver = sourceUrlResolver;
        if (sourceUrlResolver != null)
        {
            sourceUrlResolver.setRootUrl(getSourceRootUrl());
        }
    }

    /**
     * Tells whether full dependency management info (group id, artifact id and version) are present.
     */
    public boolean isDependencyManagementInfoPresent()
    {
        return groupId != null && artifactId != null && version != null;
    }
    
    /**
     * Given a logical name, tells whether a given component, page or mixin is part of this
     * component library.
     */
    public boolean isPart(String logicalName)
    {
        return logicalName.startsWith(libraryMapping.libraryName + "/") || 
                (libraryMapping.libraryName.equals("") && logicalName.indexOf("/") < 0);
    }
    
    /**
     * Returns the JavaDoc URL for a given class or <code>null</code> if the root JavaDoc URL was 
     * not provided. 
     * @param className the fully qualified class name.
     */
    public String getJavadocUrl(String className)
    {
        String url = null;
        String baseUrl = getJavadocUrl();
        if (baseUrl != null)
        {
            if (!baseUrl.endsWith("/"))
            {
                baseUrl = baseUrl + "/";
            }
            url = baseUrl + className.replace('.', '/') + ".html";
        }
        return url;
    }

    /**
     * Returns the URL where the source of this class can be found or <code>null</code> if 
     * not available. This implementation delegates to {@link SourceUrlResolver} if set.
     * @param className the fully qualified class name.
     */
    public String getSourceUrl(String className)
    {
        String url = null;
        if (sourceRootUrl != null)
        {
            if (sourceUrlResolver == null)
            {
                sourceUrlResolver = new DefaultSourceUrlResolver();
                sourceUrlResolver.setRootUrl(sourceRootUrl);
            }
            url = sourceUrlResolver.resolve(className);
        }
        return url;
    }

    private void throwExceptionIfAlreadySet(String propertyName, Object propertyValue)
    {
        if (propertyValue != null)
        {
            throw new RuntimeException(String.format("%s already has a value of \"%s\"", propertyName, propertyValue));
        }
    }
    
    /**
     * Interface that provides the source URL for a given {@link ComponentLibraryInfo}.
     */
    public static interface SourceUrlResolver
    {
        /**
         * Returns the source URL for a given class.
         * @param className the fully qualified class name.
         */
        String resolve(String className);
        
        /**
         * Sets the source root URL. This method will be invoked by {@link ComponentLibraryInfo#setSourceBrowseUrl(String)}.
         */
        void setRootUrl(String url);
        
    }
    
    /**
     * Default {@link SourceUrlResolver} implementation.
     */
    public static class DefaultSourceUrlResolver implements SourceUrlResolver
    {

        private String sourceRootUrl;

        @Override
        public String resolve(String className)
        {
            return sourceRootUrl + className.replace('.', '/') + ".java";
        }

        @Override
        public void setRootUrl(String url)
        {
            this.sourceRootUrl = url;
            if (sourceRootUrl.startsWith("scm:"))
            {
                this.sourceRootUrl = this.sourceRootUrl.replaceFirst("[^:]+:[^:]+:", "");
            }
        }
        
    }
    
    public String toString() {
        return String.format("ComponentLibraryInfo[%s]", libraryMapping);
    }
    
}