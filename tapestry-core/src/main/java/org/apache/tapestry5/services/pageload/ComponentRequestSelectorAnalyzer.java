package org.apache.tapestry5.services.pageload;

import java.util.Locale;

/**
 * Determines the {@link ComponentResourceSelector} for the current request. This is often based on cookies, query
 * parameters, or other details available in the {@link Request}. The default implementation simply wraps the
 * provided Locale as a ComponentResourceSelector. A custom implementation may
 * {@linkplain ComponentResourceSelector#withAxis(Class, Object) add additional axes} to the selector.
 * 
 * @since 5.3.0
 */
public interface ComponentRequestSelectorAnalyzer
{
    /**
     * Constructors a selector given the locale.
     * 
     * @param locale
     * @return
     */
    ComponentResourceSelector buildSelector(Locale locale);
}
