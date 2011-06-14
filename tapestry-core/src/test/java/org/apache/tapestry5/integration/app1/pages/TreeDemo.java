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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.corelib.components.Tree;
import org.apache.tapestry5.integration.app1.Stuff;
import org.apache.tapestry5.integration.app1.StuffTreeModelAdapter;
import org.apache.tapestry5.tree.DefaultTreeModel;
import org.apache.tapestry5.tree.TreeModel;

public class TreeDemo
{
    @InjectComponent
    private Tree tree;

    private static final Stuff rootStuff = new Stuff("<root>");

    static
    {
        rootStuff.addChild(new Stuff("Pets").addChildrenNamed("Oscar", "Gromit", "Max", "Roger", "Cooper"));
        rootStuff.addChild(new Stuff("Games").addChild(
                new Stuff("Board Games").addChildrenNamed("Settlers of Catan", "Agricola", "Ra", "Risk", "Dvonn"))
                .addChild(new Stuff("Card Games").addChildrenNamed("Magic the Gathering", "Dominion", "Mu")));

        Stuff numbers = new Stuff("Numbers");

        for (int i = 0; i < 10000; i++)
        {
            numbers.addChild(new Stuff(Integer.toString(i)));
        }

        rootStuff.addChild(numbers);
    }

    public TreeModel<Stuff> getStuffModel()
    {
        ValueEncoder<Stuff> encoder = new ValueEncoder<Stuff>()
        {
            public String toClient(Stuff value)
            {
                return value.uuid;
            }

            public Stuff toValue(String clientValue)
            {
                return rootStuff.seek(clientValue);
            }
        };

        return new DefaultTreeModel<Stuff>(encoder, new StuffTreeModelAdapter(), rootStuff.children);
    }

    void onActionFromClear()
    {
        tree.clearExpansions();
    }
}
