package org.example.testapp.entities;

import javax.validation.constraints.NotNull;

public class SomeOtherSimpleBean
{

    @NotNull
    private String notNullString;

    public String getNotNullString()
    {
        return notNullString;
    }

    public void setNotNullString(String notNullString)
    {
        this.notNullString = notNullString;
    }

}
