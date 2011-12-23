package testsubjects;

import testannotations.SimpleAnnotation;

/**
 * Used to test access to protected fields. Accessed from {@link ProtectedFieldCollaborator}.
 */
public class ProtectedField
{
    @SimpleAnnotation
    protected String protectedValue;
}
