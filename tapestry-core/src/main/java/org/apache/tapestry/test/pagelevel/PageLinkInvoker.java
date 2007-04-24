// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.test.pagelevel;

import org.apache.tapestry.dom.Document;
import org.apache.tapestry.internal.services.ComponentInvocation;
import org.apache.tapestry.internal.services.MarkupWriterImpl;
import org.apache.tapestry.internal.services.PageLinkHandler;
import org.apache.tapestry.internal.services.PageMarkupRenderer;
import org.apache.tapestry.internal.services.PageRenderer;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.services.MarkupWriterFactory;

/**
 * Simulates a click on a page link.
 */
public class PageLinkInvoker implements ComponentInvoker
{
    private final Registry _registry;

    private final PageLinkHandler _pageLinkHandler;

    private final PageMarkupRenderer _renderer;

    private final MarkupWriterFactory _writerFactory;

    public PageLinkInvoker(Registry registry)
    {
        _registry = registry;
        _pageLinkHandler = _registry.getService(PageLinkHandler.class);
        _renderer = _registry.getService(PageMarkupRenderer.class);
        _writerFactory = _registry.getService(MarkupWriterFactory.class);
    }

    /**
     * Click on the page link.
     * 
     * @param invocation
     *            The ComponentInvocation object corresponding to the page link.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document invoke(ComponentInvocation invocation)
    {
        try
        {
            final MarkupWriterImpl writer = (MarkupWriterImpl) _writerFactory.newMarkupWriter();
            _pageLinkHandler.handle(invocation, new PageRenderer()
            {

                public void renderPage(Page page)
                {
                    _renderer.renderPageMarkup(page, writer);
                }

            });
            return writer.getDocument();
        }
        finally
        {
            _registry.cleanupThread();
        }
    }

}
