package org.apache.tapestry;

/**
 * Meta-data keys that are applied to components and pages.
 *
 * @see org.apache.tapestry.services.MetaDataLocator
 */
public class MetaDataConstants
{
    /**
     * Meta data key applied to pages that sets the response content type. A factory default provides the value
     * "text/html" when not overridden.
     */
    public static final String RESPONSE_CONTENT_TYPE = "tapestry.response-content-type";
    /**
     * Meta data key applied to pages that may only be accessed via secure methods (HTTPS).
     */
    public static final String SECURE_PAGE = "tapestry.secure-page";
    /**
     * Meta data key applied to pages that sets the response encoding. A factory default provides the value "UTF-8" when
     * not overriden. Content type may also be specified in the {@link #RESPONSE_CONTENT_TYPE content type} as parameter
     * "charset", i.e., "text/html;charset=UTF-8".
     */
    public static final String RESPONSE_ENCODING = "tapestry.response-encoding";
}
