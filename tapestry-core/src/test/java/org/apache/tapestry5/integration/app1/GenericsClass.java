package org.apache.tapestry5.integration.app1;

import java.util.List;

public class GenericsClass<T, H> 
{

    final private List<T> list;

    public GenericsClass(List<T> list) 
    {
        super();
        this.list = list;
    }

    @Override
    public String toString() 
    {
        return "GenericsClass [list=" + list + "]";
    }
    
}
