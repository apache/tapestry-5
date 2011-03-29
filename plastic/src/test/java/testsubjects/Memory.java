package testsubjects;

public class Memory
{
    long value = 0;

    public long returnLast(long next)
    {
        long result = value;

        value = next;

        return result;
    }
}
