package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Cached;

import java.util.ArrayList;
import java.util.List;

public class CachedPage
{
    private int value;
    private List<String> value2;
    private int value3;

    private Integer watchValue;

    void beginRender()
    {
        value = 0;
        value2 = new ArrayList<String>();
        value3 = 0;
        watchValue = 0;
    }

    @Cached
    public int getValue()
    {
        return value++;
    }

    @Cached
    public List<String> getValue2()
    {
        value2.add("a");
        return value2;
    }

    @Cached(watch = "watchValue")
    public int getValue3()
    {
        return value3++;
    }

    public Integer getWatchValue()
    {
        return watchValue;
    }

    public void setWatchValue(Integer watchValue)
    {
        this.watchValue = watchValue;
    }

    public Object incrWatchValue()
    {
        watchValue++;
        return null;
    }
}
