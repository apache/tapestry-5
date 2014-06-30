package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.ActivationRequestParameter;

public class MissingRequiredARP
{
    @ActivationRequestParameter(required = true)
    private String missingARP;
}
