// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.plastic

import java.text.DateFormat

import spock.lang.Specification
import testsubjects.ContextCatcher

class ClassInstantiatorTests extends Specification
{

    def ins = new ClassInstantiatorImpl(ContextCatcher.class, ContextCatcher.class.constructors[0], null)

    def "adding a context value returns a new instantiator"() {

        String value = "instance value of type String";

        when:
        def ins2 = ins.with(String.class, value)

        then:
        ! ins2.is(ins)

        ins2.get(String.class).is(value)
    }

    def "may not add a duplicate instance context value"() {

        when:
        ins.with(String.class, "initial value").with(String.class, "conflicting value")

        then:
        def e = thrown(IllegalStateException)

        e.message == "An instance context value of type java.lang.String has already been added."
    }

    def "get a value not stored is a failure"() {
        when:
        ins.get(DateFormat.class)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Instance context for class testsubjects.ContextCatcher does not contain a value for type class java.text.DateFormat."
    }

    def "instance map wrapped as InstanceContext and passed to constructed object"() {
        def value = "instance value of type String"

        when:

        def o = ins.with(String.class, value).newInstance()

        then:

        o.instanceContext.get(String.class).is(value)
    }
}
