// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

/**
 * Markup model used when generating any form of XML markup.
 */
public final class XMLMarkupModel extends AbstractMarkupModel
{
    public XMLMarkupModel()
    {
        this(false);
    }

    public XMLMarkupModel(boolean useApostropheForAttributes)
    {
        super(useApostropheForAttributes);
    }

    /**
     * Always returns ABBREVIATE.
     */
    public EndTagStyle getEndTagStyle(String element)
    {
        return EndTagStyle.ABBREVIATE;
    }

    /**
     * Returns true.
     */
    public boolean isXML()
    {
        return true;
    }
}
