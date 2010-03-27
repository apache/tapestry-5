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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.ComponentEventCallback;

/**
 * Implementation of {@link ComponentEventCallback} that simply captures the result value.
 * 
 * @since 5.2.0
 */
public class CaptureResultCallback<T> implements ComponentEventCallback<T>
{
    private T result;

    public boolean handleResult(T result)
    {
        this.result = result;

        return true;
    }

    public T getResult()
    {
        return result;
    }

    public static <T> CaptureResultCallback<T> create()
    {
        return new CaptureResultCallback<T>();
    }
}
