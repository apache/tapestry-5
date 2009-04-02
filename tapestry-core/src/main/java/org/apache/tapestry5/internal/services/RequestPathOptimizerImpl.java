// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;

import java.util.regex.Pattern;

public class RequestPathOptimizerImpl implements RequestPathOptimizer
{
    private final Request request;

    private final boolean forceAbsolute;

    /**
     * Used to split a URI up into individual folder/file names. Any number of consecutive slashes are treated as a
     * single slash.
     */
    private final Pattern SLASH_PATTERN = Pattern.compile("/+");

    public RequestPathOptimizerImpl(Request request,

                                    @Symbol(SymbolConstants.FORCE_ABSOLUTE_URIS)
                                    boolean forceAbsolute)
    {
        this.request = request;
        this.forceAbsolute = forceAbsolute;
    }

    public String optimizePath(String absolutePath)
    {
        if (forceAbsolute ||
                absolutePath.equals("/") ||
                request.isXHR() ||
                request.getAttribute(InternalConstants.GENERATING_RENDERED_PAGE) != null)
        {
            return absolutePath;
        }

        String requestPath = request.getPath();

        StringBuilder builder = new StringBuilder();

        builder.append(request.getContextPath());

        builder.append(requestPath);

        String requestURI = builder.toString();

        String[] requestTerms = SLASH_PATTERN.split(requestURI);

        int questionx = absolutePath.indexOf('?');

        String path = questionx < 0 ? absolutePath : absolutePath.substring(0, questionx);
        String suffix = questionx < 0 ? "" : absolutePath.substring(questionx);

        // Degenerate case when getting the root application

        if (requestPath.endsWith("/") || requestPath.equals("")) requestTerms = add(requestTerms, "");

        String[] pathTerms = SLASH_PATTERN.split(path);

        builder.setLength(0);

        int i = 0;
        while (true)
        {

            if (i >= requestTerms.length - 1) break;

            if (i >= pathTerms.length - 1) break;

            if (!requestTerms[i].equals(pathTerms[i])) break;

            i++;
        }

        // Found the point of divergence.

        for (int j = i; j < requestTerms.length - 1; j++)
        {
            builder.append("../");
        }

        String sep = "";

        for (int j = i; j < pathTerms.length; j++)
        {
            builder.append(sep);

            builder.append(pathTerms[j]);

            sep = "/";
        }

        // A colon before the first slash confuses the browser; it thinks its a really long
        // protocol specifier (like "http:").

        int firstColon = builder.indexOf(":");
        if (firstColon > 0)
        {
            int slashx = builder.indexOf("/");

            // Prefixing with "./" disambiguates the path and the colon, though
            // most likely we're going to end up choosing the full path rather than
            // the relative one.

            if (slashx < 0 || slashx > firstColon) builder.insert(0, "./");
        }

        if (builder.length() < path.length())
        {
            // Restore the query parameter portion of the URL path
            builder.append(suffix);

            return builder.toString();
        }

        // The absolute path is actually shorter than the relative path, so just return the absolute
        // path.

        return absolutePath;
    }

    private String[] add(String[] array, String s)
    {
        String[] newArray = new String[array.length + 1];

        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = s;

        return newArray;
    }
}
