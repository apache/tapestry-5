// Copyright 2011-2013 The Apache Software Foundation
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

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.event.InvalidationEventHubImpl;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

public class InternalComponentInvalidationEventHubImpl extends InvalidationEventHubImpl implements
        InternalComponentInvalidationEventHub
{
    public InternalComponentInvalidationEventHubImpl(@Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                                     boolean productionMode, Logger logger)
    {
        super(productionMode, logger);
    }

    @PostInjection
    public void setupReload(ReloadHelper helper)
    {
        helper.addReloadCallback(new Runnable()
        {
            public void run()
            {
                fireInvalidationEvent();
            }
        });
    }

    public void classInControlledPackageHasChanged()
    {
        fireInvalidationEvent();
    }
}
