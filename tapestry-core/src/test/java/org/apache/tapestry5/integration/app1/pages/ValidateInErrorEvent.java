package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.services.ajax.RequireJsModeHelper;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ValidateInErrorEvent {
    @Property
    private String value;
    
    @Inject
    private RequireJsModeHelper requireJsModeHelper;
    
    void beginRender() {
        requireJsModeHelper.importModule("validate-in-error");
    }
    
}
