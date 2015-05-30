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

import java.util.List;

/**
 * A method of a {@linkplain PlasticClass transformed class}.
 *
 * No methods of this object should be invoked after the class transformation is
 * {@linkplain PlasticClassTransformation#createInstantiator() completed}.
 */
public interface PlasticMethod extends AnnotationAccess
{
    /**
     * Returns the PlasticClass containing this method.
     */
    PlasticClass getPlasticClass();

    /**
     * Returns a representation of the method's name, return value, argument types, etc.
     */
    MethodDescription getDescription();

    /**
     * Returns a handle that can be used to invoke a method of a transformed class instance.
     */
    MethodHandle getHandle();

    /**
     * Clears the instructions for this method, and creates a new empty InstructionBuilder so that the implementation of
     * the method can be specified. This may be considered a kind of last resort when no other approach is sufficient.
     *
     * If the method is currently abstract, it will have its abstract flag cleared.
     *
     * If the method has advice, the advice is <em>not</em> lost but will instead wrap around the new method
     * implementation.
     *
     * @param callback
     *         passed the InstructionBuilder so that an implementation of the method can be created
     * @return this method, for further configuration
     */
    PlasticMethod changeImplementation(InstructionBuilderCallback callback);

    /**
     * Adds advice to the method. Adding advice implicitly rewrites the implementation of the method (this occurs
     * inside at the end of the class transformation). When the method is invoked, control will flow
     * through the MethodAdvice <em>in the order they are added</em>. Each piece of advice will receive the
     * {@link MethodInvocation} and should invoke {@link MethodInvocation#proceed()} to pass control to the next piece
     * of advice (and ultimately, to the actual method invocation).
     *
     * If a method implementation is changed, using {@link #changeImplementation(InstructionBuilderCallback)}, that
     * change will be honored, but the logic will only be invoked at the end of the chain of MethodAdvice. Internally, a
     * new method is created with the same parameters, exceptions, return type and implementation (bytecode) as the
     * advised method, <em>then</em> the advised method's implementation is changed.
     *
     * Note additionally that a recursive method invocation will still invoke the MethodAdvice chain on each recursive
     * call (this is an intended side-effect of copying the exact bytecode of the method implementation.
     *
     * @param advice
     *         advice to add to the method
     * @return this method, for further configuration
     */
    PlasticMethod addAdvice(MethodAdvice advice);

    /**
     * Changes the implementation of the method to delegate to the provided field. The field must implement the
     * correct interface (or extend the correct class). The original implementation of the method is lost,
     * though (as with {@link #changeImplementation(InstructionBuilderCallback)}), method advice is retained.
     *
     * @param field
     *         to delegate to
     * @return this method, for further configuration
     */
    PlasticMethod delegateTo(PlasticField field);

    /**
     * Much like {@link #delegateTo(PlasticField)}, but the object to delegate to
     * is dynamically computed by another method of the class. The method should take no parameters
     * and must not return null, or throw any exceptions not compatible with the method being proxied.
     *
     * @param method
     *         to provide the dynamic delegate
     * @return this method, for further configuration
     */
    PlasticMethod delegateTo(PlasticMethod method);

    /**
     * Returns access to the parameters of the method and, in particular,
     * the visible annotations on those parameters.
     */
    List<MethodParameter> getParameters();

    /**
     * Returns true if the method is an override of a method from the parent class.
     *
     * @return true if the parent class contains a method with the name signature
     */
    boolean isOverride();

    /**
     * Returns true if the method is abstract.
     *
     * @since 5.4
     */
    boolean isAbstract();

    /**
     * Returns a short identifier for the method that includes the class name, the method name,
     * and the types of all parameters. This is often used when producing debugging output
     * about the method.
     *
     * @return short identifier
     * @see org.apache.tapestry5.plastic.MethodDescription#toShortString()
     */
    String getMethodIdentifier();

    /**
     * Returns true if this method is type void.
     *
     * @return true for void methods.
     */
    boolean isVoid();
}
