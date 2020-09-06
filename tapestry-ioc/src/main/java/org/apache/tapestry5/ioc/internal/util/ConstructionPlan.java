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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;

import java.util.List;

/**
 * Encapsulates the initial construction of an object instance, followed by a series
 * {@link InitializationPlan}s to initialize fields and invoke other methods of the constructed object.
 *
 * @since 5.3
 */
public class ConstructionPlan<T> implements ObjectCreator<T>
{
    private final OperationTracker tracker;

    private final String description;

    private final Invokable<T> instanceConstructor;

    private List<InitializationPlan> initializationPlans;

    public ConstructionPlan(OperationTracker tracker, String description, Invokable<T> instanceConstructor)
    {
        this.tracker = tracker;
        this.description = description;
        this.instanceConstructor = instanceConstructor;
    }

    public ConstructionPlan add(InitializationPlan plan)
    {
        if (initializationPlans == null)
        {
            initializationPlans = CollectionFactory.newList();
        }

        initializationPlans.add(plan);

        return this;
    }

    @Override
    public T createObject()
    {
        T result = tracker.invoke(description, instanceConstructor);

        if (initializationPlans != null)
        {
            executeInitializationPLans(result);
        }

        return result;
    }

    private void executeInitializationPLans(final T newInstance)
    {
        for (final InitializationPlan<T> plan : initializationPlans)
        {
            tracker.run(plan.getDescription(), new Runnable()
            {
                @Override
                public void run()
                {
                    plan.initialize(newInstance);
                }
            });
        }
    }
}
