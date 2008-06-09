package org.apache.tapestry5.services;

import org.apache.tapestry5.dom.Element;

/**
 * Provides some assistance in determining <em>where</em> to place a hidden field based on standard (X)HTML elements.
 * <p/>
 * <p/>
 * The service works based on a mapped service contribution; keys are the element names, values area {@link
 * org.apache.tapestry5.services.RelativeElementPosition}.
 */
public interface HiddenFieldLocationRules
{
    /**
     * Checks the element to see if a hidden field may be placed inside the element.
     */
    boolean placeHiddenFieldInside(Element element);

    /**
     * Checks the element to see if a hidden field may be placed after the element (as a sibling element).
     */
    boolean placeHiddenFieldAfter(Element element);
}
