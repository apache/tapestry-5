package org.apache.tapestry5.integration.app1.pages;

/**
 *
 */
public class EventMethodUnmatchedComponentId
{

    /** Baz is not a component, this should be an error as of TAP5-1596. */
    void onActionFromBaz()
    {
    }
}
