package org.apache.tapestry;

/**
 * Constants for persistent field strategies.
 *
 * @see org.apache.tapestry.annotations.Persist#value()
 */
public class PersistenceConstants
{
    /**
     * The page field persistence strategy that stores data in the session until the next request.
     */
    public static final String FLASH = "flash";
}
