// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1;

import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

public class Stuff
{
    public final String uuid = UUID.randomUUID().toString();

    public final String name;

    public final List<Stuff> children = CollectionFactory.newList();

    public Stuff(String name)
    {
        this.name = name;
    }

    public Stuff addChildrenNamed(String... names)
    {
        for (String name : names)
        {
            children.add(new Stuff(name));
        }

        return this;
    }

    public Stuff addChild(Stuff child)
    {
        children.add(child);

        return this;
    }

    public Stuff seek(String uuid)
    {
        if (this.uuid.equals(uuid))
            return this;

        for (Stuff child : children)
        {
            Stuff match = child.seek(uuid);

            if (match != null)
                return match;
        }

        return null;
    }
}
