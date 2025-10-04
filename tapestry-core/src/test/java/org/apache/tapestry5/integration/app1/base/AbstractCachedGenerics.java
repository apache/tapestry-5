package org.apache.tapestry5.integration.app1.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.integration.app1.GenericsClass;

public abstract class AbstractCachedGenerics<T, H> 
{

    protected abstract GenericsClass<T, H> createTable(List<T> itemsInCurrentPage);

    @Cached
    public GenericsClass<T, H> getEmptyTable() 
    {
        return createTable(new ArrayList<>());
    }
    
}
