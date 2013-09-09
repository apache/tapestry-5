// Copyright 2010, 2013 The Apache Software Foundation
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

package org.apache.tapestry5;

import java.util.List;

/**
 * Wrapper around a {@link ValidationTracker} that delegates all methods to the wrapped instance.
 * Subclasses will often override specific methods.
 * 
 * @since 5.2.0
 */
public class ValidationTrackerWrapper implements ValidationTracker
{
    private final ValidationTracker delegate;

    public ValidationTrackerWrapper(ValidationTracker delegate)
    {
        this.delegate = delegate;
    }

    public void clear()
    {
        delegate.clear();
    }

    public String getError(Field field)
    {
        return delegate.getError(field);
    }

    public List<String> getErrors()
    {
        return delegate.getErrors();
    }

    public List<String> getUnassociatedErrors()
    {
        return delegate.getUnassociatedErrors();
    }

    public boolean getHasErrors()
    {
        return delegate.getHasErrors();
    }

    public String getInput(Field field)
    {
        return delegate.getInput(field);
    }

    public boolean inError(Field field)
    {
        return delegate.inError(field);
    }

    public void recordError(Field field, String errorMessage)
    {
        delegate.recordError(field, errorMessage);
    }

    public void recordError(String errorMessage)
    {
        delegate.recordError(errorMessage);
    }

    public void recordInput(Field field, String input)
    {
        delegate.recordInput(field, input);
    }

    /** Returns the instance to which methods are delegated. */
    protected ValidationTracker getDelegate()
    {
        return delegate;
    }
}
