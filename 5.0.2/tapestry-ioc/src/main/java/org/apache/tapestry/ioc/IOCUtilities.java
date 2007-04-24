// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc;

import static org.apache.tapestry.ioc.IOCConstants.MODULE_BUILDER_MANIFEST_ENTRY_NAME;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

/**
 * A collection of utility methods for a couple of different areas, including creating the initial
 * {@link org.apache.tapestry.ioc.Registry}.
 */
public final class IOCUtilities
{
    private IOCUtilities()
    {
    }

    /**
     * Construct a default registry, including modules identify via the Tapestry-Module-Classes
     * Manifest entry.
     * 
     * @return constructed Registry
     */
    public static Registry buildDefaultRegistry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        addDefaultModules(builder);

        return builder.build();
    }

    /**
     * Scans the classpath for JAR Manifests that contain the Tapestry-Module-Classes attribute and
     * adds each corresponding class to the RegistryBuilder.
     * 
     * @param builder
     *            the builder to which modules will be added
     */
    public static void addDefaultModules(RegistryBuilder builder)
    {
        try
        {
            Enumeration<URL> urls = builder.getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (urls.hasMoreElements())
            {
                URL url = urls.nextElement();

                addModulesInManifest(builder, url);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static void addModulesInManifest(RegistryBuilder builder, URL url) throws IOException
    {
        InputStream in = null;

        try
        {
            in = url.openStream();

            Manifest mf = new Manifest(in);

            in.close();

            in = null;

            addModulesInManifest(builder, mf);
        }
        finally
        {
            close(in);
        }
    }

    static void addModulesInManifest(RegistryBuilder builder, Manifest mf)
    {
        String list = mf.getMainAttributes().getValue(MODULE_BUILDER_MANIFEST_ENTRY_NAME);

        if (list == null)
            return;

        String[] classnames = list.split(",");

        for (String classname : classnames)
        {
            builder.add(classname.trim());
        }
    }

    /**
     * Closes an input stream (or other Closeable), ignoring any exception.
     * 
     * @param closeable
     *            the thing to close, or null to close nothing
     */
    private static void close(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException ex)
            {
                // Ignore.
            }
        }
    }

    /**
     * Returns a fully qualfied id. If the id contains a '.', then it is returned unchanged.
     * Otherwise, the module's id is prefixed (with a seperator '.') and returned;
     */
    public static String toQualifiedId(String moduleId, String id)
    {
        if (id.indexOf('.') > 0)
            return id;

        return moduleId + "." + id;
    }

    /**
     * Qualifies a list of interceptor service ids provided for an interceptor contribution. The
     * special value "*" is not qualified.
     */

    public static String qualifySimpleIdList(String sourceModuleId, String list)
    {
        if (list == null || list.equals("") || list.equals("*"))
            return list;

        String[] items = list.split("\\s*,\\s*");

        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < items.length; i++)
        {
            if (i > 0)
                buffer.append(",");

            buffer.append(toQualifiedId(sourceModuleId, items[i]));
        }

        return buffer.toString();
    }

    /**
     * Removes the module name from a fully qualified id
     */
    public static String toSimpleId(String id)
    {
        int lastPoint = id.lastIndexOf('.');
        if (lastPoint > 0)
            return id.substring(lastPoint + 1, id.length());

        return id;
    }

    /**
     * Extracts the module name from a fully qualified id Returns null if id contains no module
     */
    public static String extractModuleId(String id)
    {
        int lastPoint = id.lastIndexOf('.');
        if (lastPoint > 0)
            return id.substring(0, lastPoint);

        return null;
    }
}
