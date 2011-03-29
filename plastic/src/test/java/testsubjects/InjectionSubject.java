package testsubjects;

import org.apache.tapestry5.plastic.test.TestInject;

public class InjectionSubject
{
    @TestInject
    private Runnable injected;

    void go()
    {
        injected.run();
    }
}
