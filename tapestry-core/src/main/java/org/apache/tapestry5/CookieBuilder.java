package org.apache.tapestry5;

import org.apache.tapestry5.services.Request;

/**
 * A fluent API to create and write cookies. Used by the
 * {@link org.apache.tapestry5.services.Cookies} service.
 * 
 * @since 5.4
 */
public abstract class CookieBuilder
{
    
    protected final String name;
    protected final String value;
    
    protected String path;
    protected String domain;
    protected Integer maxAge;
    protected Boolean secure;
    
    /**
     * Initialize a new CookieBuilder
     * 
     * @param name  the name of the resulting cookie
     * @param value the value of the resulting cookie
     */
    protected CookieBuilder(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Set the path for the cookie to be created. Defaults to {@link Request#getContextPath()}.
     * @param  path the path for the cookie
     * @return the modified {@link CookieBuilder}
     */
    public CookieBuilder setPath(String path)
    {
        this.path = path;
        return this;
    }

    /**
     * Set the domain for the cookie to be created. Will not be set by default.
     * @param  domain the domain for the cookie
     * @return the modified {@link CookieBuilder}
     */
    public CookieBuilder setDomain(String domain)
    {
        this.domain = domain;
        return this;
    }

    /**
     * Set how long the cookie should live. A value of <code>0</code> deletes a cookie, a value of
     * <code>-1</code> deletes a cookie upon closing the browser. The default is defined by
     * the symbol <code>org.apache.tapestry5.default-cookie-max-age</code>. The factory default for
     * this value is the equivalent of one week.
     * 
     * @param maxAge
     *            the cookie's maximum age in seconds
     * @return the modified {@link CookieBuilder}
     */
    public CookieBuilder setMaxAge(int maxAge)
    {
        this.maxAge = maxAge;
        return this;
    }
    
    /**
     * Set the cookie's secure mode. Defaults to {@link Request#isSecure()}.
     * 
     * @param secure whether to send the cookie over a secure channel only
     * @return the modified {@link CookieBuilder}
     */
    public CookieBuilder setSecure(boolean secure)
    {
        this.secure = secure;
        return this;
    }
    
    /**
     * Sets defaults and writes the cookie to the client.
     */
    public abstract void write();
    
    /**
     * Deletes the cookie.
     */
    public abstract void delete();
    
}
