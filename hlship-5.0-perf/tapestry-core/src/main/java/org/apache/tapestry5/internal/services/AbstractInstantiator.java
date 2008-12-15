// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.model.ComponentModel;

/**
 * Abstract base class for instantiators; for each component, a new subclass is created at runtime.
 */
public abstract class AbstractInstantiator implements Instantiator
{
    private final ComponentModel model;

    private final String description;

    public AbstractInstantiator(ComponentModel model, String description)
    {
        this.model = model;
        this.description = description;
    }

    @Override
    public String toString()
    {
        return description;
    }

    public ComponentModel getModel()
    {
        return model;
    }
}
