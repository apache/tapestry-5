// Copyright 2013 The Apache Software Foundation
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

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;

import java.util.List;

public class ReloadHelperImpl implements ReloadHelper
{
    private final AlertManager alertManager;

    private final boolean productionMode;

    private final List<Runnable> callbacks = CollectionFactory.newThreadSafeList();

    public ReloadHelperImpl(AlertManager alertManager, @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        this.alertManager = alertManager;
        this.productionMode = productionMode;
    }

    public void forceReload()
    {
        if (productionMode)
        {
            alertManager.error("Can not force a reload in production mode.");
            return;
        }

        for (Runnable c : callbacks)
        {
            c.run();
        }

        alertManager.info("Component classes, templates, and messages reloaded.");
    }

    public void addReloadCallback(Runnable callback)
    {
        assert callback != null;

        callbacks.add(callback);
    }
}
