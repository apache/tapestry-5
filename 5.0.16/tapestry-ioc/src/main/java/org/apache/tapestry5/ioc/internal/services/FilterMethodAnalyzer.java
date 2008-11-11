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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.MethodSignature;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.services.PipelineBuilderImpl} to analyze service interface methods
 * against filter interface methods to find the position of the extra service parameter (in the filter method).
 */
public class FilterMethodAnalyzer
{
    private final Class serviceInterface;

    FilterMethodAnalyzer(Class serviceInterface)
    {
        this.serviceInterface = serviceInterface;
    }

    public int findServiceInterfacePosition(MethodSignature ms, MethodSignature fms)
    {
        if (ms.getReturnType() != fms.getReturnType()) return -1;

        if (!ms.getName().equals(fms.getName())) return -1;

        Class[] filterParameters = fms.getParameterTypes();
        int filterParameterCount = filterParameters.length;
        Class[] serviceParameters = ms.getParameterTypes();

        if (filterParameterCount != (serviceParameters.length + 1)) return -1;

        // TODO: check compatible exceptions!

        // This needs work; it assumes the first occurance of the service interface
        // in the filter interface method signature is the right match. That will suit
        // most of the time.

        boolean found = false;
        int result = -1;

        for (int i = 0; i < filterParameterCount; i++)
        {
            if (filterParameters[i] == serviceInterface)
            {
                result = i;
                found = true;
                break;
            }
        }

        if (!found) return -1;

        // Check that all the parameters before and after the service interface still
        // match.

        for (int i = 0; i < result; i++)
        {
            if (filterParameters[i] != serviceParameters[i]) return -1;
        }

        for (int i = result + 1; i < filterParameterCount; i++)
        {
            if (filterParameters[i] != serviceParameters[i - 1]) return -1;
        }

        return result;
    }

}
