// Copyright 2013, 2014 The Apache Software Foundation
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

package org.apache.tapestry5.modules;

import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.internal.services.dashboard.DashboardManagerImpl;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.dashboard.DashboardManager;
import org.apache.tapestry5.services.dashboard.DashboardTab;

public class DashboardModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(DashboardManager.class, DashboardManagerImpl.class);
    }

    @Contribute(DashboardManager.class)
    public static void defaultTabs(OrderedConfiguration<DashboardTab> configuration)
    {
        configuration.add("Pages", new DashboardTab("Pages", "core/PageCatalog"));
        configuration.add("Services", new DashboardTab("Services", "core/ServiceStatus"));
        configuration.add("Libraries", new DashboardTab("ComponentLibraries", "core/ComponentLibraries"));
    }
}
