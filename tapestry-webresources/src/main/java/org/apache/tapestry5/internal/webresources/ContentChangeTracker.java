// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.webresources;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.services.assets.ResourceDependencies;

import java.io.IOException;
import java.util.Map;

/**
 * Manages a collection of Resources and can check to see if any resource's actual content has changed.
 *
 * @since 5.4
 */
public class ContentChangeTracker implements ResourceDependencies
{
    private final Map<Resource, Long> checksums = CollectionFactory.newMap();

    @Override
    public void addDependency(Resource dependency)
    {
        long checksum = ResourceTransformUtils.toChecksum(dependency);

        checksums.put(dependency, checksum);
    }

    /**
     * Checks all resources tracked by this instance and returns true if any resource's content has changed.
     *
     * @return true if a change has occurred
     */
    public boolean dirty() throws IOException
    {
        for (Map.Entry<Resource, Long> e : checksums.entrySet())
        {
            long current = ResourceTransformUtils.toChecksum(e.getKey());

            if (current != e.getValue().longValue())
            {
                return true;
            }
        }

        return false;
    }


}
