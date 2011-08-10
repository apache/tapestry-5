package testsubjects;

import testannotations.FieldAnnotation;
import testannotations.MethodAnnotation;
import testinterfaces.MagicContainer;


public class FieldConduitAdvisedMethodComplexCase
{
    @FieldAnnotation
    private MagicContainer container;

    @MethodAnnotation
    public String getMagic()
    {
        return container.magic();
    }
}
