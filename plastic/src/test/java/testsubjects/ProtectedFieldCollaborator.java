package testsubjects;

public class ProtectedFieldCollaborator
{
    private ProtectedField delegate;

    public String getProtectedValue()
    {
        return delegate.protectedValue;
    }

    public void setProtectedValue(String newValue)
    {
        delegate.protectedValue = newValue;
    }
}
