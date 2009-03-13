// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;

import java.io.InputStream;
import static java.lang.String.format;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ReadManifest
{

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        ClassLoader loader = ReadManifest.class.getClassLoader();

        Enumeration<URL> urls = loader.getResources("META-INF/MANIFEST.MF");

        while (urls.hasMoreElements())
        {
            URL url = urls.nextElement();

            System.out.println(url);

            InputStream is = url.openStream();

            Manifest mf = new Manifest(is);

            is.close();

            printManifest(mf);
        }

    }

    static void printManifest(Manifest mf)
    {

        printAttributes(mf.getMainAttributes());

        if (false)
        {
            Map<String, Attributes> entries = mf.getEntries();
            List<String> keys = newList(entries.keySet());
            Collections.sort(keys);

            for (String key : keys)
            {
                System.out.println(format("  %s", key));

                Attributes a = entries.get(key);

                printAttributes(a);

            }
        }
    }

    private static void printAttributes(Attributes a)
    {
        for (Object key : a.keySet())
        {
            Object value = a.get(key);

            System.out.println(format("    %30s: %s", key, value));
        }

    }
}
