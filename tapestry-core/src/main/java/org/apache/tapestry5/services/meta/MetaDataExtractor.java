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

import java.lang.annotation.Annotation;

import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.MetaDataLocator;

/**
 * Used to extract a {@linkplain ComponentModel#getMeta(String) meta data value} from a component annotation. Instances
 * of this interface are contributed into the MetaWorker service.
 * 
 * @since 5.2.0
 * @see MetaWorker
 * @see MetaDataLocator
 */
public interface MetaDataExtractor<T extends Annotation>
{
    /**
     * Invoked on the extractor to extract the appropriate value for the annotation and
     * {@linkplain MutableComponentModel#setMeta(String, String) set the meta data on the model}.
     * 
     * @param model
     *            on which to set meta data
     * @param annotation
     *            class annotation, from whose attributes specific data may be extracted
     */
    void extractMetaData(MutableComponentModel model, T annotation);
}
