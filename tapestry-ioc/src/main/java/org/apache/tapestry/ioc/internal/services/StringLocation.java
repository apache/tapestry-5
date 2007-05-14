package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Resource;

/**
 * Implementation of {@link Location} used when the underlying resource isn't really known.
 */
public final class StringLocation implements Location
{
    private final String _description;

    private final int _line;

    public StringLocation(String description, int line)
    {
        _description = description;
        _line = line;
    }

    @Override
    public String toString()
    {
        return _description;
    }

    /** Returns 0. */
    public int getColumn()
    {
        return 0;
    }

    public int getLine()
    {
        return _line;
    }

    /**
     * Returns null; we don't know where the file really is (it's probably a class on the class
     * path).
     */
    public Resource getResource()
    {
        return null;
    }

}
