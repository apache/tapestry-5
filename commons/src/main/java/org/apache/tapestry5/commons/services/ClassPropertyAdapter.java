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

package org.apache.tapestry5.commons.services;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Organizes all {@link org.apache.tapestry5.commons.services.PropertyAdapter}s for a particular class.
 *
 * Only provides access to <em>simple</em> properties. Indexed properties are ignored.
 *
 * When accessing properties by name, the case of the name is ignored.
 */
public interface ClassPropertyAdapter
{
    /**
     * Returns the names of all properties, sorted into alphabetic order. This includes true properties
     * (as defined in the JavaBeans specification), but also public fields. Starting in Tapestry 5.3, even public static fields are included.
     * @return the property names.
     */
    List<String> getPropertyNames();

    /**
     * Returns the type of bean this adapter provides properties for.
     * @return the type of the bean.
     */
    @SuppressWarnings("rawtypes")
    Class getBeanType();

    /**
     * Returns the property adapter with the given name, or null if no such adapter exists.
     *
     * @param name of the property (case is ignored)
     * @return the PropertyAdapter instance associated with that property
     */
    PropertyAdapter getPropertyAdapter(String name);

    /**
     * Reads the value of a property.
     *
     * @param instance     the object to read a value from
     * @param propertyName the name of the property to read (case is ignored)
     * @throws UnsupportedOperationException if the property is write only
     * @throws IllegalArgumentException      if property does not exist
     * @return the value
     */
    Object get(Object instance, String propertyName);

    /**
     * Updates the value of a property.
     *
     * @param instance     the object to update
     * @param propertyName the name of the property to update (case is ignored)
     * @param value        the value to be set
     * @throws UnsupportedOperationException if the property is read only
     * @throws IllegalArgumentException      if property does not exist
     */
    void set(Object instance, String propertyName, Object value);

    /**
     * Returns the annotation of a given property for the specified type if such an annotation is present, else null.
     *
     * @param instance     the object to read a value from
     * @param propertyName the name of the property to read (case is ignored)
     * @param annotationClass the type of annotation to return
     * @return the Annotation instance
     * @throws IllegalArgumentException      if property does not exist
     *
     * @since 5.4
     */
    Annotation getAnnotation(Object instance, String propertyName, Class<? extends Annotation> annotationClass);
}
