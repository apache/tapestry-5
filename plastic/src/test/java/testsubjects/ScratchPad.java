package testsubjects;

import java.sql.SQLException;

public class ScratchPad
{
    public Object getParameter(int index)
    {
        switch (index)
        {
            case 0:
                return null;

            default:
                throw new IllegalArgumentException("Blah.");
        }
    }

    public void go() throws SQLException, RuntimeException
    {
    }
}
