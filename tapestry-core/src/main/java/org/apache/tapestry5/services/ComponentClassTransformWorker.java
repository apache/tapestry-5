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

package org.apache.tapestry5.services;

import org.apache.tapestry5.model.MutableComponentModel;

/**
 * Interface for a set of objects that can perform component class transformations. Implementations should be
 * multithreaded, ideally they should be stateless (all necessary state can be stored in the {@link
 * org.apache.tapestry5.services.ClassTransformation}).
 */
public interface ComponentClassTransformWorker
{
    /**
     * Invoked to perform a transformation on an as-yet unloaded component class, represented by the {@link
     * ClassTransformation} instance. In some cases, the worker may make changes to the component model -- for example,
     * a worker that deals with parameters may update the model to reflect those parameters.
     */
    void transform(ClassTransformation transformation, MutableComponentModel model);
}
