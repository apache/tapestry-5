// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PathConstructor;

public class PathConstructorImpl implements PathConstructor
{
    private final String clientPrefix, dispatchPrefix;

    public PathConstructorImpl(
            @Symbol(TapestryHttpSymbolConstants.CONTEXT_PATH) String contextPath,
            @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder)
    {
        StringBuilder b = new StringBuilder("/");

        if (applicationFolder.length() > 0)
        {
            b.append(applicationFolder);
            b.append('/');
        }

        dispatchPrefix = b.toString();

        // If you mis-configure embedded Tomcat, you can get a contextPath of "/" rather than "".
        // To make things fool proof, we handle that case.
        clientPrefix = (contextPath.equals("/") ? "" : contextPath) + dispatchPrefix;
    }

    public String constructClientPath(String... terms)
    {
        return build(clientPrefix, terms);
    }

    public String constructDispatchPath(String... terms)
    {
        return build(dispatchPrefix, terms);
    }

    private String build(String prefix, String... terms)
    {
        StringBuilder b = new StringBuilder(prefix);
        String sep = "";

        for (String term : terms)
        {
            b.append(sep);
            b.append(term);

            sep = "/";
        }


        return b.toString();
    }
}
