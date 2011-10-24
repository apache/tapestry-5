package org.apache.tapestry5.integration.app1.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Parameter;

@MixinAfter
public class AltTitleDefault
{
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL, allowNull = false, value = "Default title")
    private String title;

    void beginRender(MarkupWriter writer)
    {
        writer.attributes("alt", title);
    }
}
