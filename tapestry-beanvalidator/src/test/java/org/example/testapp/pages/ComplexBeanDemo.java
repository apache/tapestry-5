package org.example.testapp.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.example.testapp.entities.ComplexBean;

public class ComplexBeanDemo
{

    @Property
    @Persist
    private ComplexBean complexBean;

}
