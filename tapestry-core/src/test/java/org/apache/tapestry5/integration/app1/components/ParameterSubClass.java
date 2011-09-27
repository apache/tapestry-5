package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.integration.app1.base.ParameterBaseClass;

public class ParameterSubClass extends ParameterBaseClass
{
    // This conflicts with value parameter defined in ParameterBaseClass
    @Parameter
    private String value;
}
