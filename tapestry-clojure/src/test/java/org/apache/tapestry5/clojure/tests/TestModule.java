package org.apache.tapestry5.clojure.tests;

import org.apache.tapestry5.clojure.ClojureBuilder;

public class TestModule
{
    public static Fixture buildFixture(ClojureBuilder builder)
    {
        return builder.build(Fixture.class);
    }
}
