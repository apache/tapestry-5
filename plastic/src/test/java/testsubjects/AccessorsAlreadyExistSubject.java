package testsubjects;

import testannotations.Property;

public class AccessorsAlreadyExistSubject
{
    @Property
    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
