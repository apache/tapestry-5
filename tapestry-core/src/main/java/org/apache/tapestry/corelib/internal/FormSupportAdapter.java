// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.corelib.internal;

import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.Field;
import org.apache.tapestry.services.FormSupport;

/**
 * An implementation of {@link org.apache.tapestry.services.FormSupport} that delegates all behavior to another instance
 * of FormSupport. This allows some of the behavior to be overridden easily.
 */
public class FormSupportAdapter implements FormSupport
{
    private final FormSupport _delegate;

    public FormSupportAdapter(FormSupport delegate)
    {
        _delegate = delegate;
    }

    public String allocateControlName(String id)
    {
        return _delegate.allocateControlName(id);
    }

    public <T> void store(T component, ComponentAction<T> action)
    {
        _delegate.store(component, action);
    }

    public <T> void storeAndExecute(T component, ComponentAction<T> action)
    {
        _delegate.storeAndExecute(component, action);
    }

    public void defer(Runnable command)
    {
        _delegate.defer(command);
    }

    public void setEncodingType(String encodingType)
    {
        _delegate.setEncodingType(encodingType);
    }

    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        _delegate.addValidation(field, validationName, message, constraint);
    }

    public String getClientId()
    {
        return _delegate.getClientId();
    }
}
