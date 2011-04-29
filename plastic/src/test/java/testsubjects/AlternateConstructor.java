package testsubjects;

public class AlternateConstructor
{
    private String value;

    public AlternateConstructor()
    {
    }

    public AlternateConstructor(String initialValue)
    {
        value = initialValue;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

}
