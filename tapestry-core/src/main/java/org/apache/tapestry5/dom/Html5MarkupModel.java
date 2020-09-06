// Copyright 2013 The Apache Software Foundation
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

import java.util.Set;

import org.apache.tapestry5.commons.util.CollectionFactory;

/**
 * Implementation of {@link org.apache.tapestry5.dom.MarkupModel} that correctly handles HTML5 void
 * elements. It does not support XHTML5.
 */
public class Html5MarkupModel extends AbstractMarkupModel
{
    // http://www.w3.org/TR/html5/syntax.html#void-elements
    private final Set<String> VOID_ELEMENTS = CollectionFactory.newSet("area", "base", "br", "col",
            "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source",
            "track", "wbr");
    
    public Html5MarkupModel()
    {
        super(false);
    }

    public Html5MarkupModel(boolean useApostropheForAttributes)
    {
        super(useApostropheForAttributes);
    }

    public EndTagStyle getEndTagStyle(String element)
    {
        return VOID_ELEMENTS.contains(element) ? EndTagStyle.VOID : EndTagStyle.REQUIRE;
    }

    public boolean isXML()
    {
        return false;
    }

}
