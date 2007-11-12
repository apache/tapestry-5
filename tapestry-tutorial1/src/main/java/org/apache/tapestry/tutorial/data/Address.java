package org.apache.tapestry.tutorial.data;

import org.apache.tapestry.beaneditor.Validate;

public class Address
{
    private Honorific _honorific;

    private String _firstName;

    private String _lastName;

    private String _street1;

    private String _street2;

    private String _city;

    private String _state;

    private String _zip;

    private String _email;

    private String _phone;

    public Honorific getHonorific()
    {
        return _honorific;
    }

    @Validate("required")
    public String getFirstName()
    {
        return _firstName;
    }

    public String getLastName()
    {
        return _lastName;
    }

    @Validate("required")
    public String getStreet1()
    {
        return _street1;
    }

    public String getStreet2()
    {
        return _street2;
    }

    @Validate("required")
    public String getCity()
    {
        return _city;
    }

    @Validate("required")
    public String getState()
    {
        return _state;
    }

    @Validate("required,regexp")
    public String getZip()
    {
        return _zip;
    }

    public String getEmail()
    {
        return _email;
    }

    public String getPhone()
    {
        return _phone;
    }

    public void setCity(String city)
    {
        _city = city;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    public void setHonorific(Honorific honorific)
    {
        _honorific = honorific;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    public void setPhone(String phone)
    {
        _phone = phone;
    }

    public void setState(String state)
    {
        _state = state;
    }

    public void setStreet1(String street1)
    {
        _street1 = street1;
    }

    public void setStreet2(String street2)
    {
        _street2 = street2;
    }

    public void setZip(String zip)
    {
        _zip = zip;
    }
}
