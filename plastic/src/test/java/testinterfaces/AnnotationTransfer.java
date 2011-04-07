package testinterfaces;

import testannotations.Maybe;
import testannotations.Truth;

@Maybe(Truth.NO)
public interface AnnotationTransfer
{
    @Maybe(Truth.NO)
    void method1(@Maybe(Truth.NO)
    int foo);
    
    void method2(int bar);
}
