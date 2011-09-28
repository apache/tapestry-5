package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.corelib.components.TextField;

public class MissingEmbeddedComponent
{
    @InjectComponent
    private TextField missing;
}
