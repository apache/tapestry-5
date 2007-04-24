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

package org.apache.tapestry.integration.app1.pages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.corelib.components.Form;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

@ComponentClass
public class ClassLoaderInspect
{
    private static final ClassLoader _classLoader = Thread.currentThread().getContextClassLoader();

    private ClassLoader _loader;

    @Persist
    private String _resource;

    @Component
    private Form _search;

    @Persist
    private List<URL> _URLs;

    @Persist
    private boolean _showMatches;

    private URL _URL;

    private JarEntry _jarEntry;

    public URL getURL()
    {
        return _URL;
    }

    public void setURL(URL url)
    {
        _URL = url;
    }

    public ClassLoader getClassLoader()
    {
        return _classLoader;
    }

    public ClassLoader getLoader()
    {
        return _loader;
    }

    public void setLoader(ClassLoader loader)
    {
        _loader = loader;
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
        return _URLs == null ? 0 : _URLs.size();
    }

    void onFailure()
    {
        _showMatches = false;
        _URLs = null;
    }

    void onSuccess()
    {
        _showMatches = false;

        _URLs = null;

        try
        {
            List<URL> urls = CollectionFactory.newList();

            Enumeration<URL> e = _classLoader.getResources(_resource);

            while (e.hasMoreElements())
                urls.add(e.nextElement());

            _URLs = urls;

            _showMatches = true;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();

            if (InternalUtils.isBlank(message))
                message = ex.getClass().getName();

            _search.recordError(message);
        }
    }

    public String getResource()
    {
        return _resource;
    }

    public void setResource(String resource)
    {
        _resource = resource;
    }

    public List<URL> getURLs()
    {
        return _URLs;
    }

    public boolean getShowMatches()
    {
        return _showMatches;
    }

    public String getContentStreamContents()
    {
        StringBuilder builder = new StringBuilder();

        try
        {
            InputStream is = _URL.openStream();
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
            URLConnection rawConnection = _URL.openConnection();

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
            return _URL.openConnection();
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    public JarEntry getJarEntry()
    {
        return _jarEntry;
    }

    public void setJarEntry(JarEntry jarEntry)
    {
        _jarEntry = jarEntry;
    }
}
