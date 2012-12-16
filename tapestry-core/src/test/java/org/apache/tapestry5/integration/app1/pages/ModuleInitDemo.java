package org.apache.tapestry5.integration.app1.pages;


import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class ModuleInitDemo
{
    @Environmental
    private JavaScriptSupport javaScriptSupport;

    void afterRender()
    {
        javaScriptSupport.require("app/alert").invoke("alert").with("Module Initialization Demo");
    }
}
