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
 * A {@link FieldConduit} is an object that effectively <em>replaces</em> the field in the instantiated object.
 * All reads and writes of the field are replaced with invocations on the conduit. Once a field's access is replaced
 * with a conduit, the field itself is no longer used. The conduit will even see initializations of the field.
 *
 * In Aspect Oriented Programming terms, a FieldConduit allows you to advise read and write access to the field.
 *
 * If a field has both a FieldConduit and a {@link FieldHandle}, then the methods of the FieldHandle will be connected
 * to the methods of the FieldConduit.
 */
public interface FieldConduit<T>
{
    /**
     * Invoked when the field is read.
     * 
     * @param instance
     *            the instance containing the field
     * @param context
     *            (see {@link ClassInstantiator#with(Class, Object)})
     */
    T get(Object instance, InstanceContext context);

    /**
     * Invoked when the field's value is updated.
     * 
     * @param instance
     *            the instance containing the field
     * @param context
     *            (see {@link ClassInstantiator#with(Class, Object)})
     * @param newValue
     *            value assigned to the field
     */
    void set(Object instance, InstanceContext context, T newValue);
}
