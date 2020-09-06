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

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.tree.DefaultTreeModel;
import org.apache.tapestry5.tree.TreeModel;

import java.util.List;
import java.util.UUID;

public class Stuff
{
    public final String uuid = UUID.randomUUID().toString();

    public final String name;

    public List<Stuff> children;

    public Stuff(String name)
    {
        this.name = name;
    }

    public Stuff addChildrenNamed(String... names)
    {
        for (String name : names)
        {
            addChild(new Stuff(name));
        }

        return this;
    }

    public Stuff addChild(Stuff child)
    {
        if (children == null)
        {
            children = CollectionFactory.newList();
        }

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

    public static final Stuff ROOT = new Stuff("<root>");

    static
    {
        ROOT.addChild(new Stuff("Pets").addChildrenNamed("Oscar", "Gromit", "Max", "Roger", "Cooper"));
        ROOT.addChild(new Stuff("Games").addChild(
                new Stuff("Board Games").addChildrenNamed("Settlers of Catan", "Agricola", "Ra", "Risk", "Dvonn"))
                .addChild(new Stuff("Card Games").addChildrenNamed("Magic the Gathering", "Dominion", "Mu")));

        Stuff numbers = new Stuff("Numbers");

        for (int i = 0; i < 10000; i++)
        {
            numbers.addChild(new Stuff(Integer.toString(i)));
        }

        ROOT.addChild(numbers);

        ROOT.addChild(new Stuff("Empty"));
        // Special case: appears as a folder, even with no children:
        ROOT.addChild(new Stuff("Empty Folder"));
    }

    public static TreeModel<Stuff> createTreeModel()
    {
        ValueEncoder<Stuff> encoder = new StuffValueEncoder();

        return new DefaultTreeModel<Stuff>(encoder, new StuffTreeModelAdapter(), Stuff.ROOT.children);
    }
}
