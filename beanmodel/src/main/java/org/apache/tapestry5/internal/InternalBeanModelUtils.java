// Copyright 2007, 2008 The Apache Software Foundation
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
package org.apache.tapestry5.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.NullAnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.InternalStringUtils;

/**
 * Some methods broken off tapestry-core's InternalUtils to avoid bringing the whole class
 * plus its multiple dependencies to the BeanModel package.
 */
public class InternalBeanModelUtils {
	
	public static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\w]");
	
	/**
	 * @since 5.3
	 */
	private final static AnnotationProvider NULL_ANNOTATION_PROVIDER = new NullAnnotationProvider();

	
    /**
     * Used to convert a property expression into a key that can be used to locate various resources (Blocks, messages,
     * etc.). Strips out any punctuation characters, leaving just words characters (letters, number and the
     * underscore).
     *
     * @param expression a property expression
     * @return the expression with punctuation removed
     */
    public static String extractIdFromPropertyExpression(String expression)
    {
        return replace(expression, NON_WORD_PATTERN, "");
    }

    public static String replace(String input, Pattern pattern, String replacement)
    {
        return pattern.matcher(input).replaceAll(replacement);
    }
    
    /**
     * Looks for a label within the messages based on the id. If found, it is used, otherwise the name is converted to a
     * user presentable form.
     */
    public static String defaultLabel(String id, Messages messages, String propertyExpression)
    {
        String key = id + "-label";

        if (messages.contains(key))
            return messages.get(key);

        return toUserPresentable(extractIdFromPropertyExpression(InternalStringUtils.lastTerm(propertyExpression)));
    }
    
    /**
     * Capitalizes the string, and inserts a space before each upper case character (or sequence of upper case
     * characters). Thus "userId" becomes "User Id", etc. Also, converts underscore into space (and capitalizes the
     * following word), thus "user_id" also becomes "User Id".
     */
    public static String toUserPresentable(String id)
    {
        StringBuilder builder = new StringBuilder(id.length() * 2);

        char[] chars = id.toCharArray();
        boolean postSpace = true;
        boolean upcaseNext = true;

        for (char ch : chars)
        {
            if (upcaseNext)
            {
                builder.append(Character.toUpperCase(ch));
                upcaseNext = false;

                continue;
            }

            if (ch == '_')
            {
                builder.append(' ');
                upcaseNext = true;
                continue;
            }

            boolean upperCase = Character.isUpperCase(ch);

            if (upperCase && !postSpace)
                builder.append(' ');

            builder.append(ch);

            postSpace = upperCase;
        }

        return builder.toString();
    }
    
    /**
     * @since 5.3
     */
    public static AnnotationProvider toAnnotationProvider(final Class element)
    {
        return new AnnotationProvider()
        {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return annotationClass.cast(element.getAnnotation(annotationClass));
            }
        };
    }
    
    public static AnnotationProvider toAnnotationProvider(final Method element)
    {
        if (element == null)
            return NULL_ANNOTATION_PROVIDER;

        return new AnnotationProvider()
        {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return element.getAnnotation(annotationClass);
            }
        };
    }


}
