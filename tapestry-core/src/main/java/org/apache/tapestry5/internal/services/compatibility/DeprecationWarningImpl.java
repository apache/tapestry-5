// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.compatibility;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentMessages;
import org.apache.tapestry5.services.ComponentTemplates;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.slf4j.Logger;

import java.util.Map;

public class DeprecationWarningImpl implements DeprecationWarning
{
    private final Logger logger;

    private final AlertManager alertManager;

    static class ParameterDeprecationKey
    {
        final String completeId, parameterName;

        ParameterDeprecationKey(String completeId, String parameterName)
        {
            this.completeId = completeId;
            this.parameterName = parameterName;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParameterDeprecationKey that = (ParameterDeprecationKey) o;

            if (!completeId.equals(that.completeId)) return false;
            if (!parameterName.equals(that.parameterName)) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = completeId.hashCode();
            result = 31 * result + parameterName.hashCode();
            return result;
        }
    }

    static class ParameterValueDeprecationKey
    {
        final String completeId, parameterName;
        final Object value;

        ParameterValueDeprecationKey(String completeId, String parameterName, Object value)
        {
            this.completeId = completeId;
            this.parameterName = parameterName;
            this.value = value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParameterValueDeprecationKey that = (ParameterValueDeprecationKey) o;

            if (!completeId.equals(that.completeId)) return false;
            if (!parameterName.equals(that.parameterName)) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = completeId.hashCode();
            result = 31 * result + parameterName.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

    // Really used as a set.
    private final Map<Object, Boolean> deprecations = CollectionFactory.newConcurrentMap();

    public DeprecationWarningImpl(Logger logger, AlertManager alertManager)
    {
        this.logger = logger;
        this.alertManager = alertManager;
    }

    public void componentParameter(ComponentResources resources, String parameterName, String message)
    {
        assert resources != null;
        assert InternalUtils.isNonBlank(parameterName);
        assert InternalUtils.isNonBlank(message);

        ParameterDeprecationKey key = new ParameterDeprecationKey(resources.getCompleteId(), parameterName);

        if (deprecations.containsKey(key))
        {
            return;
        }

        deprecations.put(key, true);

        logMessage(resources, parameterName, message);
    }

    public void ignoredComponentParameters(ComponentResources resources, String... parameterNames)
    {
        assert resources != null;

        for (String name : parameterNames)
        {

            if (resources.isBound(name))
            {
                componentParameter(resources, name, "This parameter is ignored and may be removed in a future release.");
            }
        }
    }

    public void componentParameterValue(ComponentResources resources, String parameterName, Object parameterValue, String message)
    {
        assert resources != null;
        assert InternalUtils.isNonBlank(parameterName);
        assert InternalUtils.isNonBlank(message);

        ParameterValueDeprecationKey key = new ParameterValueDeprecationKey(resources.getCompleteId(), parameterName, parameterValue);

        if (deprecations.containsKey(key))
        {
            return;
        }

        deprecations.put(key, true);

        logMessage(resources, parameterName, message);
    }

    private void logMessage(ComponentResources resources, String parameterName, String message)
    {
        String text = String.format("Component %s, parameter %s: %s\n(at %s)",
                resources.getCompleteId(),
                parameterName,
                message,
                resources.getLocation());

        logger.error(text);

        alertManager.warn(text);
    }

    public void setupClearDeprecationsWhenInvalidated(
            @ComponentClasses
            InvalidationEventHub componentClassesHub,
            @ComponentMessages
            InvalidationEventHub messagesHub,
            @ComponentTemplates
            InvalidationEventHub templatesHub)
    {
        componentClassesHub.clearOnInvalidation(deprecations);
        messagesHub.clearOnInvalidation(deprecations);
        templatesHub.clearOnInvalidation(deprecations);
    }


}
