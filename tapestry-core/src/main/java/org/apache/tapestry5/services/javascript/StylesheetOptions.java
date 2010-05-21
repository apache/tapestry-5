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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.internal.TapestryInternalUtils;

/**
 * Provides options to describe options associated with importing a stylesheet onto a page.
 * 
 * @since 5.2.0
 */
public class StylesheetOptions
{
    private final String media, condition;

    public StylesheetOptions(String media)
    {
        this(media, null);
    }

    public StylesheetOptions(String media, String condition)
    {
        this.media = media;
        this.condition = condition;
    }

    /**
     * The media associated with this stylesheet, i.e., "print". Becomes the media attribute
     * of the &lt;link&gt; tag. May be null.
     */
    public String getMedia()
    {
        return media;
    }

    /**
     * The Internet Explorer condition associated with the link. When non-blank, the
     * &lt;link&gt; element will be written inside a specially formatted comment interpreted
     * by Internet Explorer. Usually null.
     * 
     * @see http://en.wikipedia.org/wiki/Conditional_comment
     */
    public String getCondition()
    {
        return condition;
    }

    @Override
    public String toString()
    {
        return String.format("StylesheetOptions[media=%s condition=%s]", media);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(obj instanceof StylesheetOptions))
            return false;

        StylesheetOptions sso = (StylesheetOptions) obj;

        return TapestryInternalUtils.isEqual(media, sso.media)
                && TapestryInternalUtils.isEqual(condition, sso.condition);
    }
}
