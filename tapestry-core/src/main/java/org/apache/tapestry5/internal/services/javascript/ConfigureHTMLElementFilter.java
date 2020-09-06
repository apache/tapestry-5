// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.services.MarkupRendererFilter;

/**
 * Responsible for writing attributes needed to configure the client, into the
 * HTML element.
 *
 * @since 5.4
 */
public class ConfigureHTMLElementFilter implements MarkupRendererFilter
{
    private final ThreadLocale threadLocale;

    private final boolean debugEnabled;

    public ConfigureHTMLElementFilter(ThreadLocale threadLocale, @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        this.threadLocale = threadLocale;
        this.debugEnabled = !productionMode;
    }

    public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
    {
        renderer.renderMarkup(writer);

        // After that's done (i.e., pretty much all rendering), touch it up a little.

        Element html = writer.getDocument().find("html");

        // If it is an HTML document, with a root HTML node, add attributes
        // to describe locale, and if debug is enabled.
        if (html != null)
        {
            html.attributes("data-locale", threadLocale.getLocale().toString());

            if (debugEnabled)
            {
                html.attributes("data-debug-enabled", "true");
            }

        }
    }
}
