package org.apache.tapestry5.ioc.test.internal;

import java.io.File;

public interface NonAnnotatedGenericServiceInterface<T>
{
    String execute1(int i);

    String execute2(int t);

    String execute2(T t);

    String execute2(File t);

    String execute2(T t, String s);

    T execute3(int i);
}
