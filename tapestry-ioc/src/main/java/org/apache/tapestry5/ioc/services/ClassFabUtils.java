// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Handy method useful when creating new classes using {@link org.apache.tapestry5.ioc.services.ClassFab}.
 *
 * @deprecated Deprecated in Tapestry 5.3, to be removed in 5.4 with no replacement
 */
@SuppressWarnings("all")
public final class ClassFabUtils
{
    /**
     * Returns true if the method is the standard toString() method. Very few interfaces will ever include this method
     * as part of the interface, but we have to be sure.
     */
    public static boolean isToString(Method method)
    {
        if (!method.getName().equals("toString"))
            return false;

        if (method.getParameterTypes().length > 0)
            return false;

        return method.getReturnType().equals(String.class);
    }

    /**
     * Converts a URL with protocol "file" to a File instance.
     *
     * @since 5.2.0
     */
    public static File toFileFromFileProtocolURL(URL url)
    {
        assert url != null;

        if (!url.getProtocol().equals("file"))
            throw new IllegalArgumentException(String.format("URL %s does not use the 'file' protocol.", url));

        // http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html

        try
        {
            return new File(url.toURI());
        } catch (URISyntaxException ex)
        {
            return new File(url.getPath());
        }
    }
}
