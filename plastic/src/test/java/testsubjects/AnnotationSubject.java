package testsubjects;

import testannotations.ArrayAnnotation;
import testannotations.Maybe;
import testannotations.Outer;
import testannotations.PrimitiveValues;
import testannotations.SimpleAnnotation;
import testannotations.Truth;

@SimpleAnnotation
@ArrayAnnotation
public class AnnotationSubject
{
    private int noAnnotation;

    @Maybe(Truth.YES)
    private int hasMaybeAnnotation;

    @Maybe(Truth.YES)
    void yes()
    {
    }

    @Maybe(Truth.NO)
    void no()
    {
    }

    void keepThisMethodFreeOfAnnotations()
    {
    }

    @PrimitiveValues(count = 5, type = Runnable.class, title = "runnables")
    void primitiveValues()
    {
    }

    @Outer(maybe = @Maybe(Truth.YES))
    void nestedAnnotation()
    {
    }
}
