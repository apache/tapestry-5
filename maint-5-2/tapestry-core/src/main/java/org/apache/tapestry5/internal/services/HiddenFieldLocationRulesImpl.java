// Copyright 2008, 2010 The Apache Software Foundation
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

import java.util.Map;

import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.apache.tapestry5.services.RelativeElementPosition;

public class HiddenFieldLocationRulesImpl implements HiddenFieldLocationRules
{
    private final Map<String, RelativeElementPosition> configuration;

    public HiddenFieldLocationRulesImpl(Map<String, RelativeElementPosition> configuration)
    {
        this.configuration = configuration;
    }

    private boolean match(Element element, RelativeElementPosition position)
    {
        assert element != null;
        String key = element.getName();

        RelativeElementPosition actual = configuration.get(key);

        if (actual == null)
            return false;

        return actual == position;
    }

    public boolean placeHiddenFieldInside(Element element)
    {
        return match(element, RelativeElementPosition.INSIDE);
    }

    public boolean placeHiddenFieldAfter(Element element)
    {
        return match(element, RelativeElementPosition.AFTER);
    }
}
