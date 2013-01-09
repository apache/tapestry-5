// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.compatibility;

import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;

import java.util.EnumSet;
import java.util.Map;

public class CompatibilityImpl implements Compatibility
{
    private final EnumSet<Trait> traits;

    public CompatibilityImpl(Map<Trait, Boolean> configuration)
    {
        // Since the default in 5.4 is true, we can just remove those for which the value is false.

        traits = EnumSet.allOf(Trait.class);

        for (Map.Entry<Trait, Boolean> entry : configuration.entrySet())
        {
            if (entry.getValue().equals(Boolean.FALSE))
            {
                traits.remove(entry.getKey());
            }
        }
    }

    public boolean enabled(Trait trait)
    {
        assert trait != null;

        return traits.contains(trait);
    }
}
