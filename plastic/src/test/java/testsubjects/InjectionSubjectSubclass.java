package testsubjects;

import java.util.concurrent.Callable;

import org.apache.tapestry5.plastic.test.TestInject;

public class InjectionSubjectSubclass extends InjectionSubject
{

    @TestInject
    private Callable<Object> callable;

    Object call() throws Exception
    {
        return callable.call();
    }
}
