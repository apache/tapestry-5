// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.linktrans.services;

import java.util.Locale;

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.ArrayEventContext;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;

public class AppPageRenderLinkTransformer implements PageRenderLinkTransformer
{
    @Inject
    private LocalizationSetter localizationSetter;

    @Inject
    private PersistentLocale persistentLocale;

    @Inject
    private TypeCoercer typeCoercer;

    public PageRenderRequestParameters decodePageRenderRequest(Request request)
    {
        String path = request.getPath();

        String[] split = path.substring(1).split("/");

        if (split.length == 1 && split[0].equals("")) return null;
        
        int pacx = 0;

        String possibleLocaleName = split[0];

        // Might be just the page activation context, or it might be locale then page
        // activation context

        boolean localeSpecified = localizationSetter.isSupportedLocaleName(possibleLocaleName);

        if (localeSpecified)
        {
            pacx++;
        }

        if (pacx >= split.length)
            return null;

        if (localeSpecified)
            localizationSetter.setLocaleFromLocaleName(possibleLocaleName);

        boolean isLoopback = request.getParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME) != null;

        return new PageRenderRequestParameters("View", new ArrayEventContext(typeCoercer, split[pacx]), isLoopback);
    }

    public Link transformPageRenderLink(Link defaultLink, PageRenderRequestParameters parameters)
    {
        if (!parameters.getLogicalPageName().equals("View"))
            return null;

        StringBuilder path = new StringBuilder();

        Locale locale = persistentLocale.get();

        if (locale != null)
            path.append('/').append(locale.toString());

        path.append('/');

        // Cheating: we know there's exactly one value in the context.

        path.append(parameters.getActivationContext().get(String.class, 0));

        return defaultLink.copyWithBasePath(path.toString());
    }

}
