// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.Orderer;

class OrderedConfigurationOverride<T>
{
    private final Orderer<T> orderer;

    private final String id;

    private final T replacementObject;

    private final String[] constraints;

    private final ContributionDef contribDef;

    OrderedConfigurationOverride(Orderer<T> orderer, String id, T replacementObject, String[] constraints,
                                 ContributionDef contribDef)
    {
        this.orderer = orderer;
        this.id = id;
        this.replacementObject = replacementObject;
        this.constraints = constraints;
        this.contribDef = contribDef;
    }

    void apply()
    {
        try
        {
            orderer.override(id, replacementObject, constraints);
        }
        catch (Exception ex)
        {
            String message = String.format("Failure processing override from %s: %s",
                                           contribDef,
                                           InternalUtils.toMessage(ex));

            throw new RuntimeException(message, ex);
        }
    }

    public ContributionDef getContribDef()
    {
        return contribDef;
    }
}
