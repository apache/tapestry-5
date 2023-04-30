// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import java.io.IOException;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ExceptionHandlerAssistant;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.util.FormsRequirePostException;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestExceptionHandler;

/**
 * Handles {@link FormsRequirePostException}s (thrown by the {@link Form} component when the request method was
 * other than post) by redirecting to the page containing the form.
 * <p>
 * This assistant is contributed to the default {@link RequestExceptionHandler} service in a way that it is
 * effective only in production mode.
 * 
 * @see ExceptionHandlerAssistant
 * @see RequestExceptionHandler
 * @see SymbolConstants#PRODUCTION_MODE
 */
public class FormsRequirePostExceptionHandlerAssistant implements ExceptionHandlerAssistant
{
    final ComponentSource componentSource;

    final PageRenderLinkSource linkSource;

    public FormsRequirePostExceptionHandlerAssistant(final ComponentSource componentSource, final PageRenderLinkSource linkSource)
    {
        this.componentSource = componentSource;
        this.linkSource = linkSource;
    }

    @Override
    public Object handleRequestException(Throwable exception, List<Object> exceptionContext) throws IOException
    {
        ComponentResources cr = componentSource.getActivePage().getComponentResources();

        String pageName = cr.getPageName();

        return linkSource.createPageRenderLink(pageName);
    }
}
