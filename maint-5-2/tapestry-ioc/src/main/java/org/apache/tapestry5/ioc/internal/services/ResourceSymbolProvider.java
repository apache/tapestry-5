// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Makes a {@link org.apache.tapestry5.ioc.Resource} available as a {@link org.apache.tapestry5.ioc.services.SymbolProvider}
 *
 * @since 5.1.0.5
 */
public class ResourceSymbolProvider implements SymbolProvider
{
    private final Resource resource;

    private final Map<String, String> properties = CollectionFactory.newCaseInsensitiveMap();

    public ResourceSymbolProvider(final Resource resource)
    {
        this.resource = resource;

        readProperties();
    }

    private void readProperties()
    {
        Properties p = new Properties();

        InputStream is = null;

        try
        {
            is = resource.openStream();

            p.load(is);

            is.close();

            is = null;

            for (Map.Entry<Object, Object> entry : p.entrySet())
            {
                String key = entry.getKey().toString();

                properties.put(key, p.getProperty(key));
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            InternalUtils.close(is);
        }
    }

    public String valueForSymbol(String symbolName)
    {
        return properties.get(symbolName);
    }
}
