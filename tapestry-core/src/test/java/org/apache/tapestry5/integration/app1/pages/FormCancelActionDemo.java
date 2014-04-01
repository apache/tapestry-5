package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.services.FormSupport;

import java.util.ArrayList;
import java.util.List;

public class FormCancelActionDemo
{
    @Property
    @Persist
    private List<String> messages;

    static class AddMessage implements ComponentAction<FormCancelActionDemo>
    {
        public void execute(FormCancelActionDemo component)
        {
            component.addMessage("action trigger");
        }
    }

    private void addMessage(String s)
    {
        if (messages == null)
        {
            messages = new ArrayList<String>();
        }

        messages.add(s);
    }

    @Environmental
    private FormSupport formSupport;

    void onBeginRenderFromForm()
    {
        formSupport.storeCancel(this, new AddMessage());
    }

    void onCanceledFromForm()
    {
        addMessage("cancel event");
    }
}
