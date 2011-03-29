package testsubjects;

import org.apache.tapestry5.plastic.test.TestInject;

public class ParameterAnnotationsSubject
{
    void theMethod(@TestInject
    String injectFirst, int normalSecond)
    {
    }
}
