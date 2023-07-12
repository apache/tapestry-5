// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Component that renders a <a href="http://graphviz.org">Graphviz</a> graph using
 * <a href="https://www.npmjs.com/package/@hpcc-js/wasm">@hpcc-js/wasm</a>. It's mostly 
 * intended to be used internally at Tapestry, hence the limited set of options.
 * 
 * @tapestrydoc
 * @since 5.8.3
 */
public class Graphviz
{
    
    /**
     * A Graphviz graph described in its DOT language.
     */
    @Parameter(required = true, allowNull = false)
    @Property
    private String value;
    
    /**
     * Defines whether a link to download the graph as an SVG file should be provided.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "false")
    private boolean showDownloadLink;

    /**
     * Defines whether a the Graphviz source should be shown.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "false")
    private boolean showSource;

    @Environmental
    private JavaScriptSupport javaScriptSupport;
    
    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;
    
    @Inject
    private ComponentResources resources;
    
    @Inject
    private Messages messages;

    // Read value only once if showSource = true
    private String cachedValue;
    
    void setupRender(MarkupWriter writer)
    {
     
        cachedValue = value;
        String elementName = resources.getElementName();
        if (elementName == null)
        {
            elementName = "div";
        }
        
        final String id = javaScriptSupport.allocateClientId(resources);
        writer.element(elementName, "id", id);
        writer.end();
        
        javaScriptSupport.require("t5/core/graphviz").with(cachedValue, id, showDownloadLink);
        
        if (showDownloadLink)
        {
            writer.element("a", "href", "#", "id", id + "-download", "download", id + ".svg");
            writer.write(messages.get("download-graphviz-image"));
            writer.end();
        }
        
        if (showSource)
        {
            writer.element("pre", "id", id + "-source");
            writer.write(cachedValue);
            writer.end();
        }
        
    }
    
}
