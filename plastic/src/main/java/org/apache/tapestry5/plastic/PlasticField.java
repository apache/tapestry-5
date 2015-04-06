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

/**
 * Represents a field of a class being {@linkplain PlasticClass transformed}.
 *
 * No methods of this object should be invoked after the class transformation is completed.
 */
public interface PlasticField extends AnnotationAccess
{
    /** Returns the class containing this field. */
    PlasticClass getPlasticClass();

    /**
     * Returns a handle that can be used to directly access a private field of a
     * transformed class instance.
     */
    FieldHandle getHandle();

    /**
     * Returns the name of the field.
     */
    String getName();

    /**
     * Returns the fully qualified class name for the field's type or (for a primitive type)
     * the primitive type name ("int", "char", etc.). For array types, the returned name includes
     * a "[]" suffix.
     */
    String getTypeName();

    /**
     * Claims the field, used to indicate that the field is "processed". A field may only
     * be claimed once. Claiming a field is intended as a mechanism to detect or prevent
     * conflicts between different isolated transformations of the field. The tag value used does not matter, and is
     * typically either an annotation (that drove the transformation) or the instance of {@link PlasticClassTransformer}
     * that performed the transformation. That tag value is only used when generating the error message for the case
     * where a field is claimed for than once.
     * 
     * @throws RuntimeException
     *             if the field is claimed a second time
     * @throws AssertionError
     *             if tag is null
     * @see PlasticClass#getUnclaimedFields()
     * @return the field for further manipulation
     */
    PlasticField claim(Object tag);

    /**
     * Returns true if the field has already been {@linkplain #claim(Object) claimed}.
     * 
     * @see PlasticClass#getUnclaimedFields()
     */
    boolean isClaimed();

    /**
     * Converts the field to be read-only, and provide the indicated value. The field's value will be
     * set inside the class' constructor.
     * 
     * @param value
     *            to inject, which must be type compatible with the field (possibly, a wrapper type if the field is
     *            a primitive value). The value may not be null.
     * @return the field for further manipulation
     * @throws IllegalStateException
     *             if the field already has an injection, or the field has a conduit
     */
    PlasticField inject(Object value);

    /**
     * Converts the field to be read-only, and provide the value, which is computed
     * indirectly inside the class' constructor.
     * 
     * @param computedValue
     *            provides the actual value to be injected, and must return a value type compatible
     *            with the field (possibly a wrapper type if the field is a primitive value). The computedValue may not
     *            be null.
     * @return the field for further manipulation
     * @throws IllegalStateException
     *             if the field already has an injection, or the field has a conduit
     */
    PlasticField injectComputed(ComputedValue<?> computedValue);

    /**
     * As with {@link #inject(Object)}, but the value is extracted from the {@link InstanceContext}.
     * 
     * @return this field for further manipulation
     */
    PlasticField injectFromInstanceContext();

    /**
     * Intercepts all access to the field, replacing such access with calls on the conduit. Even access via
     * the FieldHandle will instead delegate to the conduit. Once a conduit is provided, it is not possible
     * to inject a value into the field.
     *
     * Normally, once a conduit is in place, the field will never be actually read or written. This is problematic for
     * debugging, so {@link TransformationOption#FIELD_WRITEBEHIND} is useful when operating in a non-production mode.
     *
     * @return the field for further manipulation
     * @throws IllegalStateException
     *             if the field already has an injection or a conduit
     * @return this field for further manipulation
     */
    <F> PlasticField setConduit(FieldConduit<F> conduit);

    /**
     * Sets the conduit for the field to a value computed when the class is instantiated
     * 
     * @param computedConduit
     *            object that will compute the actual conduit to be used
     * @return this field for further manipulation
     */
   <F>  PlasticField setComputedConduit(ComputedValue<FieldConduit<F>> computedConduit);

    /**
     * Creates access to the field, using the default property name derived from the name of the field.
     * The default property name is the same as the name of the field, but with any leading or trailing underscore
     * characters removed (a common convention among some programmers). Also, strips leading "m_" from the field name
     * (another common convention).
     * 
     * @param accessType
     *            which methods to create
     * @return the field for further manipulation
     * @throws IllegalArgumentException
     *             if an accessor method to be created already exists (possibly inherited from a base class)
     */
    PlasticField createAccessors(PropertyAccessType accessType);

    /**
     * Creates accessors, possibly replacing existing methods (or overriding methods from a super class).
     * The method names consist of the property name, with its first character converted to upper-case, prefixed
     * with "get" or "set". The accessor methods must not already exist.
     * 
     * @param accessType
     *            which methods to create
     * @param propertyName
     *            the name of the property (from which the names of the methods are generated)
     * @return the field for further manipulation
     * @throws IllegalArgumentException
     *             if an accessor method to be created already exists (possibly inherited from a base class)
     */
    PlasticField createAccessors(PropertyAccessType accessType, String propertyName);

    /** Returns the field's fully qualified generic type, or null if not defined. */
    String getGenericSignature();

    /** Returns the modifiers on the field. */
    int getModifiers();
}
