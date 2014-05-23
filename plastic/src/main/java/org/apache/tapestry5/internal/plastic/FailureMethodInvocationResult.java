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

public class FailureMethodInvocationResult implements MethodInvocationResult
{
    private final Throwable thrown;

    public FailureMethodInvocationResult(Throwable thrown)
    {
        assert thrown != null;

        this.thrown = thrown;
    }

    @Override
    public Object getReturnValue()
    {
        return null;
    }

    @Override
    public void rethrow()
    {
        throw new RuntimeException(thrown);
    }

    @Override
    public boolean didThrowCheckedException()
    {
        return true;
    }

    @Override
    public <T extends Throwable> T getCheckedException(Class<T> exceptionType)
    {
        if (exceptionType.isInstance(thrown))
            return exceptionType.cast(thrown);

        return null;
    }

}
