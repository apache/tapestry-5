package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.MarkupWriter;

public class TriggerDemo
{
    public void onProvideAdditionalMarkup(MarkupWriter writer)
    {
        writer.writeRaw("Event 'provideAdditionalMarkup' handled.");
    }

}
