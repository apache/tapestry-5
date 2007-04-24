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

package org.apache.tapestry.internal.services;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;

import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.util.Stack;

public class ComponentClassLocatorImpl implements ComponentClassLocator
{
    private static final String CLASS_SUFFIX = ".class";

    private final ClassLoader _contextClassLoader = Thread.currentThread().getContextClassLoader();

    static class Queued
    {
        final URL _packageURL;

        final String _packagePath;

        public Queued(final URL packageURL, final String packagePath)
        {
            _packageURL = packageURL;
            _packagePath = packagePath;
        }
    }

    /**
     * Synchronization should not be necessary, but perhaps the underlying ClassLoader's are
     * sensitive to threading. This is just a shot in the dark to address our continuous integration
     * instability.
     */
    public synchronized Collection<String> locateComponentClassNames(String packageName)
    {
        String packagePath = packageName.replace('.', '/') + "/";

        try
        {
            Collection<String> result = findClassesWithinPath(packagePath);

            return result;

        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Collection<String> findClassesWithinPath(String packagePath) throws IOException
    {
        Collection<String> result = CollectionFactory.newList();

        Enumeration<URL> urls = _contextClassLoader.getResources(packagePath);

        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();

            scanURL(packagePath, result, url);
        }

        return result;
    }

    private void scanURL(String packagePath, Collection<String> componentClassNames, URL url)
            throws IOException
    {
        URLConnection connection = url.openConnection();

        if (connection instanceof JarURLConnection)
        {
            scanJarConnection(packagePath, componentClassNames, (JarURLConnection) connection);
            return;
        }

        // Otherwise, we're forced to assume that it is a file: URL for files in the user's
        // workspace.

        Stack<Queued> queue = CollectionFactory.newStack();

        queue.push(new Queued(url, packagePath));

        while (!queue.isEmpty())
        {
            Queued queued = queue.pop();

            scan(queued._packagePath, queued._packageURL, componentClassNames, queue);
        }

    }

    private void scan(String packagePath, URL packageURL, Collection<String> componentClassNames,
            Stack<Queued> queue) throws IOException
    {
        InputStream is = null;

        try
        {
            is = new BufferedInputStream(packageURL.openStream());
        }
        catch (FileNotFoundException ex)
        {
            // This can happen for certain application servers (JBoss 4.0.5 for example), that
            // export part of the exploded WAR for deployment, but leave part (WEB-INF/classes)
            // unexploded.

            return;
        }

        Reader reader = new InputStreamReader(is);
        LineNumberReader lineReader = new LineNumberReader(reader);

        String packageName = null;

        try
        {
            while (true)
            {
                String line = lineReader.readLine();

                if (line == null)
                    break;

                if (line.contains("$"))
                    continue;

                if (line.endsWith(CLASS_SUFFIX))
                {
                    if (packageName == null)
                        packageName = packagePath.replace('/', '.');

                    // packagePath ends with '/', packageName ends with '.'

                    String fullClassName = packageName
                            + line.substring(0, line.length() - CLASS_SUFFIX.length());

                    componentClassNames.add(fullClassName);

                    continue;
                }

                // Either a file or a hidden directory (such as .svn)

                if (line.contains("."))
                    continue;

                // The name of a subdirectory.

                URL newURL = new URL(packageURL.toExternalForm() + line + "/");
                String newPackagePath = packagePath + line + "/";

                queue.push(new Queued(newURL, newPackagePath));
            }

            lineReader.close();
            lineReader = null;
        }
        finally
        {
            TapestryUtils.close(lineReader);
        }

    }

    private void scanJarConnection(String packagePath, Collection<String> componentClassNames,
            JarURLConnection connection) throws IOException
    {
        Enumeration<JarEntry> e = connection.getJarFile().entries();

        while (e.hasMoreElements())
        {
            String name = e.nextElement().getName();

            if (!name.startsWith(packagePath))
                continue;

            if (!name.endsWith(CLASS_SUFFIX))
                continue;

            if (name.contains("$"))
                continue;

            // Strip off .class and convert the slashes back to periods.

            String className = name.substring(0, name.length() - CLASS_SUFFIX.length()).replace(
                    "/",
                    ".");

            componentClassNames.add(className);
        }
    }

}
