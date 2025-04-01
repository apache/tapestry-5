package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.Initialization;
import org.apache.tapestry5.services.javascript.InitializationPriority;

class InitializationImpl extends BaseInitialization<Initialization> implements Initialization
{
    
    JSONArray arguments;
    
    InitializationPriority priority = InitializationPriority.NORMAL;

    public InitializationImpl(String moduleName) 
    {
        super(moduleName);
    }
    
    public Initialization priority(InitializationPriority priority)
    {
        assert priority != null;

        this.priority = priority;

        return this;
    }
    
    @Override
    public void with(Object... arguments)
    {
        assert arguments != null;

        this.arguments = new JSONArray(arguments);
    }

    
}