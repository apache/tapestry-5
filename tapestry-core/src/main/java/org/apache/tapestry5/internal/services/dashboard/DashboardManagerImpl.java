// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.dashboard;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.dashboard.DashboardManager;
import org.apache.tapestry5.services.dashboard.DashboardTab;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DashboardManagerImpl implements DashboardManager
{
    private final ComponentSource componentSource;

    private final List<String> tabNames;

    private final Map<String, String> tab2PageName = CollectionFactory.newCaseInsensitiveMap();

    public DashboardManagerImpl(ComponentSource componentSource, List<DashboardTab> tabs)
    {
        this.componentSource = componentSource;

        List<String> tabNames = CollectionFactory.newList();

        for (DashboardTab tab : tabs)
        {
            tabNames.add(tab.tabName);

            tab2PageName.put(tab.tabName, tab.pageName);
        }

        this.tabNames = Collections.unmodifiableList(tabNames);
    }

    public List<String> getTabNames()
    {
        return tabNames;
    }

    public Block getTabContent(String tabName)
    {
        Component page = componentSource.getPage(tab2PageName.get(tabName));

        return page.getComponentResources().getBlock("content");
    }
}
