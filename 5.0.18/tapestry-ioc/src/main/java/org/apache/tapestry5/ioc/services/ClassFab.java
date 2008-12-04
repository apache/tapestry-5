// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

/**
 * Used when fabricating a new class. Represents a wrapper around the Javassist library.
 * <p/>
 * The core concept of Javassist is how method bodies (as well as constructor bodies, etc.) are specified ... as a very
 * Java-like scripting language. Details are available at the <a href="http://jboss.org/products/javassist">Javassist
 * home page</a>.
 * <p/>
 * Method bodies look largely like Java. References to java classes must be fully qualified. Several special variables
 * are used: <ul> <li><code>$0</code> first parameter, equivalent to <code>this</code> in Java code (and can't be used
 * when creating a static method) <li><code>$1, $2, ...</code> actual parameters to the method <li><code>$args</code>
 * all the parameters as an <code>Object[]</code> <li><code>$r</code> the return type of the method, typically used as
 * <code>return ($r) ...</code>. <code>$r</code> is valid with method that return <code>void</code>. This also handles
 * conversions between wrapper types and primitive types. <li><code>$w</code> conversion from primitive type to wrapper
 * type, used as <code>($w) foo()</code> where <code>foo()</code> returns a primitive type and a wrapper type is needed
 * <li> </ul>
 * <p/>
 * ClassFab instances are not thread safe.
 * <p/>
 * ClassFab instances are created by a {@link org.apache.tapestry5.ioc.services.ClassFactory}.
 */
public interface ClassFab
{
    /**
     * Adds the specified interface as an interface implemented by this class. It is not an error to invoke this method
     * multiple times with the same interface class (and the interface is only added once).
     */
    void addInterface(Class interfaceClass);

    /**
     * Adds a new field with the given name and type. The field is added as a private field.
     */
    void addField(String name, Class type);

    /**
     * Adds a new field with the provided modifiers.
     */
    void addField(String name, int modifiers, Class Type);

    /**
     * Adds a method. The method is a public instance method.
     *
     * @param modifiers Modifiers for the method (see {@link java.lang.reflect.Modifier}).
     * @param signature defines the name, return type, parameters and exceptions thrown
     * @param body      The body of the method.
     * @return a method fabricator, used to add catch handlers.
     * @throws RuntimeException if a method with that signature has already been added, or if there is a Javassist
     *                          compilation error
     */
    void addMethod(int modifiers, MethodSignature signature, String body);

    /**
     * Adds a constructor to the class. The constructor will be public.
     *
     * @param parameterTypes the type of each parameter, or null if the constructor takes no parameters.
     * @param exceptions     the type of each exception, or null if the constructor throws no exceptions.
     * @param body           The body of the constructor.
     */
    void addConstructor(Class[] parameterTypes, Class[] exceptions, String body);

    /**
     * Adds an implementation of toString, as a method that returns a fixed string.
     */
    void addToString(String toString);

    /**
     * Makes the fabricated class implement the provided service interface. The interface will be added, and all methods
     * in the interface will be delegate wrappers. If toString() is not part of the delegate interface, then an
     * implementation will be supplied that returns the provided string. This method is used when creating objects that
     * proxy their behavior to some other object.
     *
     * @param serviceInterface   the interface to implement
     * @param delegateExpression the expression used to find the delegate on which methods should be invoked. Typically
     *                           a field name, such as "_delegate", or a method to invoke, such as "_service()".
     * @param toString           fixed value to be returned as the description of the resultant object
     */
    void proxyMethodsToDelegate(Class serviceInterface, String delegateExpression, String toString);

    /**
     * Invoked last to create the class. This will enforce that all abstract methods have been implemented in the
     * (concrete) class.
     */
    Class createClass();

    /**
     * Adds a public no-op method. The method will return null, false, or zero as per the return type (if not void).
     */

    void addNoOpMethod(MethodSignature signature);
}
