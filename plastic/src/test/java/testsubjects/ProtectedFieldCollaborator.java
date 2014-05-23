package testsubjects;

import testannotations.KindaInject;
import testinterfaces.ValueGetter;

public class ProtectedFieldCollaborator
{
    @KindaInject
    ProtectedField delegate;

    public String getProtectedValue()
    {
        return delegate.protectedValue;
    }

    public void setProtectedValue(String newValue)
    {
        delegate.protectedValue = newValue;
    }

    public ValueGetter getValueGetter()
    {
        return new ValueGetter()
        {
            @Override
            public String getValue()
            {
                return delegate.protectedValue;
            }
        };
    }
}
