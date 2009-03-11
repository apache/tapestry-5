package org.apache.tapestry5.services;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

/**
 * Class that wraps an {@linkplain Request}, delegating all its methods. 
 * 
 * @since 5.1.0.1
 */
public class DelegatingRequest implements Request
{

    private Request request;

    /**
     * No-arg constructor. It should only be used for testing purposes.
     */
    public DelegatingRequest() {
    }
    
    /**
     * Constructor that receives a {@linkplain Request}.
     * 
     * @param request a {@link Request}. It cannot be null.
     */
    public DelegatingRequest(Request request)
    {
        setRequest(request);
    }

    /**
     * Sets the delegate request.
     * @param request a {@link Request}. It cannot be null.
     */
    public void setRequest(Request request)
    {
        Defense.notNull(request, "request");
        this.request = request;
    }

    public Object getAttribute(String name)
    {
        return request.getAttribute(name);
    }

    public String getContextPath()
    {
        return request.getContextPath();
    }

    public long getDateHeader(String name)
    {
        return request.getDateHeader(name);
    }

    public String getHeader(String name)
    {
        return request.getHeader(name);
    }

    public List<String> getHeaderNames()
    {
        return request.getHeaderNames();
    }

    public Locale getLocale()
    {
        return request.getLocale();
    }

    public String getMethod()
    {
        return request.getMethod();
    }

    public String getParameter(String name)
    {
        return request.getParameter(name);
    }

    public List<String> getParameterNames()
    {
        return request.getParameterNames();
    }

    public String[] getParameters(String name)
    {
        return request.getParameters(name);
    }

    public String getPath()
    {
        return request.getPath();
    }

    public String getServerName()
    {
        return request.getServerName();
    }

    public Session getSession(boolean create)
    {
        return request.getSession(create);
    }

    public boolean isRequestedSessionIdValid()
    {
        return request.isRequestedSessionIdValid();
    }

    public boolean isSecure()
    {
        return request.isSecure();
    }

    public boolean isXHR()
    {
        return request.isXHR();
    }

    public void setAttribute(String name, Object value)
    {
        request.setAttribute(name, value);
    }

}