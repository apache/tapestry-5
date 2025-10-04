package org.apache.tapestry5.integration.app1.components;

import java.util.List;

import org.apache.tapestry5.integration.app1.GenericsClass;
import org.apache.tapestry5.integration.app1.GenericsEntity;
import org.apache.tapestry5.integration.app1.base.AbstractCachedGenerics;

public class CachedGenerics extends AbstractCachedGenerics<GenericsEntity, String> 
{
    
    @Override
    protected GenericsClass<GenericsEntity, String> createTable(List<GenericsEntity> itemsInCurrentPage) 
    {
        return new GenericsClass<>();
    }

}
