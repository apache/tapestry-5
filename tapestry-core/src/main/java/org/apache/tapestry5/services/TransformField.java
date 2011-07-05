// Copyright 2010, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.services;

import java.lang.reflect.Field;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.services.FieldValueConduit;

/**
 * A field defined by (or created within) a {@link ClassTransformation},
 * allowing the details of the field to be
 * accessed or modified.
 *
 * @since 5.2.0
 */
public interface TransformField extends AnnotationProvider, Comparable<TransformField>
{
    /**
     * Returns the name of the field.
     */
    String getName();

    /**
     * Returns the field's type, either a primitive name (such as "int" or "boolean")
     * or a fully qualified class name, or an array type name
     * (in Java source syntax, i.e., "java.lang.String[]").
     */
    String getType();

    /**
     * Returns the field's fully qualified generic type, or null if not defined.
     * (in Java source syntax, i.e., "()Ljava/util/List<Ljava/lang/String;>;"
     *
     * @since 5.3
     */
    String getSignature();

    /**
     * Claims the field so as to ensure that only a single annotation is applied to any single field.
     * When a transformation occurs (driven by a field annotation), the field is claimed (using the
     * annotation object as the tag). If a field has multiple conflicting annotations, this will be discovered when
     * the code attempts to claim the field a second time.
     *
     * @param tag
     *            a non-null object that represents why the field is being tagged (this is typically
     *            a specific annotation on the field)
     * @throws IllegalStateException
     *             if the field is already claimed for some other tag
     */
    void claim(Object tag);

    /**
     * Replaces read and write field access with a conduit. 
     * 
     * @param conduitProvider
     *            provides the actual conduit at class instantiation time
     */
    void replaceAccess(ComponentValueProvider<FieldValueConduit> conduitProvider);

    /**
     * Replaces read and write field access with a conduit. 
     * 
     * @param conduitField
     *            identifies the field containing (via injection) an instance of {@link FieldValueConduit}
     */
    void replaceAccess(TransformField conduitField);

    /**
     * Replaces read and write field access with a conduit. A new field is created for the conduit instance.
     * 
     * @param conduit
     *            used to replace read and write access to the field
     */
    void replaceAccess(FieldValueConduit conduit);

    /**
     * Returns the modifiers for the field.
     *
     * @see Field#getModifiers()
     */
    int getModifiers();

    /**
     * Converts this field into a read only field whose value is the provided
     * value. This is used when converting an existing field into a read-only injected value.
     *
     * @param value
     *            the value provided by the field
     */
    void inject(Object value);

    /**
     * Like {@link #inject(Object)}, except that the value to be injected is obtained
     * from a {@link ComponentValueProvider}. It is assumed that the provider will return an object
     * assignable to the field.
     *
     * @param <T>
     *            type of field
     * @param provider
     *            provides the value to be assigned to the field
     */
    <T> void injectIndirect(ComponentValueProvider<T> provider);

    /**
     * Returns an object that can be used to access the value of the field for read and update.
     * Changes to the field will honor any {@link FieldValueConduit} that has been applied to the field.
     */
    FieldAccess getAccess();
}
