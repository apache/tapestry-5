package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Collections;
import java.util.List;

public class ChecklistDemo
{
    @Property
    @Persist
    private List<String> selected;

    public ValueEncoder getEncoder()
    {
        return new StringValueEncoder();
    }

    public List<String> getSorted()
    {
        if(selected == null)
            return null;

        final List<String> result = CollectionFactory.newList(selected);

        Collections.sort(result);

        return result;
    }
}
