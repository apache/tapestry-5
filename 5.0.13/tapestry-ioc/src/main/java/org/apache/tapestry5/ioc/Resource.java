// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

/**
 * Represents a resource on the server that may be used for server side processing, or may be exposed to the client
 * side. Generally, this represents an abstraction on top of files on the class path and files stored in the web
 * application context.
 * <p/>
 * Resources are often used as map keys; they should be immutable and should implement hashCode() and equals().
 */
public interface Resource
{

    /**
     * Returns true if the resource exists; if a stream to the content of the file may be openned.
     *
     * @return true if the resource exists, false if it does not
     */
    boolean exists();

    /**
     * Opens a stream to the content of the resource, or returns null if the resource does not exist.
     *
     * @return an open, buffered stream to the content, if available
     */
    InputStream openStream() throws IOException;

    /**
     * Returns the URL for the resource, or null if it does not exist.
     */
    URL toURL();

    /**
     * Returns a localized version of the resource. May return null if no such resource exists.
     */
    Resource forLocale(Locale locale);

    /**
     * Returns a Resource based on a relative path, relative to the folder containing the resource. Understands the "."
     * (current folder) and ".." (parent folder) conventions, and treats multiple sequential slashes as a single slash.
     */
    Resource forFile(String relativePath);

    /**
     * Returns a new Resource with the extension changed (or, if the resource does not have an extension, the extension
     * is added). The new Resource may not exist (that is, {@link #toURL()} may return null.
     *
     * @param extension to apply to the resource, such as "html" or "properties"
     * @return the new resource
     */
    Resource withExtension(String extension);

    /**
     * Returns the portion of the path up to the last forward slash; this is the directory or folder portion of the
     * Resource.
     */
    String getFolder();

    /**
     * Returns the file portion of the Resource path, everything that follows the final forward slash.
     */
    String getFile();

    /**
     * Return the path (the combination of folder and file).
     */
    String getPath();
}
