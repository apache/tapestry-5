package org.apache.tapestry5.integration.app1.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;

/**
 * Used to test https://issues.apache.org/jira/browse/TAP5-1680.
 */
@MixinAfter
public class AltTitle
{
    @InjectContainer
    private ClientElement container;

    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title;

    void afterRender()
    {
        container.getClientId();
    }

    void beginRender(MarkupWriter writer)
    {
        writer.attributes("alt", title);
    }

}
