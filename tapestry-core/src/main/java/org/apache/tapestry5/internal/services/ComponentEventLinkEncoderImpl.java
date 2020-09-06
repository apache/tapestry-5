// Copyright 2009, 2010, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.*;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.security.ClientWhitelist;

import java.util.List;
import java.util.Locale;

public class ComponentEventLinkEncoderImpl implements ComponentEventLinkEncoder
{
    private final ComponentClassResolver componentClassResolver;

    private final ContextPathEncoder contextPathEncoder;

    private final LocalizationSetter localizationSetter;

    private final Response response;

    private final RequestSecurityManager requestSecurityManager;

    private final BaseURLSource baseURLSource;

    private final PersistentLocale persistentLocale;

    private final boolean encodeLocaleIntoPath;

    private final MetaDataLocator metaDataLocator;

    private final ClientWhitelist clientWhitelist;

    private final String contextPath;

    private final String applicationFolder;

    private final String applicationFolderPrefix;

    private static final int BUFFER_SIZE = 100;

    private static final char SLASH = '/';

    public ComponentEventLinkEncoderImpl(ComponentClassResolver componentClassResolver,
                                         ContextPathEncoder contextPathEncoder, LocalizationSetter localizationSetter,
                                         Response response, RequestSecurityManager requestSecurityManager, BaseURLSource baseURLSource,
                                         PersistentLocale persistentLocale,
                                         @Symbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH)
                                         boolean encodeLocaleIntoPath,
                                         @Symbol(TapestryHttpSymbolConstants.CONTEXT_PATH)
                                         String contextPath,
                                         @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder,
                                         MetaDataLocator metaDataLocator,
                                         ClientWhitelist clientWhitelist)
    {
        this.componentClassResolver = componentClassResolver;
        this.contextPathEncoder = contextPathEncoder;
        this.localizationSetter = localizationSetter;
        this.response = response;
        this.requestSecurityManager = requestSecurityManager;
        this.baseURLSource = baseURLSource;
        this.persistentLocale = persistentLocale;
        this.encodeLocaleIntoPath = encodeLocaleIntoPath;
        this.contextPath = contextPath;
        this.applicationFolder = applicationFolder;
        this.metaDataLocator = metaDataLocator;
        this.clientWhitelist = clientWhitelist;

        boolean hasAppFolder = applicationFolder.equals("");

        applicationFolderPrefix = hasAppFolder ? null : SLASH + applicationFolder;
    }

    public Link createPageRenderLink(PageRenderRequestParameters parameters)
    {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        // Build up the absolute URI.

        String activePageName = parameters.getLogicalPageName();

        builder.append(contextPath);

        encodeAppFolderAndLocale(builder);

        builder.append(SLASH);

        String encodedPageName = encodePageName(activePageName);

        builder.append(encodedPageName);

        appendContext(encodedPageName.length() > 0, parameters.getActivationContext(), builder);

        Link link = new LinkImpl(builder.toString(), false, requestSecurityManager.checkPageSecurity(activePageName),
                response, contextPathEncoder, baseURLSource);

        if (parameters.isLoopback())
        {
            link.addParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME, "t");
        }

        return link;
    }

    private void encodeAppFolderAndLocale(StringBuilder builder)
    {
        if (!applicationFolder.equals(""))
        {
            builder.append(SLASH).append(applicationFolder);
        }

        if (encodeLocaleIntoPath)
        {
            Locale locale = persistentLocale.get();

            if (locale != null)
            {
                builder.append(SLASH);
                builder.append(locale.toString());
            }
        }
    }

    private String encodePageName(String pageName)
    {
        if (pageName.equalsIgnoreCase("index"))
            return "";

        String encoded = pageName.toLowerCase();

        if (!encoded.endsWith("/index"))
            return encoded;

        return encoded.substring(0, encoded.length() - 6);
    }

    public Link createComponentEventLink(ComponentEventRequestParameters parameters, boolean forForm)
    {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        // Build up the absolute URI.

        String activePageName = parameters.getActivePageName();
        String containingPageName = parameters.getContainingPageName();
        String eventType = parameters.getEventType();

        String nestedComponentId = parameters.getNestedComponentId();
        boolean hasComponentId = InternalUtils.isNonBlank(nestedComponentId);

        builder.append(contextPath);

        encodeAppFolderAndLocale(builder);

        builder.append(SLASH);
        builder.append(activePageName.toLowerCase());

        if (hasComponentId)
        {
            builder.append('.');
            builder.append(nestedComponentId);
        }

        if (!hasComponentId || !eventType.equals(EventConstants.ACTION))
        {
            builder.append(':');
            builder.append(encodePageName(eventType));
        }

        appendContext(true, parameters.getEventContext(), builder);

        Link result = new LinkImpl(builder.toString(), forForm,
                requestSecurityManager.checkPageSecurity(activePageName), response, contextPathEncoder, baseURLSource);

        EventContext pageActivationContext = parameters.getPageActivationContext();

        if (pageActivationContext.getCount() != 0)
        {
            // Reuse the builder
            builder.setLength(0);
            appendContext(true, pageActivationContext, builder);

            // Omit that first slash
            result.addParameter(InternalConstants.PAGE_CONTEXT_NAME, builder.substring(1));
        }

        // TAPESTRY-2044: Sometimes the active page drags in components from another page and we
        // need to differentiate that.

        if (!containingPageName.equalsIgnoreCase(activePageName))
            result.addParameter(InternalConstants.CONTAINER_PAGE_NAME, encodePageName(containingPageName));

        return result;
    }

    /**
     * Splits path at slashes into a <em>mutable</em> list of strings. Empty terms, including the
     * expected leading term (paths start with a '/') are dropped.
     *
     * @param path
     * @return mutable list of path elements
     */
    private List<String> splitPath(String path)
    {
        String[] split = TapestryInternalUtils.splitPath(path);

        List<String> result = CollectionFactory.newList();

        for (String name : split)
        {
            if (name.length() > 0)
            {
                result.add(name);
            }
        }

        return result;
    }

    private String joinPath(List<String> path)
    {
        if (path.isEmpty())
        {
            return "";
        }

        StringBuilder builder = new StringBuilder(100);
        String sep = "";

        for (String term : path)
        {
            builder.append(sep).append(term);
            sep = "/";
        }

        return builder.toString();
    }

    public ComponentEventRequestParameters decodeComponentEventRequest(Request request)
    {
        String explicitLocale = null;

        // Split the path around slashes into a mutable list of terms, which will be consumed term by term.

        String requestPath = request.getPath();

        if (applicationFolderPrefix != null)
        {
            requestPath = removeApplicationPrefix(requestPath);
        }

        List<String> path = splitPath(requestPath);



        if (path.isEmpty())
        {
            return null;
        }

        // Next up: the locale (which is optional)

        String potentialLocale = path.get(0);

        if (localizationSetter.isSupportedLocaleName(potentialLocale))
        {
            explicitLocale = potentialLocale;
            path.remove(0);
        }

        StringBuilder pageName = new StringBuilder(100);
        String sep = "";

        while (!path.isEmpty())
        {
            String name = path.remove(0);
            String eventType = EventConstants.ACTION;
            String nestedComponentId = "";

            boolean found = false;

            // First, look for an explicit action name.

            int colonx = name.lastIndexOf(':');

            if (colonx > 0)
            {
                found = true;
                eventType = name.substring(colonx + 1);
                name = name.substring(0, colonx);
            }

            int dotx = name.indexOf('.');

            if (dotx > 0)
            {
                found = true;
                nestedComponentId = name.substring(dotx + 1);
                name = name.substring(0, dotx);
            }

            pageName.append(sep).append(name);

            if (found)
            {
                ComponentEventRequestParameters result = validateAndConstructComponentEventRequest(request, pageName.toString(), nestedComponentId, eventType, path);

                if (result == null)
                {
                    return result;
                }

                if (explicitLocale == null)
                {
                    setLocaleFromRequest(request);
                } else
                {
                    localizationSetter.setLocaleFromLocaleName(explicitLocale);
                }

                return result;
            }

            // Continue on to the next name in the path
            sep = "/";
        }

        // Path empty before finding something that looks like a component id or event name, so
        // it is not a component event request.

        return null;
    }

    private ComponentEventRequestParameters validateAndConstructComponentEventRequest(Request request, String pageName, String nestedComponentId, String eventType, List<String> remainingPath)
    {
        if (!componentClassResolver.isPageName(pageName))
        {
            return null;
        }

        String activePageName = componentClassResolver.canonicalizePageName(pageName);

        if (isWhitelistOnlyAndNotValid(activePageName))
        {
            return null;
        }

        String value = request.getParameter(InternalConstants.CONTAINER_PAGE_NAME);

        String containingPageName = value == null
                ? activePageName
                : componentClassResolver.canonicalizePageName(value);

        EventContext eventContext = contextPathEncoder.decodePath(joinPath(remainingPath));
        EventContext activationContext = contextPathEncoder.decodePath(request.getParameter(InternalConstants.PAGE_CONTEXT_NAME));

        return new ComponentEventRequestParameters(activePageName, containingPageName, nestedComponentId, eventType,
                activationContext, eventContext);
    }

    private void setLocaleFromRequest(Request request)
    {
        Locale locale = request.getLocale();

        // And explicit locale will have invoked setLocaleFromLocaleName().

        localizationSetter.setNonPersistentLocaleFromLocaleName(locale.toString());
    }

    public PageRenderRequestParameters decodePageRenderRequest(Request request)
    {
        boolean explicitLocale = false;

        // The extended name may include a page activation context. The trick is
        // to figure out where the logical page name stops and where the
        // activation context begins. Further, strip out the leading slash.

        String path = request.getPath();

        if (applicationFolderPrefix != null)
        {
            path = removeApplicationPrefix(path);
        }


        // TAPESTRY-1343: Sometimes path is the empty string (it should always be at least a slash,
        // but Tomcat may return the empty string for a root context request).

        String extendedName = path.length() == 0 ? path : path.substring(1);

        // Ignore trailing slashes in the path.
        while (extendedName.endsWith("/"))
        {
            extendedName = extendedName.substring(0, extendedName.length() - 1);
        }

        int slashx = extendedName.indexOf('/');

        // So, what can we have left?
        // 1. A page name
        // 2. A locale followed by a page name
        // 3. A page name followed by activation context
        // 4. A locale name, page name, activation context
        // 5. Just activation context (for root Index page)
        // 6. A locale name followed by activation context

        String possibleLocaleName = slashx > 0 ? extendedName.substring(0, slashx) : extendedName;

        if (localizationSetter.setLocaleFromLocaleName(possibleLocaleName))
        {
            extendedName = slashx > 0 ? extendedName.substring(slashx + 1) : "";
            explicitLocale = true;
        }

        slashx = extendedName.length();
        boolean atEnd = true;

        while (slashx > 0)
        {
            String pageName = extendedName.substring(0, slashx);
            String pageActivationContext = atEnd ? "" : extendedName.substring(slashx + 1);

            PageRenderRequestParameters parameters = checkIfPage(request, pageName, pageActivationContext);

            if (parameters != null)
            {
                return parameters;
            }

            // Work backwards, splitting at the next slash.
            slashx = extendedName.lastIndexOf('/', slashx - 1);

            atEnd = false;
        }

        // OK, maybe its all page activation context for the root Index page.

        PageRenderRequestParameters result = checkIfPage(request, "", extendedName);

        if (result != null && !explicitLocale)
        {
            setLocaleFromRequest(request);
        }

        return result;
    }

    private String removeApplicationPrefix(String path) {
        int prefixLength = applicationFolderPrefix.length();

        assert path.substring(0, prefixLength).equalsIgnoreCase(applicationFolderPrefix);

        // This checks that the character after the prefix is a slash ... the extra complexity
        // only seems to occur in Selenium. There's some ambiguity about what to do with a request for
        // the application folder that doesn't end with a slash. Manuyal with Chrome and IE 8 shows that such
        // requests are passed through with a training slash,  automated testing with Selenium and FireFox
        // can include requests for the folder without the trailing slash.

        assert path.length() <= prefixLength || path.charAt(prefixLength) == '/';

        // Strip off the folder prefix (i.e., "/foldername"), leaving the rest of the path (i.e., "/en/pagename").

        path = path.substring(prefixLength);
        return path;
    }

    private PageRenderRequestParameters checkIfPage(Request request, String pageName, String pageActivationContext)
    {
        if (!componentClassResolver.isPageName(pageName))
        {
            return null;
        }
        String canonicalized = componentClassResolver.canonicalizePageName(pageName);

        // If the page is only visible to the whitelist, but the request is not on the whitelist, then
        // pretend the page doesn't exist!
        if (isWhitelistOnlyAndNotValid(canonicalized))
        {
            return null;
        }
        try
        {
            EventContext activationContext = contextPathEncoder.decodePath(pageActivationContext);

            boolean loopback = request.getParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME) != null;

            return new PageRenderRequestParameters(canonicalized, activationContext, loopback);
        } catch (IllegalArgumentException e)
        {
            // TAP5-2436
            return null;
        }
    }

    private boolean isWhitelistOnlyAndNotValid(String canonicalized)
    {
        return metaDataLocator.findMeta(MetaDataConstants.WHITELIST_ONLY_PAGE, canonicalized, boolean.class) &&
                !clientWhitelist.isClientRequestOnWhitelist();
    }

    public void appendContext(boolean seperatorRequired, EventContext context, StringBuilder builder)
    {
        String encoded = contextPathEncoder.encodeIntoPath(context);

        if (encoded.length() > 0)
        {
            if (seperatorRequired)
            {
                builder.append(SLASH);
            }

            builder.append(encoded);
        }
    }
}
