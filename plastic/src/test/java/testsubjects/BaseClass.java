package testsubjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseClass
{
    public void voidMethod()
    {
    }

    public int primitiveMethod(int input)
    {
        return input;
    }

    public String objectMethod(String input)
    {
        return input;
    }

    public Map<Long, List<String>> genericParametersAndReturnTypeMethod(Long key, List<String> aList) {
        HashMap<Long, List<String>> map = new HashMap<Long, List<String>>();
        map.put(key, aList);
        return map;
    }
}
