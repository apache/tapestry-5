package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Person;

public class BeanEditFormPrepareBubbling
{
    @Property
    @Persist
    private Person person;

    private boolean eventHandled;

    void onPrepare()
    {
        if(eventHandled)
            throw new IllegalStateException("Illegal event handler invocation. The 'prepare' event has been already handled");

        eventHandled = true;
    }
}
