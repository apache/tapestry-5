// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.clojure;

import clojure.lang.Symbol;
import org.apache.tapestry5.clojure.MethodToFunctionSymbolMapper;

import java.lang.reflect.Method;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation that transforms a camelCased method-name to a clojure-style function name.
 */
public class DefaultMapper implements MethodToFunctionSymbolMapper
{
    @Override
    public Symbol mapMethod(String namespace, Method method)
    {
        return mapMethodName(namespace, method.getName());
    }

    private Symbol mapMethodName(String namespace, String name)
    {
        return Symbol.create(namespace, transformName(name));
    }

    private final Pattern transition = Pattern.compile("(\\p{Lower}\\p{Upper})");

    private String transformName(String name)
    {

        Matcher matcher = transition.matcher(name);

        StringBuilder builder = new StringBuilder();

        int lastx = 0;

        while (matcher.find())
        {
            MatchResult matchResult = matcher.toMatchResult();

            int start = matchResult.start();
            int end = matchResult.end();

            builder.append(name.substring(lastx, start + 1));
            builder.append('-');
            // TODO: An acronym (such as "URL") should not be lower cased here.
            builder.append(name.substring(end - 1, end).toLowerCase());

            lastx = end;
        }

        if (lastx == 0)
        {
            return name;
        }

        builder.append(name.substring(lastx));

        return builder.toString();
    }

}
