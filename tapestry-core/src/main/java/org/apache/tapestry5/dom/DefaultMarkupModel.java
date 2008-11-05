// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
 * markup. Assumes that all tags are lower-case.  A certain set of tags will always be expanded (with seperate begin and
 * end tags) even if their content is empty: script, div, span, p, textarea, select; this is for compatibility with web
 * browsers, especially when the content type of a response indicates HTML, not true XML.
 */
public class DefaultMarkupModel extends AbstractMarkupModel
{
    /**
     * For these tags, use {@link org.apache.tapestry5.dom.EndTagStyle#REQUIRE}.
     */
    private final Set<String> REQUIRE_END_TAG =
            CollectionFactory.newSet("script", "div", "span", "p", "textarea", "select");

    public EndTagStyle getEndTagStyle(String element)
    {
        boolean required = REQUIRE_END_TAG.contains(element);

        return required ? EndTagStyle.REQUIRE : EndTagStyle.ABBREVIATE;
    }

    /**
     * Returns false.
     */
    public boolean isXML()
    {
        return false;
    }
}
