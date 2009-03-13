// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import static org.easymock.EasyMock.reportMatcher;
import org.easymock.IArgumentMatcher;

/**
 * Special version of string equality used to compare two snippets of code. This is somewhat simpleminded (it certainly
 * doesn't understand about literal strings in quotes). It works by eliminating unecessary whitespace around curly
 * braces, then reducing all whitespace to a single space.
 */
public class CodeEq implements IArgumentMatcher
{
    private final String code;

    public CodeEq(String input)
    {
        code = strip(input);
    }

    public boolean matches(Object argument)
    {
        String string = (String) argument;
        String stripped = strip(string);

        return code.equals(stripped);
    }

    public void appendTo(StringBuffer buffer)
    {
        buffer.append("codeEq(");
        buffer.append(code);
        buffer.append(")");
    }

    public static String codeEq(String input)
    {
        reportMatcher(new CodeEq(input));

        return null;
    }

    static String strip(String input)
    {
        return input.trim().replaceAll("\\s*\\{\\s*", "{").replaceAll("\\s*\\}\\s*", "}")
                .replaceAll("\\s+", " ");
    }
}
