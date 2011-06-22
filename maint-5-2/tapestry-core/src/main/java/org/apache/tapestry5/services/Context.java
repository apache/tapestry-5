// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * An API agnostic version of {@link javax.servlet.ServletContext}, used to bridge the gaps between the Servlet API and
 * the Portlet API.
 */
public interface Context
{
    /**
     * Returns a URL to a resource stored within the context. The path should start with a leading slash.
     *
     * @param path to the resource (with a leading slash)
     * @return the URL for the path, or null if the path does not correspond to a file.
     */
    URL getResource(String path);

    /**
     * Attempts to find the actual file, on the file system, that would be provided by the servlet container for the
     * given path (which must start with a leading slash). This may return null if no such file exists, or if the
     * resource in question is packaged inside a WAR.  If packaged inside a WAR, the contents may be accessed via {@link
     * #getResource(String)}.
     *
     * @param path to  the resource (with a leading slash)
     * @return the underlying File, or null if no such file
     */
    File getRealFile(String path);

    /**
     * Returns an initial parameter value defined by servlet.
     */
    String getInitParameter(String name);

    /**
     * Looks for resources within the web application within the supplied path. The list will be recurively expanded, as
     * necessary. The path must start with a leading slash, and usually ends with a slash as well.
     *
     * @param path to search for (should start with a leading slash)
     * @return the matches, sorted alphabetically
     */
    List<String> getResourcePaths(String path);

    /**
     * Returns an attribute previously stored into the context with the given name.
     *
     * @param name used to retrieve the attribute
     * @return the attribute, or null if not found
     */
    Object getAttribute(String name);

    /**
     * Returns the names of all attributes of the context, sorted alphabetically.
     */
    List<String> getAttributeNames();

    /**
     * Returns the MIME content type of the specified file, or null if no content type is known. MIME types are built-in
     * to servlet containers and may be futher specified via the web application deployment descriptor.
     *
     * @param file name of file
     * @return the presumed MIME content type, or null if not known
     * @since 5.1.0.0
     */
    String getMimeType(String file);
}
