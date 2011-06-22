package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Checkbox;
import org.apache.tapestry5.corelib.components.FormFragment;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class NestedFormFragment 
{

    @InjectComponent
    private FormFragment innerremove;

    @InjectComponent
    private Checkbox innertrigger2;

    @Inject
    private JavaScriptSupport jsSupport;

    void afterRender() 
    {
        jsSupport.addScript("$('%s').observe('click', function() { $('%s').fire(Tapestry.HIDE_AND_REMOVE_EVENT); });", innertrigger2.getClientId(), innerremove.getClientId()); 
    }
}
