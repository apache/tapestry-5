// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Symbol;

import java.net.URL;
import java.util.Map;

/**
 * Parses Tapestry XML template files into {@link ComponentTemplate} instances.
 * A new instance of {@link SaxTemplateParser} is created for each document
 * parsed.
 *
 * @since 5.1.0.0
 */
public class TemplateParserImpl implements TemplateParser
{
    private final Map<String, URL> configuration;

    private final boolean defaultCompressWhitespace;

    private final OperationTracker tracker;

    public TemplateParserImpl(Map<String, URL> configuration,

                              @Symbol(SymbolConstants.COMPRESS_WHITESPACE)
                              boolean defaultCompressWhitespace, OperationTracker tracker)
    {
        this.configuration = configuration;
        this.defaultCompressWhitespace = defaultCompressWhitespace;
        this.tracker = tracker;
    }

    public ComponentTemplate parseTemplate(final Resource templateResource)
    {
        if (!templateResource.exists())
            throw new RuntimeException(String.format("Template resource %s does not exist.", templateResource));

        return tracker.invoke("Parsing component template " + templateResource, new Invokable<ComponentTemplate>()
        {
            public ComponentTemplate invoke()
            {
                return new SaxTemplateParser(templateResource, configuration).parse(defaultCompressWhitespace);
            }
        });
    }

    public Map<String, URL> getDTDURLMappings()
    {
        return configuration;
    }
}
