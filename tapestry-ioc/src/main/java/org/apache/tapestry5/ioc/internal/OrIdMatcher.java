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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.IdMatcher;

import java.util.Collection;

/**
 * A wrapper around a collection of IdMatchers. A match occurs if <em>any</em> matcher matches.
 */
public final class OrIdMatcher implements IdMatcher
{
    private final IdMatcher[] matchers;

    public OrIdMatcher(Collection<IdMatcher> matchers)
    {
        this.matchers = matchers.toArray(new IdMatcher[matchers.size()]);
    }

    public boolean matches(String id)
    {
        for (IdMatcher m : matchers)
            if (m.matches(id)) return true;

        return false;
    }

}
