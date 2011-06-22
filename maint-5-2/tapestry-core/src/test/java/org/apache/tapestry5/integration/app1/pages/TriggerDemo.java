package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.Environmental;

public class TriggerDemo
{
    @Environmental
    private RenderSupport renderSupport;

    public void onProvideAdditionalMarkup(MarkupWriter writer)
    {
        writer.writeRaw("Event 'provideAdditionalMarkup' handled.");
    }

}
