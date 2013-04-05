package org.apache.tapestry5.internal.t5internal.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

/**
 * A default layout for a number of internal pages in Tapestry, such as {@link org.apache.tapestry5.internal.t5internal.pages.ServiceStatus} and {@link org.apache.tapestry5.internal.t5internal.pages.PageCatalog}.
 * <strong>This component is not intended for use in user applications, and may change at any time.</strong>
 *
 * @tapestrydoc
 * @since 5.3
 */
public class InternalLayout
{
    @Property
    @Parameter
    private Block leftNav;

    @Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title;

    @Inject
    @Symbol(SymbolConstants.TAPESTRY_VERSION)
    @Property
    private String frameworkVersion;

}
