package testsubjects;

import testannotations.Maybe;
import testannotations.Truth;
import testinterfaces.AnnotationTransfer;

@Maybe(Truth.YES)
public class AnnotationTransferImpl implements AnnotationTransfer
{

    @Maybe(Truth.YES)
    public void method1(@Maybe(Truth.YES)
    int foo)
    {
        // TODO Auto-generated method stub

    }

    public void method2(@Maybe(Truth.YES)
    int bar)
    {
    }

    @Maybe(Truth.YES)
    public void notInInterfaceMethod()
    {
    }

    protected void hasNoAnnotationsMethod()
    {
    }

}
