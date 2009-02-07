// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Responsible for acquiring a StAX reader and configuring an instance of {@link org.apache.tapestry5.internal.services.StaxTemplateParser},
 * which does all the real work. This is dependent on a few features of StAX2, and therefore, on the Woodstock StAX
 * parser.
 *
 * @since 5.1.0.0
 */
public class TemplateParserImpl implements TemplateParser, XMLResolver
{
    private final Map<String, URL> configuration;

    private final boolean defaultCompressWhitespace;

    private final XMLInputFactory2 inputFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();

    public TemplateParserImpl(Map<String, URL> configuration,

                              @Symbol(SymbolConstants.COMPRESS_WHITESPACE)
                              boolean defaultCompressWhitespace)
    {

        this.configuration = configuration;
        this.defaultCompressWhitespace = defaultCompressWhitespace;

        inputFactory.configureForSpeed();

        inputFactory.setXMLResolver(this);

        // Coalescing must be off, or CDATA sections can "coealesce" into CHARACTER events
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, false);

        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true);

        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
    }

    public ComponentTemplate parseTemplate(Resource templateResource)
    {
        if (!templateResource.exists())
            throw new RuntimeException(ServicesMessages.missingTemplateResource(templateResource));


        StaxTemplateParser parser;

        try
        {
            parser = new StaxTemplateParser(templateResource, inputFactory);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServicesMessages.newParserError(templateResource, ex), ex);
        }

        return parser.parse(defaultCompressWhitespace);
    }

    // XMLResolver methods

    public Object resolveEntity(String publicID,
                                String systemID,
                                String baseURI,
                                String namespace) throws XMLStreamException
    {
        URL url = configuration.get(publicID);

        try
        {
            if (url != null)
                return url.openStream();
        }
        catch (IOException ex)
        {
            throw new XMLStreamException(
                    String.format("Unable to open stream for resource %s: %s",
                                  url,
                                  InternalUtils.toMessage(ex)), ex);
        }

        return null;
    }
}
