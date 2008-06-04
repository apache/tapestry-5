package org.apache.tapestry5.services;

/**
 * Used by {@link org.apache.tapestry5.services.HiddenFieldLocationRules} to identify where a hidden field may be placed
 * relative to a particular element.
 */
public enum RelativeElementPosition
{
    /**
     * The hidden field may be placed inside the element, as a child.
     */
    INSIDE,

    /**
     * The hidden field may be placed after the element, as a sibling.
     */
    AFTER;
}
