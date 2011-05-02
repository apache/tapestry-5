package testsubjects;

import java.util.concurrent.atomic.AtomicReference;

import testannotations.Property;

public class GenericCreateAccessorsSubject
{
    @Property
    private AtomicReference<String> m_ref;

    public String getRefValue()
    {
        return m_ref.get();
    }
}
