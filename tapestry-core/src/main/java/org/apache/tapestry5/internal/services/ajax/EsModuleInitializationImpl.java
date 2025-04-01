package org.apache.tapestry5.internal.services.ajax;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.services.javascript.EsModuleInitialization;
import org.apache.tapestry5.services.javascript.ImportPlacement;

public class EsModuleInitializationImpl extends BaseInitialization<EsModuleInitialization> implements EsModuleInitialization
{
    
    private Map<String, String> attributes;
    private ImportPlacement placement = ImportPlacement.BODY_BOTTOM;
    private Object[] arguments;
    
    EsModuleInitializationImpl(String moduleName) 
    {
        super(moduleName);
    }

    public EsModuleInitialization withAttribute(String id, String value) 
    {
        if (attributes == null)
        {
            attributes = CollectionFactory.newMap();
        }
        attributes.put(id, value);
        return this;
    }

    public EsModuleInitialization placement(ImportPlacement placement) 
    {
        this.placement = placement;
        return null;
    }

    public String getModuleId() {
        return moduleName;
    }

    public Map<String, String> getAttributes() {
        return attributes != null ? 
                Collections.unmodifiableMap(attributes) : 
                    Collections.emptyMap();
    }

    public ImportPlacement getPlacement() {
        return placement;
    }
    
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public void with(Object... arguments) 
    {
        this.arguments = arguments;
    }
    
    public Object[] getArguments() 
    {
        return arguments;
    }
    
}