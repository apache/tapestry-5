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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentClasses;
import org.apache.tapestry5.services.ComponentMessages;
import org.apache.tapestry5.services.ComponentTemplates;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.slf4j.Logger;

import java.util.Map;

public class DeprecationWarningImpl implements DeprecationWarning
{
    private final Logger logger;

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

    public DeprecationWarningImpl(Logger logger)
    {
        this.logger = logger;
    }

    @Override
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

        logger.error(String.format("Component %s, parameter %s. %s\n(at %s)",
                key.completeId,
                parameterName,
                message,
                resources.getLocation());
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
