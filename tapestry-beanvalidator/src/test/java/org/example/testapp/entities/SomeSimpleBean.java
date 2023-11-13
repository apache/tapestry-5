package org.example.testapp.entities;

import jakarta.validation.constraints.Min;

public class SomeSimpleBean
{

    @Min(6)
    private int minValue;

    public int getMinValue()
    {
        return minValue;
    }

    public void setMinValue(int minValue)
    {
        this.minValue = minValue;
    }

}
