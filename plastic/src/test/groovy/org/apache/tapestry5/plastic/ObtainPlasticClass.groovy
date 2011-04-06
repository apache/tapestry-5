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

package org.apache.tapestry5.plastic

import java.lang.reflect.InvocationTargetException

import org.apache.tapestry5.internal.plastic.NoopDelegate

import spock.lang.Specification

class ObtainPlasticClass extends Specification
{
    def mgr = new PlasticManager()

    def "access to simple empty class"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.Empty")

        // More to come, but for now

        expect:
        pc != null

        pc.methods.empty == true
    }

    def "can obtain only method in class"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.SingleMethod")
        def methods = pc.methods

        expect:
        methods.size() == 1
        methods.first().description.toString() == "public void aSingleMethod(int)"
    }

    def "static methods are ignored"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.StaticMethodsIgnored")
        def methods = pc.methods

        expect:
        methods.size() == 1
        methods.first().description.toString() == "void anInstanceMethod()"
    }

    def "methods obtained in sorted alphabetic order"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.MultipleMethods")
        def descs = pc.methods.collect({
            it.description.toString()
        });

        expect:

        descs == [
            "protected void barney(java.lang.String)",
            "private int betty(int, int, int)",
            "public java.lang.String betty(int, int)",
            "public void betty(int)",
            "public synchronized void betty()",
            "void fred()",
            "private void wilma() throws java.sql.SQLException"
        ]
    }

    def "fields obtained in sorted alphabetic order"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.MultipleFields")

        expect:
        pc.fields.collect({ it.name }) == [
            "barney",
            "betty",
            "fred",
            "wilma"
        ]
    }

    def "static fields are ignored"() {
        setup:
        def pc = mgr.getPlasticClass("testsubjects.StaticFields")

        expect:
        pc.allFields == []
    }

    def "instance fields must be private"() {
        when:
        mgr.getPlasticClass("testsubjects.NonPrivateInstanceField")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Field shouldBePrivate of class testsubjects.NonPrivateInstanceField is not private. Class transformation requires that all instance fields be private."
    }

    def "alternate constructors now throw exceptions"() {
        setup:

        def delegate = new NoopDelegate()
        def packages = ["testsubjects"]as Set

        def mgr = new PlasticManager(Thread.currentThread().contextClassLoader, delegate, packages)

        Class clazz = mgr.classLoader.loadClass("testsubjects.AlternateConstructor")

        when:

        clazz.getConstructor([String.class]as Class[]).newInstance([null]as Object[])

        then:

        def e = thrown(InvocationTargetException)

        e.cause.getClass() == IllegalStateException.class

        e.cause.message == "Class testsubjects.AlternateConstructor has been transformed and may not be directly instantiated."
    }
}