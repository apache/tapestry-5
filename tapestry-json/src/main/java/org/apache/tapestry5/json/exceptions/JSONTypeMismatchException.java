package org.apache.tapestry5.json.exceptions;

import org.apache.tapestry5.json.JSONType;

public class JSONTypeMismatchException extends RuntimeException
{

    private static final long serialVersionUID = 268314880458464132L;

    private final String location;
    private final JSONType requiredType;
    private final Class<? extends Object> invalidClass;

    public JSONTypeMismatchException(String location, JSONType requiredType,
            Class<? extends Object> invalidClass)
    {
        super(location + (invalidClass == null ? " is null."
                : " is not a " + requiredType.name() + ". Actual: " + invalidClass.getName()));
        this.location = location;
        this.requiredType = requiredType;
        this.invalidClass = invalidClass;
    }

    public String getLocation()
    {
        return this.location;
    }

    public JSONType getRequiredType()
    {
        return this.requiredType;
    }

    public Class<? extends Object> getInvalidClass()
    {
        return this.invalidClass;
    }

}
