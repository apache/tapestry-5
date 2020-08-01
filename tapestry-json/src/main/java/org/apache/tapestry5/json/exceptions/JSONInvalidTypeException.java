package org.apache.tapestry5.json.exceptions;

public class JSONInvalidTypeException extends RuntimeException
{

    private static final long serialVersionUID = 934805933638996600L;

    private Class<? extends Object> invalidClass;

    public JSONInvalidTypeException(Class<? extends Object> invalidClass)
    {
        super("JSONArray values / JSONObject properties may be one of Boolean, Number, String, org.apache.tapestry5.json.JSONArray, org.apache.tapestry5.json.JSONLiteral, org.apache.tapestry5.json.JSONObject, org.apache.tapestry5.json.JSONObject$Null, org.apache.tapestry5.json.JSONString. Type "
                + invalidClass.getName() + " is not allowed.");

        this.invalidClass = invalidClass;
    }

    public Class<? extends Object> getInvalidClass()
    {
        return this.invalidClass;
    }
}
