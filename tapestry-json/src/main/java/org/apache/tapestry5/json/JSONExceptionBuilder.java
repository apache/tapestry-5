package org.apache.tapestry5.json;

import org.apache.tapestry5.json.exceptions.JSONTypeMismatchException;
import org.apache.tapestry5.json.exceptions.JSONValueNotFoundException;

class JSONExceptionBuilder
{

    static RuntimeException typeMismatch(boolean array, Object indexOrName, Object actual,
            JSONType requiredType)
    {
        String location = array ? "JSONArray[" + indexOrName + "]"
                : "JSONObject[\"" + indexOrName + "\"]";
        if (actual == null)
        {
            return valueNotFound(array, indexOrName, requiredType);
        }
        else
        {
            return new JSONTypeMismatchException(location, requiredType, actual.getClass());
        }
    }

    static RuntimeException valueNotFound(boolean array, Object indexOrName, JSONType requiredType)
    {
        String location = array ? "JSONArray[" + indexOrName + "]"
                : "JSONObject[\"" + indexOrName + "\"]";
        return new JSONValueNotFoundException(location, requiredType);
    }

    static RuntimeException tokenerTypeMismatch(Object actual, JSONType requiredType)
    {
        if (actual == null)
        {
            return new JSONValueNotFoundException("Value", requiredType);
        }
        else
        {
            return new JSONTypeMismatchException("Value", requiredType, actual.getClass());
        }
    }
}
