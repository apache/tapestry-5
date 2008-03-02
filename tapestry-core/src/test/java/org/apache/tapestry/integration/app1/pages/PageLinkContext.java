// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.annotations.Inject;

public class PageLinkContext
{
    @Inject
    private ComponentResources _resources;

    public Object[] getComputedContext()
    {
        return new Object[] { "fred", 7, true };
    }

    public String getUnsafeCharacters()
    {
        return "unsafe characters: !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    }

    public String getJapaneseKanji()
    {
        return "japanese kanji: \u65E5\u672C\u8A9E";
    }

    Object onActionFromNullContext()
    {
        return _resources.createPageLink("target", true, new Object[] { null });
    }

}
