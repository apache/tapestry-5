// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.dom;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Set;

/**
 * Default implementation of {@link org.apache.tapestry5.dom.MarkupModel} that is appropriate for traditional (X)HTML
 * markup. Assumes that all tags are lower-case.  The majority of elements will be "expanded" (meaning a complete start
 * and end tag); this is for compatibility with web browsers, especially when the content type of a response indicates
 * HTML, not true XML. Only the "hr" and "br" and "img" tags will be rendered abbreviated (i.e., "lt;img/&gt;").
 */
public class DefaultMarkupModel extends AbstractMarkupModel
{
    private final Set<String> ALWAYS_EMPTY = CollectionFactory.newSet("hr", "br", "img");

    public DefaultMarkupModel()
    {
        this(false);
    }

    public DefaultMarkupModel(boolean useApostropheForAttributes)
    {
        super(useApostropheForAttributes);
    }

    public EndTagStyle getEndTagStyle(String element)
    {
        boolean alwaysEmpty = ALWAYS_EMPTY.contains(element);

        return alwaysEmpty ? EndTagStyle.ABBREVIATE : EndTagStyle.REQUIRE;
    }

    /**
     * Returns false.
     */
    public boolean isXML()
    {
        return false;
    }
}
