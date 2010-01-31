// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.MethodInvocationResult;

/**
 * Implementation of {@link MethodInvocationResult} for failed
 * invocations (where a checked exception was thrown).
 * 
 * @since 5.2.0
 */
public class MethodInvocationFailResult implements MethodInvocationResult
{
    private final Throwable thrown;

    public MethodInvocationFailResult(Throwable thrown)
    {
        this.thrown = thrown;
    }

    public Object getReturnValue()
    {
        return null;
    }

    public <T extends Throwable> T getThrown(Class<T> throwableClass)
    {
        if (throwableClass.isInstance(thrown))
            return throwableClass.cast(thrown);

        return null;
    }

    public boolean isFail()
    {
        return true;
    }

    public void rethrow()
    {
        throw new RuntimeException(thrown);
    }
}
