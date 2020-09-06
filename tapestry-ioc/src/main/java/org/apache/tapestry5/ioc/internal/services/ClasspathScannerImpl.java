// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.Stack;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClasspathMatcher;
import org.apache.tapestry5.ioc.services.ClasspathScanner;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class ClasspathScannerImpl implements ClasspathScanner
{
    private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    private final ClasspathURLConverter converter;

    private static final Pattern FOLDER_NAME_PATTERN = Pattern.compile("^\\p{javaJavaIdentifierStart}[\\p{javaJavaIdentifierPart}]*$", Pattern.CASE_INSENSITIVE);


    public ClasspathScannerImpl(ClasspathURLConverter converter)
    {
        this.converter = converter;
    }

    /**
     * Scans the indicated package path for matches.
     *
     * @param packagePath
     *         a package path (like a package name, but using '/' instead of '.', and ending with '/')
     * @param matcher
     *         passed a resource path from the package (or a sub-package), returns true if the provided
     *         path should be included in the returned collection
     * @return collection of matching paths, in no specified order
     * @throws java.io.IOException
     */
    @Override
    public Set<String> scan(String packagePath, ClasspathMatcher matcher) throws IOException
    {
        assert packagePath != null && packagePath.endsWith("/");
        assert matcher != null;

        return new Job(matcher, contextClassLoader, converter).findMatches(packagePath);
    }

    /**
     * Check whether container supports opening a stream on a dir/package to get a list of its contents.
     */
    private static boolean supportsDirStream(URL packageURL)
    {
        InputStream is = null;

        try
        {
            is = packageURL.openStream();

            return true;
        } catch (FileNotFoundException ex)
        {
            return false;
        } catch (IOException ex)
        {
            return false;
        } finally
        {
            InternalUtils.close(is);
        }
    }

    /**
     * For URLs to JARs that do not use JarURLConnection - allowed by the servlet spec - attempt to produce a JarFile
     * object all the same. Known servlet engines that function like this include Weblogic and OC4J. This is not a full
     * solution, since an unpacked WAR or EAR will not have JAR "files" as such.
     *
     * @param url
     *         URL of jar
     * @return JarFile or null
     * @throws java.io.IOException
     *         If error occurs creating jar file
     */
    private static JarFile getAlternativeJarFile(URL url) throws IOException
    {
        String urlFile = url.getFile();
        // Trim off any suffix - which is prefixed by "!/" on Weblogic
        int separatorIndex = urlFile.indexOf("!/");

        // OK, didn't find that. Try the less safe "!", used on OC4J
        if (separatorIndex == -1)
        {
            separatorIndex = urlFile.indexOf('!');
        }

        if (separatorIndex != -1)
        {
            String jarFileUrl = urlFile.substring(0, separatorIndex);
            // And trim off any "file:" prefix.
            if (jarFileUrl.startsWith("file:"))
            {
                jarFileUrl = jarFileUrl.substring("file:".length());
            }

            return new JarFile(jarFileUrl);
        }

        return null;
    }

    /**
     * Variation of {@link Runnable} that throws {@link IOException}.  Still think checked exceptions are a good idea?
     */
    interface IOWork
    {
        void run() throws IOException;
    }

    /**
     * Encapsulates the data, result, and queue of deferred operations for performing the scan.
     */
    static class Job
    {
        final ClasspathMatcher matcher;

        final ClasspathURLConverter converter;

        final ClassLoader classloader;

        final Set<String> matches = CollectionFactory.newSet();

        /**
         * Explicit queue used to avoid deep tail-recursion.
         */
        final Stack<IOWork> queue = CollectionFactory.newStack();


        Job(ClasspathMatcher matcher, ClassLoader classloader, ClasspathURLConverter converter)
        {
            this.matcher = matcher;
            this.classloader = classloader;
            this.converter = converter;
        }

        Set<String> findMatches(String packagePath) throws IOException
        {

            Enumeration<URL> urls = classloader.getResources(packagePath);

            while (urls.hasMoreElements())
            {
                URL url = urls.nextElement();

                URL converted = converter.convert(url);

                scanURL(packagePath, converted);

                while (!queue.isEmpty())
                {
                    IOWork queued = queue.pop();

                    queued.run();
                }
            }

            return matches;
        }

        void scanURL(final String packagePath, final URL url) throws IOException
        {
            URLConnection connection = url.openConnection();

            JarFile jarFile;

            if (connection instanceof JarURLConnection)
            {
                jarFile = ((JarURLConnection) connection).getJarFile();
            } else
            {
                jarFile = getAlternativeJarFile(url);
            }

            if (jarFile != null)
            {
                scanJarFile(packagePath, jarFile);
            } else if (supportsDirStream(url))
            {
                queue.push(new IOWork()
                {
                    @Override
                    public void run() throws IOException
                    {
                        scanDirStream(packagePath, url);
                    }
                });
            } else
            {
                // Try scanning file system.

                scanDir(packagePath, new File(url.getFile()));
            }

        }

        /**
         * Scan a dir for classes. Will recursively look in the supplied directory and all sub directories.
         *
         * @param packagePath
         *         Name of package that this directory corresponds to.
         * @param packageDir
         *         Dir to scan for classes.
         */
        private void scanDir(String packagePath, File packageDir)
        {
            if (packageDir.exists() && packageDir.isDirectory())
            {
                for (final File file : packageDir.listFiles())
                {
                    String fileName = file.getName();

                    if (file.isDirectory())
                    {
                        final String nestedPackagePath = packagePath + fileName + "/";

                        queue.push(new IOWork()
                        {
                            @Override
                            public void run() throws IOException
                            {
                                scanDir(nestedPackagePath, file);
                            }
                        });
                    }

                    if (matcher.matches(packagePath, fileName))
                    {
                        matches.add(packagePath + fileName);
                    }
                }
            }
        }

        private void scanDirStream(String packagePath, URL packageURL) throws IOException
        {
            InputStream is;

            try
            {
                is = new BufferedInputStream(packageURL.openStream());
            } catch (FileNotFoundException ex)
            {
                // This can happen for certain application servers (JBoss 4.0.5 for example), that
                // export part of the exploded WAR for deployment, but leave part (WEB-INF/classes)
                // unexploded.

                return;
            }

            Reader reader = new InputStreamReader(is);
            LineNumberReader lineReader = new LineNumberReader(reader);

            try
            {
                while (true)
                {
                    String line = lineReader.readLine();

                    if (line == null) break;

                    if (matcher.matches(packagePath, line))
                    {
                        matches.add(packagePath + line);
                    } else
                    {

                        // This should match just directories.  It may also match files that have no extension;
                        // when we read those, none of the lines should look like class files.

                        if (FOLDER_NAME_PATTERN.matcher(line).matches())
                        {
                            final URL newURL = new URL(packageURL.toExternalForm() + line + "/");
                            final String nestedPackagePath = packagePath + line + "/";

                            queue.push(new IOWork()
                            {
                                @Override
                                public void run() throws IOException
                                {
                                    scanURL(nestedPackagePath, newURL);
                                }
                            });
                        }
                    }
                }

                lineReader.close();
                lineReader = null;
            } finally
            {
                InternalUtils.close(lineReader);
            }

        }

        private void scanJarFile(String packagePath, JarFile jarFile)
        {
            Enumeration<JarEntry> e = jarFile.entries();

            while (e.hasMoreElements())
            {
                String name = e.nextElement().getName();

                if (!name.startsWith(packagePath)) continue;

                int lastSlashx = name.lastIndexOf('/');

                String filePackagePath = name.substring(0, lastSlashx + 1);
                String fileName = name.substring(lastSlashx + 1);

                if (matcher.matches(filePackagePath, fileName))
                {
                    matches.add(name);
                }
            }
        }
    }
}
