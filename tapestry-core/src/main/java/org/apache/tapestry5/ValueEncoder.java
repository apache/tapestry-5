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

package org.apache.tapestry5;

/**
 * A ValueEncoder is used to convert server side objects to unique client-side
 * strings (typically IDs) and back. This mechanism is widely used in Tapestry
 * to allow you to work more seamlessly with objects rather than manually
 * managing the encoding and decoding process throughout your application.
 *
 * Tapestry uses a ValueEncoder when generating an
 * {@link org.apache.tapestry5.EventContext} as part of a URL, and when
 * components (such as {@link org.apache.tapestry5.corelib.components.Select})
 * need to generate unique client-side strings to be rendered within form
 * elements.
 *
 * Tapestry can automatically generate ValueEncoders for enums as well as
 * Collections of any object types for which a coercion can be found from a
 * formatted String, such as primitives, primitive wrappers, Dates, Calendars,
 * "name=value" strings, and any types for which a {@linkplain org.apache.tapestry5.commons.services.TypeCoercer
 * custom type coercion} has been contributed.
 *
 * Custom ValueEncoder implementations will need to be supplied for entity type
 * objects. In such cases the {@link #toClient(Object)} method typically returns
 * an object's database primary key, and the {@link #toValue(String)}
 * re-acquires the corresponding entity object, perhaps by doing a database
 * lookup by that ID.
 *
 * Some optional modules, such as Tapestry's own Hibernate and JPA modules, can
 * automatically create a ValueEncoder for each of your entity types and then
 * configure Tapestry to use them whenever a ValueEncoder is needed for those
 * types. If you don't use one of those modules, you can still configure
 * Tapestry to automatically use your custom ValueEncoder implementations by
 * having your ValueEncoder implement the
 * {@link org.apache.tapestry5.services.ValueEncoderFactory} interface and then
 * contributing a ValueEncoderSource that adds your encoder, like this, in your
 * application's module class:
 * 
* <pre>
 * public static void contributeValueEncoderSource(
 *         MappedConfiguration&lt;Class&lt;Color&gt;, ValueEncoderFactory&lt;Color&gt;&gt; configuration)
 * {
 *     configuration.addInstance(Color.class, ColorEncoder.class);
 * }
 * </pre>
 * 
 * @see SelectModel
 * @see org.apache.tapestry5.services.ValueEncoderSource
 * @see org.apache.tapestry5.services.ValueEncoderFactory
 * @see org.apache.tapestry5.annotations.PageActivationContext
 * @see org.apache.tapestry5.annotations.RequestParameter
 * @see org.apache.tapestry5.annotations.ActivationRequestParameter
 */
public interface ValueEncoder<V>
{
    /**
     * Converts a value into a client-side representation. The value should be parseable by {@link #toValue(String)}. In
     * some cases, what is returned is an identifier used to locate the true object, rather than a string representation
     * of the value itself.
     *
     * @param value to be encoded
     * @return a string representation of the value, or the value's identity
     */
    String toClient(V value);

    /**
     * Converts a client-side representation, provided by {@link #toClient(Object)}, back into a server-side value.
     *
     * @param clientValue string representation of the value's identity
     * @return the corresponding entity, or null if not found
     */
    V toValue(String clientValue);
}
