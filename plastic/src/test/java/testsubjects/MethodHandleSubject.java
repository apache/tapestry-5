package testsubjects;

public class MethodHandleSubject
{
    private String message;

    public String getMessage()
    {
        return message;
    }

    void voidPrimitiveParameters(boolean boolValue, long longValue, int intValue)
    {
        message = String.format("bool: %s, long: %d, int: %d", boolValue, longValue, intValue);
    }

    int mayThrowException(int value, boolean doThrow) throws WillNotDoubleException, RuntimeException
    {
        if (doThrow)
            throw new WillNotDoubleException();

        return value * 2;
    }

    @SuppressWarnings("unused")
    private String wrapString(String input, char prefix, char suffix)
    {
        return prefix + input + suffix;
    }

    @SuppressWarnings("unused")
    private void forceMessage(String message)
    {
        this.message = message;
    }
}
