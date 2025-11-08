package org.apache.tapestry5.integration.app1.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.integration.app1.GenericsClass;

public abstract class AbstractCachedGenerics<T, H> 
{
    
    protected abstract GenericsClass<T, H> createTable(List<T> itemsInCurrentPage);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Cached
    public GenericsClass<T, H> getEmptyTable() 
    {
        final String timestamp = String.valueOf(System.currentTimeMillis());
//        System.out.println("XXXXX " + timestamp);
        return createTable(new ArrayList(Arrays.asList("value1", "value2", 
                timestamp)));
    }
    
}
