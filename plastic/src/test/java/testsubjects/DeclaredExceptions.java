package testsubjects;

import testannotations.MethodAnnotation;

public class DeclaredExceptions
{
    @MethodAnnotation
    public void throwsRuntime() throws RuntimeException {
        throw new RuntimeException("throwsRuntime");
    }

    @MethodAnnotation
    public void throwsError() throws Error {
        throw new Error("throwsError");
    }

    @MethodAnnotation
    public void throwsException() throws Exception {
        throw new Exception("throwsException");
    }
}
