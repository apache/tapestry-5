package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.apache.tapestry5.services.RelativeElementPosition;

import java.util.Map;

public class HiddenFieldLocationRulesImpl implements HiddenFieldLocationRules
{
    private final Map<String, RelativeElementPosition> configuration;

    public HiddenFieldLocationRulesImpl(Map<String, RelativeElementPosition> configuration)
    {
        this.configuration = configuration;
    }

    private boolean match(Element element, RelativeElementPosition position)
    {
        Defense.notNull(element, "element");

        String key = element.getName();

        RelativeElementPosition actual = configuration.get(key);

        if (actual == null) return false;

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
