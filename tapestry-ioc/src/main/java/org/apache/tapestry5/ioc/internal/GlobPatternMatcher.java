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

package org.apache.tapestry.ioc.internal;

import static org.apache.tapestry.ioc.internal.MatchType.*;

public class GlobPatternMatcher
{
    private String substring;

    private MatchType type;

    public GlobPatternMatcher(String pattern)
    {
        analyze(pattern);
    }

    private void analyze(String pattern)
    {
        if (pattern.equals("*"))
        {
            type = ANY;
            return;
        }

        boolean globPrefix = pattern.startsWith("*");
        boolean globSuffix = pattern.endsWith("*");

        if (globPrefix && globSuffix)
        {
            substring = pattern.substring(1, pattern.length() - 1);
            type = INFIX;
            return;
        }

        if (globPrefix)
        {
            substring = pattern.substring(1);
            type = SUFFIX;
            return;
        }

        if (globSuffix)
        {
            substring = pattern.substring(0, pattern.length() - 1);
            type = PREFIX;
            return;
        }

        type = MatchType.EXACT;
        substring = pattern;
    }

    public boolean matches(String input)
    {
        switch (type)
        {
            case ANY:
                return true;

            case EXACT:

                return input.equalsIgnoreCase(substring);

            case INFIX:

                return input.toLowerCase().contains(substring.toLowerCase());

            case PREFIX:

                return input.regionMatches(true, 0, substring, 0, substring.length());

            default:

                return input.regionMatches(
                        true,
                        input.length() - substring.length(),
                        substring,
                        0,
                        substring.length());
        }
    }
}
