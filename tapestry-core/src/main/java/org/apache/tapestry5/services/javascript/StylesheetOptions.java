// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.internal.TapestryInternalUtils;

/**
 * Provides options to describe options associated with importing a stylesheet onto a page.  Stylesheet options
 * are immutable.
 *
 * @since 5.2.0
 */
public final class StylesheetOptions
{
    /**
     * The media associated with this stylesheet, i.e., "print". Becomes the media attribute
     * of the &lt;link&gt; tag. May be null.
     */
    public final String media;

    /**
     * The Internet Explorer condition associated with the link. When non-blank, the
     * &lt;link&gt; element will be written inside a specially formatted comment interpreted
     * by Internet Explorer. Usually null, and only used for full page renders (not partial page renders).
     *
     * @see <a href="http://en.wikipedia.org/wiki/Conditional_comment">http://en.wikipedia.org/wiki/Conditional_comment</a>
     */
    public final String condition;

    /**
     * If true, then this stylesheet is the insertion point for Ajax operations; any added CSS links will be inserted before this link. Only at most
     * one CSS link should be the insertion point.     Only used for full page renders (not partial page renders). When this is true, the {@code <link>} element's
     * ref attribute is wrtten out as "stylesheet ajax-insertion-point".
     */
    public boolean ajaxInsertionPoint;

    /**
     * Returns a new options object with media as null (that is, unspecified), no condition, and not the Ajax insertion point.
     */
    public StylesheetOptions()
    {
        this(null);
    }

    public StylesheetOptions(String media)
    {
        this(media, null);
    }

    /**
     * @deprecated In 5.3, may be removed in a later release. Use {@link #StylesheetOptions(String)} and {@link #withCondition(String)}} instead.
     */
    public StylesheetOptions(String media, String condition)
    {
        this(media, condition, false);
    }

    private StylesheetOptions(String media, String condition, boolean ajaxInsertionPoint)
    {
        this.media = media;
        this.condition = condition;
        this.ajaxInsertionPoint = ajaxInsertionPoint;
    }

    /**
     * Returns a new options object with the indicated {@linkplain #condition Internet Explorer condition}. @since 5.3
     */
    public StylesheetOptions withCondition(String condition)
    {
        return new StylesheetOptions(media, condition, ajaxInsertionPoint);
    }

    /**
     * Returns a new options object with the {@link #ajaxInsertionPoint} flag set to true.
     */
    public StylesheetOptions asAjaxInsertionPoint()
    {
        return new StylesheetOptions(media, condition, true);
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("StyleSheetOptions[");

        String sep = "";

        if (media != null)
        {
            builder.append("media=").append(media);
            sep = " ";
        }

        if (condition != null)
        {
            builder.append(sep).append("condition=").append(condition);
            sep = " ";
        }

        if (ajaxInsertionPoint)
        {
            builder.append(sep).append("ajaxInsertionPoint=true");
            sep = " ";
        }

        return builder.append(']').toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(obj instanceof StylesheetOptions))
            return false;

        StylesheetOptions sso = (StylesheetOptions) obj;

        return ajaxInsertionPoint == sso.ajaxInsertionPoint && TapestryInternalUtils.isEqual(media, sso.media)
                && TapestryInternalUtils.isEqual(condition, sso.condition);
    }
}
