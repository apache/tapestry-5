// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.Component;

/**
 * An object that can instantiate a component. This is used with transformed classes, in which the normal no-arguments
 * constructor has been replaced with a constructor with arguments; the instantiator will retain the necessary arguments
 * and pass them to the enhanced class' constructor.
 */
public interface Instantiator
{
    /**
     * Instantiates and returns a new instance of the desired class. Component classes are always modified so that they
     * implement {@link Component} (and often, other interfaces as well).
     */
    Component newInstance(InternalComponentResources resources);

    /**
     * Returns the model that defines the behavior of the component.
     */
    ComponentModel getModel();
}
