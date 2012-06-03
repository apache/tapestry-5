package org.apache.tapestry5.clojure.tests;

import org.apache.tapestry5.clojure.FunctionName;
import org.apache.tapestry5.clojure.Namespace;

import java.util.List;

@Namespace("fixture")
public interface Fixture
{
    long doubler(long value);

    @FunctionName("clojure.core/first")
    Object first(List<?> list);
}
