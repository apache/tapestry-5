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

import org.apache.tapestry.ioc.IdMatcher;

/**
 * Used to match input values (as fully qualified ids) to a pattern. The pattern is split into two
 * glob match patterns at the last dot.
 * 
 * 
 */
public class IdMatcherImpl implements IdMatcher
{
    private final GlobPatternMatcher _moduleMatcher;

    private final GlobPatternMatcher _serviceMatcher;

    public IdMatcherImpl(String pattern)
    {
        int dotx = pattern.lastIndexOf('.');

        if (dotx < 0)
            throw new IllegalArgumentException(String.format(
                    "Pattern '%s' does not contain a '.' seperator character.",
                    pattern));

        _moduleMatcher = new GlobPatternMatcher(pattern.substring(0, dotx));
        _serviceMatcher = new GlobPatternMatcher(pattern.substring(dotx + 1));
    }

    public boolean matches(String id)
    {
        int dotx = id.lastIndexOf('.');

        if (dotx < 0)
            throw new IllegalArgumentException(String.format(
                    "Input id '%s' does not contain a '.' seperator character.",
                    id));

        String moduleId = id.substring(0, dotx);
        String serviceId = id.substring(dotx + 1);

        return _moduleMatcher.matches(moduleId) && _serviceMatcher.matches(serviceId);
    }
}
