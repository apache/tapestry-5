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

package org.apache.tapestry.ioc.internal;

import static org.apache.tapestry.ioc.internal.MatchType.ANY;
import static org.apache.tapestry.ioc.internal.MatchType.INFIX;
import static org.apache.tapestry.ioc.internal.MatchType.PREFIX;
import static org.apache.tapestry.ioc.internal.MatchType.SUFFIX;

public class GlobPatternMatcher
{
    private String _substring;

    private MatchType _type;

    public GlobPatternMatcher(String pattern)
    {
        analyze(pattern);
    }

    private void analyze(String pattern)
    {
        if (pattern.equals("*"))
        {
            _type = ANY;
            return;
        }

        boolean globPrefix = pattern.startsWith("*");
        boolean globSuffix = pattern.endsWith("*");

        if (globPrefix && globSuffix)
        {
            _substring = pattern.substring(1, pattern.length() - 1);
            _type = INFIX;
            return;
        }

        if (globPrefix)
        {
            _substring = pattern.substring(1);
            _type = SUFFIX;
            return;
        }

        if (globSuffix)
        {
            _substring = pattern.substring(0, pattern.length() - 1);
            _type = PREFIX;
            return;
        }

        _type = MatchType.EXACT;
        _substring = pattern;
    }

    public boolean matches(String input)
    {
        switch (_type)
        {
            case ANY:
                return true;

            case EXACT:

                return input.equalsIgnoreCase(_substring);

            case INFIX:

                return input.toLowerCase().contains(_substring.toLowerCase());

            case PREFIX:

                return input.regionMatches(true, 0, _substring, 0, _substring.length());

            default:

                return input.regionMatches(
                        true,
                        input.length() - _substring.length(),
                        _substring,
                        0,
                        _substring.length());
        }
    }
}
