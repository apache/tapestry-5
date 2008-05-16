// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.pages;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotation.Meta;
import org.apache.tapestry.annotation.Property;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.annotation.Inject;
import org.apache.tapestry.ioc.services.ServiceActivity;
import org.apache.tapestry.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry.services.BeanModelSource;

import java.util.List;

/**
 * Page used to see the status of all services defined by the {@link Registry}.
 * <p/>
 * TODO: Add filters to control which services are displayed.
 * <p/>
 * TODO: Disable this page if in production mode (or not, as it does no harm).
 */
@Meta("tapestry.response-content-type=text/html")
public class ServiceStatus
{
    @Inject
    private ServiceActivityScoreboard scoreboard;

    @Property
    private List<ServiceActivity> activity;

    @Property
    private ServiceActivity row;

    @Inject
    private BeanModelSource source;

    @Property
    private final BeanModel model;

    @Inject
    private ComponentResources resources;

    {
        model = source.create(ServiceActivity.class, false, resources);

        model.add("serviceInterface", null);

        // There's no line number information for interfaces, so we'll reorder the
        // propreties manually.

        model.reorder("serviceId", "serviceInterface", "scope", "status");
    }

    void setupRender()
    {
        activity = scoreboard.getServiceActivity();
    }
}
