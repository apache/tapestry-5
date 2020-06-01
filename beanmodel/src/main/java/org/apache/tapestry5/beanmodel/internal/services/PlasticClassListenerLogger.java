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

package org.apache.tapestry5.beanmodel.internal.services;

import org.apache.tapestry5.plastic.ClassType;
import org.apache.tapestry5.plastic.PlasticClassEvent;
import org.apache.tapestry5.plastic.PlasticClassListener;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class PlasticClassListenerLogger implements PlasticClassListener
{
    private final Logger logger;

    public PlasticClassListenerLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void classWillLoad(PlasticClassEvent event)
    {
        if (logger.isDebugEnabled())
        {
            Marker marker = MarkerFactory.getMarker(event.getPrimaryClassName());

            String extendedClassName = event.getType() == ClassType.PRIMARY ? event.getPrimaryClassName() : String
                    .format("%s (%s for %s)", event.getClassName(), event.getType(), event.getPrimaryClassName());

            logger.debug(marker,
                    String.format("Loading class %s:\n%s", extendedClassName, event.getDissasembledBytecode()));
        }
    }
}
