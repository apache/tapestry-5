// Copyright 2007, 2008, 2009, 2013 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.DefaultMarkupModel;
import org.apache.tapestry5.dom.Html5MarkupModel;
import org.apache.tapestry5.dom.MarkupModel;
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.internal.parser.DTDToken;
import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.internal.parser.TokenType;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

public class MarkupWriterFactoryImpl implements MarkupWriterFactory
{
    private final PageContentTypeAnalyzer pageContentTypeAnalyzer;

    private final RequestPageCache cache;
    
    private final ComponentTemplateSource templateSource;
    
    private final ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer;

    private final MarkupModel htmlModel = new DefaultMarkupModel();

    private final MarkupModel xmlModel = new XMLMarkupModel();

    private final MarkupModel htmlPartialModel = new DefaultMarkupModel(true);

    private final MarkupModel xmlPartialModel = new XMLMarkupModel(true);
    
    private final MarkupModel html5Model = new Html5MarkupModel();
    
    private final MarkupModel html5PartialModel = new Html5MarkupModel(true);

    public MarkupWriterFactoryImpl(PageContentTypeAnalyzer pageContentTypeAnalyzer,
            RequestPageCache cache, ComponentTemplateSource templateSource,
            ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer)
    {
        this.pageContentTypeAnalyzer = pageContentTypeAnalyzer;
        this.cache = cache;
        this.templateSource = templateSource;
        this.componentRequestSelectorAnalyzer = componentRequestSelectorAnalyzer;
    }

    public MarkupWriter newMarkupWriter(ContentType contentType)
    {
        return constructMarkupWriter(contentType, false, false);
    }

    public MarkupWriter newPartialMarkupWriter(ContentType contentType)
    {
        return constructMarkupWriter(contentType, true, false);
    }

    private MarkupWriter constructMarkupWriter(ContentType contentType, boolean partial, boolean HTML5)
    {
        final String mimeType = contentType.getMimeType();
        boolean isHTML = mimeType.equalsIgnoreCase("text/html");

        MarkupModel model;
        
        if(isHTML)
            model = HTML5 ? (partial ? html5PartialModel : html5Model) : (partial ? htmlPartialModel : htmlModel);
        else
            model = partial ? xmlPartialModel : xmlModel;
        // The charset parameter sets the encoding attribute of the XML declaration, if
        // not null and if using the XML model.

        return new MarkupWriterImpl(model, contentType.getCharset(), mimeType);
    }

    public MarkupWriter newMarkupWriter(String pageName)
    {
        Page page = cache.get(pageName);

        return newMarkupWriter(page);
    }
    
    private boolean hasHTML5Doctype(Page page)
    {
        ComponentModel componentModel = page.getRootComponent().getComponentResources().getComponentModel();
        
        ComponentResourceSelector selector = componentRequestSelectorAnalyzer.buildSelectorForRequest();
        
        List<TemplateToken> tokens = templateSource.getTemplate(componentModel, selector).getTokens();
        
        DTDToken dtd = null;
        
        for(TemplateToken token : tokens)
        {
            if(token.getTokenType() == TokenType.DTD)
            {
                dtd = (DTDToken) token;
                break;
            }
        }
        
        return dtd != null && dtd.name.equalsIgnoreCase("html") && dtd.publicId == null && dtd.systemId == null;
    }

    public MarkupWriter newMarkupWriter(Page page)
    {
        boolean isHTML5 = hasHTML5Doctype(page);
        
        ContentType contentType = pageContentTypeAnalyzer.findContentType(page);
        
        return constructMarkupWriter(contentType, false, isHTML5);
    }

    public MarkupWriter newPartialMarkupWriter(Page page)
    {
        boolean isHTML5 = hasHTML5Doctype(page);
        
        ContentType contentType = pageContentTypeAnalyzer.findContentType(page);
        
        return constructMarkupWriter(contentType, true, isHTML5);
    }

    public MarkupWriter newPartialMarkupWriter(String pageName)
    {
        Page page = cache.get(pageName);
        
        return newPartialMarkupWriter(page);
    }
}
