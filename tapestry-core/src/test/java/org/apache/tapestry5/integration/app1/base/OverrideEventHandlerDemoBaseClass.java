package org.apache.tapestry5.integration.app1.base;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.util.CollectionFactory;

import java.util.List;

public abstract class OverrideEventHandlerDemoBaseClass
{
    @Persist
    @Property
    private List<String> methodNames;

    protected void add(String methodName)
    {
        if (methodNames == null)
        {
            methodNames = CollectionFactory.newList();
        }
        
        methodNames.add(methodName);
    }

    public abstract Object onActionFromTrigger();
}
