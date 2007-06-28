// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.Set;

import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.services.ExceptionTracker;

@Scope(PERTHREAD_SCOPE)
public class ExceptionTrackerImpl implements ExceptionTracker
{
    private final Set<Throwable> _exceptions = newSet();

    public boolean exceptionLogged(Throwable exception)
    {
        boolean result = _exceptions.contains(exception);

        _exceptions.add(exception);

        return result;
    }
}
