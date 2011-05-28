package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.internal.pageload.DefaultComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.services.Core;

/**
 * @since 5.3.0
 */
@Marker(Core.class)
public class PageLoadModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ComponentRequestSelectorAnalyzer.class, DefaultComponentRequestSelectorAnalyzer.class);
    }
}
