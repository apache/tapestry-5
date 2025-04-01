package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.javascript.AbstractInitialization;

abstract class BaseInitialization<T extends AbstractInitialization<?>> implements AbstractInitialization<T>
{
    final String moduleName;

    protected String functionName;

    BaseInitialization(String moduleName)
    {
        this.moduleName = moduleName;
    }

    @SuppressWarnings("unchecked")
    public T invoke(String functionName)
    {
        assert InternalUtils.isNonBlank(functionName);

        this.functionName = functionName;

        return (T) this;
    }
    
}