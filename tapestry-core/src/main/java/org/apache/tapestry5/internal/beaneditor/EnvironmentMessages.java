
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

import org.apache.tapestry5.commons.Messages;

/**
 * Holds the current (overrides) Messages object and override id for placemnt into the environment
 * by FieldValidatorDefaultSourceImpl so ValidationConstraintGenerator implementations have access
 * to the catalog if necessary.
 */
public class EnvironmentMessages
{

    private final Messages messages;
    private final String overrideId;

    public EnvironmentMessages(Messages messages, String overrideId)
    {
        this.messages = messages;
        this.overrideId = overrideId;
    }

    public Messages getMessages()
    {
        return messages;
    }

    public String getOverrideId()
    {
        return overrideId;
    }

}
