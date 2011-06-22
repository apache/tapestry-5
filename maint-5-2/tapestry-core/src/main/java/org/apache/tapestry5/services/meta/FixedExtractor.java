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

import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;

/**
 * Implementation of {@link MetaDataExtractor} that is used to set a fixed
 * value for a fixed meta-data key, when a given annotation is present.
 * 
 * @since 5.2.0
 */
public class FixedExtractor<T extends Annotation> implements MetaDataExtractor<T>
{
    private final String key;

    private final String value;

    /** Defaults the value to "true". */
    public FixedExtractor(String key)
    {
        this(key, "true");
    }

    public FixedExtractor(String key, String value)
    {
        assert InternalUtils.isNonBlank(key);
        this.key = key;
        assert InternalUtils.isNonBlank(value);
        this.value = value;
    }

    public void extractMetaData(MutableComponentModel model, T annotation)
    {
        model.setMeta(key, value);
    }

}
