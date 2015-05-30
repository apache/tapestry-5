package testsubjects;

import testannotations.Property;

public class AccessorsAlreadyExistSubject2
{
    @Property
    private int value;

    public String getValue()
    {
        return String.valueOf(value);
    }

}
