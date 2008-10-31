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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ServiceActivity;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.services.BeanModelSource;

import java.util.List;

/**
 * Page used to see the status of all services defined by the {@link Registry}.
 * <p/>
 * TODO: Add filters to control which services are displayed
 */
@ContentType("text/html")
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
    private Messages messages;

    @Property
    @Inject
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    {
        model = source.createDisplayModel(ServiceActivity.class, messages);

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
