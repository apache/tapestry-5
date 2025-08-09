package org.apache.tapestry5.integration.app1.pages;


import org.apache.tapestry5.internal.services.ajax.RequireJsModeHelper;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ModuleInitDemo
{
    @Inject
    private RequireJsModeHelper requireJsModeHelper;

    void afterRender()
    {
        requireJsModeHelper.importModule("app/alert").invoke("alert").with("Module Initialization Demo");
    }
}
