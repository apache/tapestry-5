// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services.meta;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;

/**
 * Service that makes it easy to identify a class annotation and use its presence, or the value of an attribute,
 * to set a meta-data key. The configuration map class annotation types to corresponding extractors who will be invoked
 * when the annotation is present. Most commonly, a {@link FixedExtractor} is used to set a fixed value to a fixed key,
 * triggered by the presence of the corresponding annotation.
 *
 * @since 5.2.0
 */
@UsesMappedConfiguration(key = Class.class, value = MetaDataExtractor.class)
public interface MetaWorker
{
    /**
     * Returns the worker that performs transformations (in 5.2, MetaWorker implemented ComponentClassTransformWorker).
     *
     * @return worker that implements the meta data analysis
     */
    ComponentClassTransformWorker2 getWorker();
}
