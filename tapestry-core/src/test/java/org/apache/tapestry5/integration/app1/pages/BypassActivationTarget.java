package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Property;

public class BypassActivationTarget
{
    @Property
    private boolean activated;

    void onActivate()
    {
        activated = true;
    }
}
