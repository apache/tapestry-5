// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ajax;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;

import java.util.Map;

/**
 * A mapping from <em>client-side zone ids</em> to objects that can render the content for that zone on the client. An
 * event handler method may instantiate an instance and chain together a series of calls to {@link #add(String,
 * Object)}, and return the final result.
 * <p/>
 * Remember that client-side element ids may not match server-side component ids, especially once Ajax is added to the
 * mix. Because of this, it is highly recommended that the client-side logic gather the actual component ids and include
 * those in the Ajax request, to ensure that the server generates updates that the client can process. Better yet, use
 * the Zone's id parameter to lock down the zone's id to a known, predictable value.
 *
 * @since 5.1.0.1
 */
public class MultiZoneUpdate
{
    private final MultiZoneUpdate parent;

    private final String zoneId;

    private final Object renderer;

    public MultiZoneUpdate(String zoneId, Object renderer)
    {
        this(zoneId, renderer, null);
    }

    private MultiZoneUpdate(String zoneId, Object renderer, MultiZoneUpdate parent)
    {
        this.zoneId = Defense.notBlank(zoneId, "zoneId");
        this.renderer = Defense.notNull(renderer, "renderer");

        this.parent = parent;
    }

    /**
     * Returns a <strong>new</strong> MultiZoneUpdate reflecting the mapping from the indicated zone to an object that
     * will render the content for that zone.
     *
     * @param zoneId   client id of zone to update
     * @param renderer object that can provide the content for the zone
     * @return new MultiZoneUpdate
     */
    public MultiZoneUpdate add(String zoneId, Object renderer)
    {
        return new MultiZoneUpdate(zoneId, renderer, this);
    }

    /**
     * Returns a mapping from client zone id to renderer object for that zone.
     *
     * @return string to renderer map
     */
    public Map<String, Object> getZoneToRenderMap()
    {
        Map<String, Object> result = CollectionFactory.newMap();

        MultiZoneUpdate cursor = this;

        while (cursor != null)
        {
            result.put(cursor.zoneId, cursor.renderer);

            cursor = cursor.parent;
        }

        return result;
    }

    @Override
    public String toString()
    {
        return String.format("MultiZoneUpdate[%s]", getZoneToRenderMap());
    }
}

