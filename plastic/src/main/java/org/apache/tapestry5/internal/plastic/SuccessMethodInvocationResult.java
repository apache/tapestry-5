// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.plastic.MethodInvocationResult;

/**
 * A successful method invocation; one that did not throw a checked exception.
 */
public class SuccessMethodInvocationResult implements MethodInvocationResult
{
    private final Object returnValue;

    public SuccessMethodInvocationResult(Object returnValue)
    {
        this.returnValue = returnValue;
    }

    @Override
    public Object getReturnValue()
    {
        return returnValue;
    }

    @Override
    public void rethrow()
    {
    }

    @Override
    public boolean didThrowCheckedException()
    {
        return false;
    }

    @Override
    public <T extends Throwable> T getCheckedException(Class<T> exceptionType)
    {
        return null;
    }
}
