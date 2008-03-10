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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Asset;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.json.JSONObject;

import java.util.Formatter;

/**
 * Used during partial page renders to allocate ids and collect JavaScript initialization.
 */
public class PartialRenderPageRenderSupport implements PageRenderSupport
{
    private final StringBuilder _builder = new StringBuilder();

    private final Formatter _formatter = new Formatter(_builder);

    private boolean _dirty;

    private final IdAllocator _idAllocator;

    public PartialRenderPageRenderSupport(String namespace)
    {
        _idAllocator = new IdAllocator(namespace);
    }

    public String allocateClientId(String id)
    {
        return _idAllocator.allocateId(id);
    }

    public String allocateClientId(ComponentResources resources)
    {
        return allocateClientId(resources.getId());
    }

    /**
     * Does nothing.  Script links are only supported during full page renders, not partials (at this time).
     */
    public void addScriptLink(Asset... scriptAssets)
    {
    }

    /**
     * Does nothing.  Script links are only supported during full page renders, not partials (at this time).
     */
    public void addClasspathScriptLink(String... classpaths)
    {
    }

    /**
     * Does nothing.  Stylesheet links are only supported during full page renders, not partials (at this time).
     */
    public void addStylesheetLink(Asset stylesheet, String media)
    {
    }

    public void addScript(String format, Object... arguments)
    {
        _formatter.format(format, arguments);

        _dirty = true;
    }

    /**
     * Updates the reply with a "script" key, if any scripting has been collected via {@link #addScript(String,
     * Object[])} }.
     *
     * @param reply
     */
    public void update(JSONObject reply)
    {
        if (_dirty) reply.put("script", _builder.toString());
    }

}
