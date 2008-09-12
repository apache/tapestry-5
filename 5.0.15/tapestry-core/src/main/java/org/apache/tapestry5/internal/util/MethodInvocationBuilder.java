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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.services.TransformUtils;

import java.util.Map;

/**
 * A utility class for building part of a method body to invoke a method. Analyzes the method and matches parameter
 * types to ParameterBuilders.
 */
public final class MethodInvocationBuilder
{
    private final Map<String, ParameterBuilder> builders = CollectionFactory.newMap();

    /**
     * Maps a parameter type to a {@link ParameterBuilder}.
     */
    public void addParameter(String parameterType, ParameterBuilder builder)
    {
        // TODO: Name conflicts

        builders.put(parameterType, builder);
    }

    /**
     * Maps a parameter type to a literal string to be used for the parameter expression.
     *
     * @see StringParameterBuilder
     */
    public void addParameter(String parameterType, String expression)
    {
        addParameter(parameterType, new StringParameterBuilder(expression));
    }

    /**
     * Builds the method invocation. Analyzes the type of each parameter to the method, and uses a {@link
     * ParameterBuilder} to provide the expression. Supplies a default value (usually null) for any parameters that do
     * not have parameter builders.
     *
     * @param signature      of the method to invoke
     * @param transformation
     * @return method invocation expression
     * @see TransformUtils#getDefaultValue(String)
     */
    public String buildMethodInvocation(TransformMethodSignature signature,
                                        ClassTransformation transformation)
    {
        StringBuilder builder = new StringBuilder(signature.getMethodName());

        builder.append("(");

        String[] parameterTypes = signature.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (i > 0) builder.append(", ");

            String type = parameterTypes[i];

            ParameterBuilder parameterBuilder = builders.get(type);

            if (parameterBuilder == null)
            {
                // TODO: Log an error

                builder.append(TransformUtils.getDefaultValue(type));
            }
            else
            {
                builder.append(parameterBuilder.buildParameter(transformation));
            }
        }

        builder.append(")");

        return builder.toString();
    }

}
