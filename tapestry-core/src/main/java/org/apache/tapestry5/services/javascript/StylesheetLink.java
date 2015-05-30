// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Captures the information needed to create a stylesheet link in the final {@link Document}, or
 * as part of a JSON partial page render response.
 *
 * @see DocumentLinker
 * @see JavaScriptStack
 * @since 5.2.0
 */
public final class StylesheetLink
{
    private final Asset asset;

    private final String url;

    private final StylesheetOptions options;

    private static final StylesheetOptions BLANK_OPTIONS = new StylesheetOptions(null);

    public StylesheetLink(Asset asset)
    {
        this(asset, null, null);
    }

    public StylesheetLink(Asset asset, StylesheetOptions options)
    {
        this(asset, null, options);
    }

    public StylesheetLink(String url)
    {
        this(null, url, null);
    }

    public StylesheetLink(String url, StylesheetOptions options)
    {
        this(null, url, options);
    }

    private StylesheetLink(Asset asset, String url, StylesheetOptions options)
    {
        assert asset != null || InternalUtils.isNonBlank(url);

        this.asset = asset;
        this.url = url;
        this.options = options != null ? options : BLANK_OPTIONS;
    }

    public String getURL()
    {
        // Only one of asset or url will be non-null.
        // Starting in 5.4, we keep the asset around and ask it for its clientURL; this is because
        // clientURLs can change if the content of the underlying Resource changes.
        return asset != null ? asset.toClientURL() : url;
    }

    /**
     * Returns an instance of {@link StylesheetOptions}. Never returns null (a blank options
     * object is returned if null is passed to the constructor).
     */
    public StylesheetOptions getOptions()
    {
        return options;
    }

    /**
     * Invoked to add the stylesheet link to a container element.
     *
     * @param container
     *         to add the new element to
     */
    public void add(Element container)
    {
        boolean hasCondition = InternalUtils.isNonBlank(options.condition);

        if (hasCondition)
        {
            container.raw(String.format("\n<!--[if %s]>\n", options.condition));
        }

        String rel = options.ajaxInsertionPoint ? "stylesheet ajax-insertion-point" : "stylesheet";

        container.element("link",
                "href", getURL(),
                "rel", rel,
                "type", "text/css",
                "media", options.media);

        if (hasCondition)
        {
            container.raw("\n<![endif]-->\n");
        }
    }

    @Override
    public String toString()
    {
        return String.format("StylesheetLink[%s %s]", url, options);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(obj instanceof StylesheetLink))
            return false;

        StylesheetLink ssl = (StylesheetLink) obj;

        return TapestryInternalUtils.isEqual(getURL(), ssl.getURL()) && TapestryInternalUtils.isEqual(options, ssl.options);
    }

}
