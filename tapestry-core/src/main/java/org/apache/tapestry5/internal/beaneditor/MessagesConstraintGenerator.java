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

package org.apache.tapestry5.internal.beaneditor;

import org.apache.tapestry5.services.ValidationConstraintGenerator;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.PropertyEditContext;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Generates constraints from the containing component's property file.
 * Looks for a key in the form: propertyId-validate. 
 *
 */
public class MessagesConstraintGenerator implements ValidationConstraintGenerator
{

    private final Environment environment;
    private final Pattern splitPattern;

    public MessagesConstraintGenerator(final Environment environment) {
        this.environment = environment;
        splitPattern = Pattern.compile(ValidateAnnotationConstraintGenerator.VALIDATOR_PATTERN);
    }

    public List<String> buildConstraints(Class propertyType, AnnotationProvider annotationProvider)
    {
        EnvironmentMessages environmentMessages = environment.peek(EnvironmentMessages.class);
        if (environmentMessages == null) {
            return null;
        }

        String key = environmentMessages.getOverrideId() + "-validate";
        Messages m = environmentMessages.getMessages();
        if (!m.contains(key))
        {
            return null;
        }

        String result = m.get(key);
        if (InternalUtils.isBlank(result))
        {
            return null;
        }
        return Arrays.asList(splitPattern.split(result));
    }
}
