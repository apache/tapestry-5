package org.apache.tapestry.internal.services;

public class MethodPrefixTarget
{
    // If this is final, then the read is inlined, defeating the test.
    private int _targetField = 42;

    public int getTargetValue()
    {
        return _targetField;
    }

    // Again, necessary to defeat inlining of the value.
    public void setTargetField(int value)
    {
        _targetField = value;
    }
}
