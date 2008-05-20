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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class ClassLoaderInspect
{
    private static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();

    private ClassLoader loader;

    @Persist
    private String resource;

    @Component
    private Form search;

    @Persist
    private List<URL> URLs;

    @Persist
    private boolean showMatches;

    private URL URL;

    private JarEntry jarEntry;

    public URL getURL()
    {
        return URL;
    }

    public void setURL(URL url)
    {
        URL = url;
    }

    public ClassLoader getClassLoader()
    {
        return CLASS_LOADER;
    }

    public ClassLoader getLoader()
    {
        return loader;
    }

    public void setLoader(ClassLoader loader)
    {
        this.loader = loader;
    }

    public List<ClassLoader> getLoaders()
    {
        List<ClassLoader> result = CollectionFactory.newList();

        ClassLoader current = getClass().getClassLoader();

        while (current != null)
        {
            result.add(0, current);

            current = current.getParent();
        }

        return result;
    }

    public int getListSize()
    {
        return URLs == null ? 0 : URLs.size();
    }

    void onFailure()
    {
        showMatches = false;
        URLs = null;
    }

    void onSuccess()
    {
        showMatches = false;

        URLs = null;

        try
        {
            List<URL> urls = CollectionFactory.newList();

            Enumeration<URL> e = CLASS_LOADER.getResources(resource);

            while (e.hasMoreElements())
                urls.add(e.nextElement());

            URLs = urls;

            showMatches = true;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();

            if (InternalUtils.isBlank(message))
                message = ex.getClass().getName();

            search.recordError(message);
        }
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public List<URL> getURLs()
    {
        return URLs;
    }

    public boolean getShowMatches()
    {
        return showMatches;
    }

    public String getContentStreamContents()
    {
        StringBuilder builder = new StringBuilder();

        try
        {
            InputStream is = URL.openStream();
            InputStreamReader reader = new InputStreamReader(is);

            char[] buffer = new char[1000];

            while (true)
            {
                int length = reader.read(buffer);

                if (length < 0)
                    break;

                builder.append(buffer, 0, length);
            }

            reader.close();

            return builder.toString();
        }
        catch (Exception ex)
        {
            return ex.getMessage();
        }
    }

    public List<JarEntry> getJarEntries()
    {
        try
        {
            URLConnection rawConnection = URL.openConnection();

            JarURLConnection jarConnection = (JarURLConnection) rawConnection;

            JarEntry rootEntry = jarConnection.getJarEntry();

            List<JarEntry> result = CollectionFactory.newList();

            if (rootEntry.isDirectory())
            {
                Enumeration<JarEntry> e = jarConnection.getJarFile().entries();

                while (e.hasMoreElements())
                    result.add(e.nextElement());
            }
            else
            {
                result.add(rootEntry);
            }

            return result;
        }
        catch (Exception ex)
        {
            return null;
        }

    }

    public URLConnection getURLConnection()
    {
        try
        {
            return URL.openConnection();
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    public JarEntry getJarEntry()
    {
        return jarEntry;
    }

    public void setJarEntry(JarEntry jarEntry)
    {
        this.jarEntry = jarEntry;
    }
}
