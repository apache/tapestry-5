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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.services.MarkupRendererFilter;

/**
 * Injects a {@code <meta/>} element into the {@code <head/>} to identify the Tapestry page name.
 * This filter is only active during development, not production.
 *
 * @since 5.4
 */
public class PageNameMetaInjector implements MarkupRendererFilter
{
    private final RequestGlobals globals;

    public PageNameMetaInjector(RequestGlobals globals)
    {
        this.globals = globals;
    }

    public void renderMarkup(MarkupWriter writer, MarkupRenderer delegate)
    {
        delegate.renderMarkup(writer);

        String pageName = globals.getActivePageName();

        Document document = writer.getDocument();

        Element element = document.find("html/head");

        if (element != null)
        {
            element.element("meta",
                    "name", "tapestry-page-name",
                    "content", pageName);
        }

    }
}
