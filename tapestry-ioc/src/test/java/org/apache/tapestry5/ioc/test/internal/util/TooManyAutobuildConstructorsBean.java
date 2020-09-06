package org.apache.tapestry5.ioc.test.internal.util;


import org.apache.tapestry5.ioc.annotations.Inject;

public class TooManyAutobuildConstructorsBean
{
    private final String foo;
    private final Runnable bar;

    public TooManyAutobuildConstructorsBean()
    {
        this(null);
    }

    @Inject
    public TooManyAutobuildConstructorsBean(String foo)
    {
        this(foo, null);
    }

    @javax.inject.Inject
    public TooManyAutobuildConstructorsBean(String foo, Runnable bar)
    {

        this.foo = foo;
        this.bar = bar;
    }

    public String getFoo()
    {
        return foo;
    }

    public Runnable getBar()
    {
        return bar;
    }
}
