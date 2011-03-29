package testsubjects;

import java.sql.SQLException;

public class MethodAdviceTarget
{
    public String dupe(int count, String value)
    {
        StringBuilder b = new StringBuilder();
        String sep = "";

        for (int i = 0; i < count; i++)
        {
            b.append(sep).append(value);
            sep = " ";
        }

        return b.toString();
    }

    public long maybeThrow(long value) throws SQLException
    {
        if (value != 0)
            return value;

        throw new SQLException();
    }
}
