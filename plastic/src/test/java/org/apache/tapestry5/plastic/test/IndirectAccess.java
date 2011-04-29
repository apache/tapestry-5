package org.apache.tapestry5.plastic.test;

public interface IndirectAccess<T>
{
    T get();

    void set(T newValue);
}
