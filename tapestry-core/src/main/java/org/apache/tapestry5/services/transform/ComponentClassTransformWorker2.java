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

package org.apache.tapestry5.services.transform;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;

/**
 * Interface for a set of objects that can perform transformation of component classes. Implementations should
 * be thread safe and ideally stateless (all necessary state can be stored inside the {@link PlasticClass}).
 *
 * The ComponentClassTransformWorker service uses an ordered configuration of these works as a {@linkplain ChainBuilder
 * chain of command}.
 *
 * @see PlasticClass
 * @since 5.3
 */
@UsesOrderedConfiguration(ComponentClassTransformWorker2.class)
public interface ComponentClassTransformWorker2
{
    /**
     * Invoked to perform part of the transformation of the {@link PlasticClass}.
     *
     * @param plasticClass component class being transformed
     * @param support      additional utilities needed during the transformation
     * @param model        the model for the component being transformed
     */
    void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model);
}
