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

package org.apache.tapestry5.services.javascript;

/**
 * A contribution to an extensible {@link JavaScriptStack}. Such a stack is created in terms of all the contributions.
 *
 * @see ExtensibleJavaScriptStack
 * @since 5.3
 */
public class StackExtension
{
    /**
     * The type of extension.
     */
    public final StackExtensionType type;

    /**
     * The value contributed; will have symbols expanded, then be converted to the appropriate type.
     */
    public final String value;

    public StackExtension(StackExtensionType type, String value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return String.format("StackExtension[%s %s]", type.name(), value);
    }

    /**
     * Convenience for defining a LIBRARY.
     *
     * @since 5.4
     */
    public static StackExtension library(String path)
    {
        return new StackExtension(StackExtensionType.LIBRARY, path);
    }

    /**
     * Convenience for defining a MODULE.
     *
     * @since 5.4
     */
    public static StackExtension module(String name)
    {
        return new StackExtension(StackExtensionType.MODULE, name);
    }

    /**
     * Convenience for defining a STYLESHEET.
     *
     * @since 5.4
     */
    public static StackExtension stylesheet(String path)
    {
        return new StackExtension(StackExtensionType.STYLESHEET, path);
    }

    /**
     * Convenience for defining a STACK.
     *
     * @since 5.4
     */
    public static StackExtension stack(String name)
    {
        return new StackExtension(StackExtensionType.STACK, name);
    }

    /**
     * Convenience for defining the {@link JavaScriptStack#getJavaScriptAggregationStrategy()}.
     *
     * @since 5.4
     */
    public static StackExtension javascriptAggregation(JavaScriptAggregationStrategy strategy)
    {
        return new StackExtension(StackExtensionType.AGGREGATION_STRATEGY, strategy.name());
    }

}
