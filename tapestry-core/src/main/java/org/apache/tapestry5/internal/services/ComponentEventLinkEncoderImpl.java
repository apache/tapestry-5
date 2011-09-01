// Copyright 2009, 2010 The Apache Software Foundation
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
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.*;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentEventLinkEncoderImpl implements ComponentEventLinkEncoder
{
    private final ComponentClassResolver componentClassResolver;

    private final ContextPathEncoder contextPathEncoder;

    private final LocalizationSetter localizationSetter;

    private final Request request;

    private final Response response;

    private final RequestSecurityManager requestSecurityManager;

    private final BaseURLSource baseURLSource;

    private final PersistentLocale persistentLocale;

    private final boolean encodeLocaleIntoPath;

    private final String applicationFolder;

    private static final int BUFFER_SIZE = 100;

    private static final char SLASH = '/';

    // A beast that recognizes all the elements of a path in a single go.
    // We skip the leading slash, then take the next few terms (until a dot or a colon)
    // as the page name. Then there's a sequence that sees a dot
    // and recognizes the nested component id (which may be missing), which ends
    // at the colon, or at the slash (or the end of the string). The colon identifies
    // the event name (the event name is also optional). A valid path will always have
    // a nested component id or an event name (or both) ... when both are missing, then the
    // path is most likely a page render request. After the optional event name,
    // the next piece is the action context, which is the remainder of the path.

    private final Pattern PATH_PATTERN;

    // Constants for the match groups in the above pattern.
    private static final int LOGICAL_PAGE_NAME = 1;
    private static final int NESTED_ID = 6;
    private static final int EVENT_NAME = 9;
    private static final int CONTEXT = 11;

    public ComponentEventLinkEncoderImpl(ComponentClassResolver componentClassResolver,
                                         ContextPathEncoder contextPathEncoder, LocalizationSetter localizationSetter, Request request,
                                         Response response, RequestSecurityManager requestSecurityManager, BaseURLSource baseURLSource,
                                         PersistentLocale persistentLocale, @Symbol(SymbolConstants.ENCODE_LOCALE_INTO_PATH)
    boolean encodeLocaleIntoPath, @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder)
    {
        this.componentClassResolver = componentClassResolver;
        this.contextPathEncoder = contextPathEncoder;
        this.localizationSetter = localizationSetter;
        this.request = request;
        this.response = response;
        this.requestSecurityManager = requestSecurityManager;
        this.baseURLSource = baseURLSource;
        this.persistentLocale = persistentLocale;
        this.encodeLocaleIntoPath = encodeLocaleIntoPath;
        this.applicationFolder = applicationFolder;

        String folderPattern = applicationFolder.equals("") ? "" : SLASH + applicationFolder;

        PATH_PATTERN = Pattern.compile(

                "^/" + // The leading slash is recognized but skipped
                        folderPattern + // The folder containing the application (TAP5-743)
                        "(((\\w+)/)*(\\w+))" + // A series of folder names leading up to the page name, forming
                        // the logical page name (may include the locale name)
                        "(\\.(\\w+(\\.\\w+)*))?" + // The first dot separates the page name from the nested
                        // component id
                        "(\\:(\\w+))?" + // A colon, then the event type
                        "(/(.*))?", // A slash, then the action context
                Pattern.COMMENTS);
    }

    public Link createPageRenderLink(PageRenderRequestParameters parameters)
    {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        // Build up the absolute URI.

        String activePageName = parameters.getLogicalPageName();

        builder.append(request.getContextPath());

        encodeAppFolderAndLocale(builder);

        builder.append(SLASH);

        String encodedPageName = encodePageName(activePageName);

        builder.append(encodedPageName);

        appendContext(encodedPageName.length() > 0, parameters.getActivationContext(), builder);

        Link link = new LinkImpl(builder.toString(), false, requestSecurityManager.checkPageSecurity(activePageName),
                response, contextPathEncoder, baseURLSource);

        if (parameters.isLoopback())
            link.addParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME, "t");

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

        builder.append(request.getContextPath());

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
            builder.append(":");
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

    public ComponentEventRequestParameters decodeComponentEventRequest(Request request)
    {
        Matcher matcher = PATH_PATTERN.matcher(request.getPath());

        if (!matcher.matches())
            return null;

        String nestedComponentId = matcher.group(NESTED_ID);

        String eventType = matcher.group(EVENT_NAME);

        if (nestedComponentId == null && eventType == null)
            return null;

        String activePageName = matcher.group(LOGICAL_PAGE_NAME);

        int slashx = activePageName.indexOf('/');

        String possibleLocaleName = slashx > 0 ? activePageName.substring(0, slashx) : "";

        if (localizationSetter.setLocaleFromLocaleName(possibleLocaleName))
            activePageName = activePageName.substring(slashx + 1);

        if (!componentClassResolver.isPageName(activePageName))
            return null;

        activePageName = componentClassResolver.canonicalizePageName(activePageName);

        EventContext eventContext = contextPathEncoder.decodePath(matcher.group(CONTEXT));

        EventContext activationContext = contextPathEncoder.decodePath(request
                .getParameter(InternalConstants.PAGE_CONTEXT_NAME));

        // The event type is often omitted, and defaults to "action".

        if (eventType == null)
            eventType = EventConstants.ACTION;

        if (nestedComponentId == null)
            nestedComponentId = "";

        String containingPageName = request.getParameter(InternalConstants.CONTAINER_PAGE_NAME);

        if (containingPageName == null)
            containingPageName = activePageName;
        else
            containingPageName = componentClassResolver.canonicalizePageName(containingPageName);

        return new ComponentEventRequestParameters(activePageName, containingPageName, nestedComponentId, eventType,
                activationContext, eventContext);
    }

    public PageRenderRequestParameters decodePageRenderRequest(Request request)
    {
        // The extended name may include a page activation context. The trick is
        // to figure out where the logical page name stops and where the
        // activation context begins. Further, strip out the leading slash.

        String path = request.getPath();

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
        }

        slashx = extendedName.length();
        boolean atEnd = true;

        while (slashx > 0)
        {
            String pageName = extendedName.substring(0, slashx);
            String pageActivationContext = atEnd ? "" : extendedName.substring(slashx + 1);

            PageRenderRequestParameters parameters = checkIfPage(request, pageName, pageActivationContext);

            if (parameters != null)
                return parameters;

            // Work backwards, splitting at the next slash.
            slashx = extendedName.lastIndexOf('/', slashx - 1);

            atEnd = false;
        }

        // OK, maybe its all page activation context for the root Index page.

        return checkIfPage(request, "", extendedName);
    }

    private PageRenderRequestParameters checkIfPage(Request request, String pageName, String pageActivationContext)
    {
        if (!componentClassResolver.isPageName(pageName))
            return null;

        EventContext activationContext = contextPathEncoder.decodePath(pageActivationContext);

        String canonicalized = componentClassResolver.canonicalizePageName(pageName);

        boolean loopback = request.getParameter(TapestryConstants.PAGE_LOOPBACK_PARAMETER_NAME) != null;

        return new PageRenderRequestParameters(canonicalized, activationContext, loopback);
    }

    public void appendContext(boolean seperatorRequired, EventContext context, StringBuilder builder)
    {
        String encoded = contextPathEncoder.encodeIntoPath(context);

        if (encoded.length() > 0)
        {
            if (seperatorRequired)
                builder.append(SLASH);

            builder.append(encoded);
        }
    }
}
