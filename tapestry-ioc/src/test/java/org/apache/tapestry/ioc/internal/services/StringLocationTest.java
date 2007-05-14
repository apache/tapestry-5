package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.Location;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StringLocationTest extends Assert
{
    @Test
    public void thats_all_there_is_folks()
    {
        String description = "location description";
        int line = 99;

        Location l = new StringLocation(description, line);

        assertEquals(l.toString(), description);
        assertEquals(l.getLine(), line);
        assertEquals(l.getColumn(), 0);
        assertNull(l.getResource());
    }
}
