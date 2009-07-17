// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal;

/**
 * Event associated with a parameter value changing, used by <code>ParameterChangeListener</code>s.
 * @since 5.2.0.0
 */
public class ParameterChangedEvent<T>
{
    private final String parameterName;
    private final T newValue;


    public ParameterChangedEvent(String parameterName, T newValue)
    {
        this.parameterName = parameterName;
        this.newValue = newValue;
    }

    /**
     * @return the name of the parameter that changed.
     */
    public String getParameterName()
    {
        return parameterName;
    }

    /**
     * @return the new value. May be null.
     */
    public T getNewValue()
    {
        return newValue;
    }

}
