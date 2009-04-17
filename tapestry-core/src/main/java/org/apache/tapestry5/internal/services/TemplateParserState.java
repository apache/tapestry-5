// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * Used to track behaviors inside {@link org.apache.tapestry5.internal.services.StaxTemplateParser}. Internal state is
 * immutable, the set-like methods return a new immutable instance.
 *
 * @since 5.1.0.0
 */
class TemplateParserState
{
    private final boolean compressWhitespace;

    private final boolean collectingContent;

    private final boolean insideComponent;

    TemplateParserState()
    {
        compressWhitespace = false;
        collectingContent = false;
        insideComponent = false;
    }

    private TemplateParserState(boolean compressWhitespace, boolean collectingContent, boolean insideComponent)
    {
        this.compressWhitespace = compressWhitespace;
        this.collectingContent = collectingContent;
        this.insideComponent = insideComponent;
    }

    TemplateParserState compressWhitespace(boolean flag)
    {
        return flag == compressWhitespace ? this : new TemplateParserState(flag, collectingContent, insideComponent);
    }

    TemplateParserState collectingContent()
    {
        return collectingContent ? this : new TemplateParserState(compressWhitespace, true, insideComponent);
    }

    TemplateParserState insideComponent(boolean flag)
    {
        return flag == insideComponent ? this : new TemplateParserState(compressWhitespace, collectingContent, flag);
    }

    boolean isInsideComponent()
    {
        return insideComponent;
    }

    boolean isCompressWhitespace()
    {
        return compressWhitespace;
    }

    /**
     * Is content being collected, inside a &lt;t:content&gt; element?
     */
    public boolean isCollectingContent()
    {
        return collectingContent;
    }

    @Override
    public String toString()
    {
        return String.format("TemplateParserState[compressWhitespace=%s, collectingContent=%s, insideComponent=%s]",
                             compressWhitespace, collectingContent, insideComponent);
    }
}
