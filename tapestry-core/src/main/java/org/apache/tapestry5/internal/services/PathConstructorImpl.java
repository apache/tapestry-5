package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PathConstructor;

public class PathConstructorImpl implements PathConstructor
{
    private final String clientPrefix, dispatchPrefix;

    public PathConstructorImpl(
            @Symbol(SymbolConstants.CONTEXT_PATH) String contextPath,
            @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder)
    {
        StringBuilder b = new StringBuilder("/");

        if (applicationFolder.length() > 0)
        {
            b.append(applicationFolder);
            b.append("/");
        }

        dispatchPrefix = b.toString();

        clientPrefix = contextPath + dispatchPrefix;
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
