//  Copyright 2008-2013 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Manages a per-thread OperationTracker using a ThreadLocal.
 */
public class PerThreadOperationTracker implements OperationTracker
{
    private final Logger logger;

    private final ThreadLocal<OperationTrackerImpl> perThread = new ThreadLocal<OperationTrackerImpl>()
    {
        @Override
        protected OperationTrackerImpl initialValue()
        {
            return new OperationTrackerImpl(logger);
        }
    };

    public PerThreadOperationTracker(Logger logger)
    {
        this.logger = logger;
    }

    OperationTracker get()
    {
        return perThread.get();
    }

    void cleanup()
    {
        if (perThread.get().isEmpty()) perThread.remove();
    }

    @Override
    public void run(String description, Runnable operation)
    {
        try
        {
            get().run(description, operation);
        } finally
        {
            cleanup();
        }
    }

    @Override
    public <T> T invoke(String description, Invokable<T> operation)
    {
        try
        {
            return get().invoke(description, operation);
        } finally
        {
            cleanup();
        }
    }

    @Override
    public <T> T perform(String description, IOOperation<T> operation) throws IOException
    {
        try
        {
            return get().perform(description, operation);
        } finally
        {
            cleanup();
        }
    }
}
