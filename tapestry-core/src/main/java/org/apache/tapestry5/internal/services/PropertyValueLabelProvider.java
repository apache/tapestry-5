// Copyright 2013  The Apache Software Foundation
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

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.ValueLabelProvider;

/**
 * Provides a label from a property of the passed object.
 * 
 * @since 5.4
 */
public class PropertyValueLabelProvider implements ValueLabelProvider<Object>
{
    private final PropertyAccess propertyAccess;
    private final ValueEncoderSource valueEncoderSource;
    private final String labelProperty;

    public PropertyValueLabelProvider(ValueEncoderSource valueEncoderSource,
            PropertyAccess propertyAccess, String labelProperty)
    {
        this.valueEncoderSource = valueEncoderSource;
        this.propertyAccess = propertyAccess;
        this.labelProperty = labelProperty;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public String getLabel(Object object)
    {
        final ClassPropertyAdapter classPropertyAdapter = this.propertyAccess.getAdapter(object);

        final PropertyAdapter propertyAdapter = classPropertyAdapter
                .getPropertyAdapter(labelProperty);

        final ValueEncoder encoder = this.valueEncoderSource.getValueEncoder(propertyAdapter
                .getType());

        final Object label = propertyAdapter.get(object);

        return encoder.toClient(label);
    }
}
