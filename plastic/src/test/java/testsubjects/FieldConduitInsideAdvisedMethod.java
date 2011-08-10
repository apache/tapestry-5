package testsubjects;

import testannotations.FieldAnnotation;
import testannotations.MethodAnnotation;

public class FieldConduitInsideAdvisedMethod
{
    @FieldAnnotation
    private String magic;

    @MethodAnnotation
    public String getMagic()
    {
        return magic;
    }
}
