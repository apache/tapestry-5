// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.plastic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * The representation of a class while it is being instrumented and transformed. PlasticClass allows
 * for an imperative style of development: the PlasticClass is provided to other objects; they can query it
 * for relevant fields or methods, and invoke methods that modify the class in various ways. Ultimately, the
 * end result is a {@link ClassInstantiator} used to create instances of the fully instrumented and transformed class.
 *
 * The terminology is that a class that is being transformed is "plastic", but the end result is a normal concrete class
 * (albeit in a different class loader).
 *
 * Implements {@link AnnotationAccess} to provide access to annotations on the type itself.
 *
 * This class is expressly <em>not thread safe</em>; only a single thread should be responsible for operating on a
 * PlasticClass.
 *
 * TODO: what about annotation inheritance?
 */
@SuppressWarnings("rawtypes")
public interface PlasticClass extends AnnotationAccess
{
    /**
     * Returns the fully qualified class name of the class being transformed.
     */
    String getClassName();

    /**
     * Matches all fields (claimed or not) that have the given annotation. Returns the fields in sorted order.
     *
     * @return Unmodifiable List of fields.
     */
    <T extends Annotation> List<PlasticField> getFieldsWithAnnotation(Class<T> annotationType);

    /**
     * Returns all non-introduced fields, in sorted order by name.
     *
     * @return Unmodifiable list of fields.
     */
    List<PlasticField> getAllFields();

    /**
     * Returns all unclaimed fields, in sorted order by name. This does not include introduced fields.
     *
     * @return Unmodifiable list of fields.
     * @see PlasticField#claim(Object)
     */
    List<PlasticField> getUnclaimedFields();

    /**
     * Introduces a new private field into the class.
     *
     * @param typeName      the Java class name for the field, or (possibly) a primitive type name or an array
     * @param suggestedName the suggested name for the field, which may be modified to ensure that the field name
     *                      is unique
     * @return PlasticField for the introduced field
     */
    PlasticField introduceField(String typeName, String suggestedName);

    /**
     * Convenience method that uses a Java class rather than a type name.
     */
    PlasticField introduceField(Class fieldType, String suggestedName);

    /**
     * Introduces a new private method into the class, ensuring that the method name is unique.
     *
     * @param typeName       return type of method
     * @param suggestedName  suggested name for the method; the actual method name may be modified to ensure uniqueness
     * @param argumentTypes  types of any arguments (may be null)
     * @param exceptionTypes type of any checked exceptions (may be null)
     * @return new method, with default implementation
     */
    PlasticMethod introducePrivateMethod(String typeName, String suggestedName, String[] argumentTypes,
                                         String[] exceptionTypes);

    /**
     * Matches methods with the given annotation.
     *
     * @return Unmodifiable list of methods, in sorted order.
     */
    <T extends Annotation> List<PlasticMethod> getMethodsWithAnnotation(Class<T> annotationType);

    /**
     * Returns all methods of the class, in sorted order. This does not include static methods,
     * or any {@linkplain #introduceMethod(MethodDescription) introduced methods}.
     *
     * @return Unmodifiable list of methods.
     */
    List<PlasticMethod> getMethods();

    /**
     * Returns an existing method declared in this class, or introduces a new method into this class.
     * The method is created with default behavior. If the method overrides a non-private, non-abstract method
     * implemented in a <em>transformed</em> super class, the the default behavior is to invoke that method and return
     * its value. Otherwise, the default behavior is to ignore parameters and return 0, false, or null. Void methods
     * will invoke the super-class implementation (if it exists) and return no value.
     *
     * It is allowed for the method description to indicate an abstract method; however the abstract flag will be
     * removed, and a non-abstract method will be created.
     *
     * @param description describes the method name, visibility, return value, etc.
     * @return a new (or previously created) PlasticMethod for the method
     * @throws IllegalArgumentException if the method is abstract or static
     */
    PlasticMethod introduceMethod(MethodDescription description);

    /**
     * Returns an existing method declared in this class, or introduces a new method into this class.
     * The method is created with default behavior.
     *
     * It is allowed for the method description to indicate an abstract method; however the abstract flag will be
     * removed, and a non-abstract method will be created.
     *
     * @param description describes the method name, visibility, return value, etc.
     * @param callback    used to create the implementation of the method
     * @return a new (or previously created) PlasticMethod for the method
     * @throws IllegalArgumentException if the method is abstract or static
     */
    PlasticMethod introduceMethod(MethodDescription description, InstructionBuilderCallback callback);

    /**
     * A convenience that creates a {@link MethodDescription} from the Method and introduces that. This is often
     * invoked when walking the methods of an interface and introducing each of those methods.
     *
     * Introduced methods are always concrete, not abstract. The abstract flag on the method modifiers will always be
     * stripped off, which is handy when {@linkplain #introduceInterface(Class) introducing methods from an interface}.
     *
     * @param method to introduce
     * @return new (or previously created) PlasticMethod
     */
    PlasticMethod introduceMethod(Method method);

    /**
     * Introduces each method defined by the interface into the class. Determines which new methods must
     * be introduced in order to ensure that all methods of the interface are implemented. The newly introduced methods,
     * if any, are returned.
     */
    Set<PlasticMethod> introduceInterface(Class interfaceType);

    /**
     * Introduces the interface, and then invokes {@link PlasticMethod#delegateTo(PlasticField)} on each method
     * defined by the interface.
     *
     * @param interfaceType defines the interface to proxy
     * @param field         field containing an object to delegate to
     * @return this plastic class, for further configuration
     */
    PlasticClass proxyInterface(Class interfaceType, PlasticField field);
    
    /**
     * Introduces the interface, and then invokes {@link PlasticMethod#delegateTo(PlasticMethod)} on each method
     * defined by the interface.
     *
     * @param interfaceType defines the interface to proxy
     * @param method        method to delegate to
     * @return this plastic class, for further configuration
     * @since 5.4.4
     */
    PlasticClass proxyInterface(Class interfaceType, PlasticMethod method);

    /**
     * Conditionally adds an implementation of <code>toString()</code> to the class, but only if it is not already
     * present in the class, or in a (transformed) super-class.
     *
     * @param toStringValue the fixed value to be returned from invoking toString()
     * @return this plastic class, for further configuration
     */
    PlasticClass addToString(String toStringValue);

    /**
     * Returns true if this class has an implementation of the indicated method, or a super-class provides
     * a non-abstract implementation.
     */
    boolean isMethodImplemented(MethodDescription description);

    /**
     * Returns true if this class, or a super-class, implements the indicated interface.
     *
     * @param interfaceType
     * @return true if the interface is implemented
     */
    boolean isInterfaceImplemented(Class interfaceType);

    /**
     * Returns the name of the super-class of the class being transformed.
     */
    String getSuperClassName();

    /**
     * Adds the callback for execution when an instance of the class is instantiated.
     *
     * @param callback to execute at instance construction time
     */
    PlasticClass onConstruct(ConstructorCallback callback);
}
