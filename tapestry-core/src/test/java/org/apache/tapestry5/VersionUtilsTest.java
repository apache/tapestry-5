package org.apache.tapestry;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VersionUtilsTest extends Assert
{
    @Test
    public void read_version_number_missing()
    {
        assertEquals(VersionUtils.readVersionNumber("no-such-file.properties"), "UNKNOWN");
    }

    @Test
    public void read_version_number()
    {
        assertEquals(VersionUtils.readVersionNumber("org/apache/tapestry/version.properties"), "1.2.3.4");
    }

    @Test
    public void read_version_number_no_version_key()
    {
        assertEquals(VersionUtils.readVersionNumber("org/apache/tapestry/noversion.properties"), "UNKNOWN");
    }
}
