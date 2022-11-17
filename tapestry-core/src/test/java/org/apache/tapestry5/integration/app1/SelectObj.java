package org.apache.tapestry5.integration.app1;

public class SelectObj
{
    final int id;
    final String label;

    public SelectObj(int id, String label)
    {
        this.id = id;
        this.label = label;
    }

    public int getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }
}