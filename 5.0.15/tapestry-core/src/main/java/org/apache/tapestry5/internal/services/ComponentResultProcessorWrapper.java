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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

import java.io.IOException;

/**
 * A wrapper around {@link ComponentEventResultProcessor} that encapsulates capturing the exception.
 */
public class ComponentResultProcessorWrapper implements ComponentEventCallback
{
    private boolean aborted;

    private IOException exception;

    private final ComponentEventResultProcessor processor;

    public ComponentResultProcessorWrapper(ComponentEventResultProcessor processor)
    {
        this.processor = processor;
    }

    public boolean handleResult(Object result)
    {
        try
        {
            processor.processResultValue(result);
        }
        catch (IOException ex)
        {
            exception = ex;
        }

        aborted = true;

        return true;
    }

    /**
     * Returns true if {@link org.apache.tapestry5.ComponentEventCallback#handleResult(Object)} was invoked, false
     * otherwise.
     *
     * @return true if the event was aborted
     * @throws IOException if {@link ComponentEventResultProcessor#processResultValue(Object)} threw an IOException
     */
    public boolean isAborted() throws IOException
    {
        if (exception != null) throw exception;

        return aborted;
    }
}
