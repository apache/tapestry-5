package testsubjects;

import org.apache.tapestry5.plastic.test.IndirectAccess;

public class AccessMethodsSubject
{
    private String value;

    public IndirectAccess<String> getValueAccess()
    {
        return new IndirectAccess<String>()
        {
            public String get()
            {
                return value;
            }

            public void set(String newValue)
            {
                value = newValue;
            }

        };
    }
}
