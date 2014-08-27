package org.apache.tapestry5.integration.app1;

import java.io.Serializable;

public class ClientDataWrapper implements Serializable
{
    private String value;


    public ClientDataWrapper(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.format("ClientDataWrapper[%s]", value);
    }
}
