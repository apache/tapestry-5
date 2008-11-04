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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.services.FormSupport;

/**
 * An implementation of {@link org.apache.tapestry5.services.FormSupport} that delegates all behavior to another
 * instance of FormSupport. This allows some of the behavior to be overridden easily.
 */
public class FormSupportAdapter implements FormSupport
{
    private final FormSupport delegate;

    public FormSupportAdapter(FormSupport delegate)
    {
        this.delegate = delegate;
    }

    public String allocateControlName(String id)
    {
        return delegate.allocateControlName(id);
    }

    public <T> void store(T component, ComponentAction<T> action)
    {
        delegate.store(component, action);
    }

    public <T> void storeAndExecute(T component, ComponentAction<T> action)
    {
        delegate.storeAndExecute(component, action);
    }

    public void defer(Runnable command)
    {
        delegate.defer(command);
    }

    public void setEncodingType(String encodingType)
    {
        delegate.setEncodingType(encodingType);
    }

    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        delegate.addValidation(field, validationName, message, constraint);
    }

    public String getClientId()
    {
        return delegate.getClientId();
    }

    public boolean isClientValidationEnabled()
    {
        return delegate.isClientValidationEnabled();
    }

    public String getFormComponentId()
    {
        return delegate.getFormComponentId();
    }

    public String getFormValidationId()
    {
        return delegate.getFormValidationId();
    }
}
