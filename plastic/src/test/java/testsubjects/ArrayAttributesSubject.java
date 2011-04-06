package testsubjects;

import testannotations.ArrayAnnotation;
import testannotations.Maybe;
import testannotations.Truth;

@ArrayAnnotation(numbers = 5, strings =
{ "frodo", "sam" }, types = Runnable.class, annotations =
{ @Maybe(Truth.YES), @Maybe(Truth.NO) })
public class ArrayAttributesSubject
{

}
