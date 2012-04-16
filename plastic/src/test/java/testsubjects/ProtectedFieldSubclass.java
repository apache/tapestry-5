package testsubjects;

public class ProtectedFieldSubclass extends ProtectedField
{
    public String getValue()
    {
        return protectedValue;
    }

    public void setValue(String value)
    {
        protectedValue = value;
    }

}
