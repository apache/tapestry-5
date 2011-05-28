package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.ioc.services.ThreadLocale;

/**
 * Determines the {@link ComponentResourceSelector} for the current request. This is often based on cookies, query
 * parameters, or other details available in the {@link Request}. The default implementation simply wraps the
 * {@linkplain ThreadLocale current locale} as a ComponentResourceSelector. A custom implementation may
 * {@linkplain ComponentResourceSelector#withAxis(Class, Object) add additional axes} to the selector.
 * 
 * @since 5.3.0
 */
public interface ComponentRequestSelectorAnalyzer
{
    /**
     * Constructs a selector for locating or loading pages in the current request.
     */
    ComponentResourceSelector buildSelectorForRequest();
}
