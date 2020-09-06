// Copyright 2010-2013 The Apache Software Foundation
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

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.ValueLabelProvider;

import java.util.List;

public class SelectModelFactoryImpl implements SelectModelFactory
{
    private final PropertyAccess propertyAccess;

    private final ValueEncoderSource valueEncoderSource;

    private final ValueLabelProvider<Object> valueLabelProvider;

    public SelectModelFactoryImpl(PropertyAccess propertyAccess, ValueEncoderSource valueEncoderSource, ValueLabelProvider<Object> valueLabelProvider)
    {
        this.propertyAccess = propertyAccess;
        this.valueEncoderSource = valueEncoderSource;
        this.valueLabelProvider = valueLabelProvider;
    }


    public SelectModel create(List<?> objects, String labelProperty)
    {
        PropertyValueLabelProvider propertyValueLabelProvider = new PropertyValueLabelProvider(
                valueEncoderSource, propertyAccess, labelProperty);

        return createSelectModel(objects, propertyValueLabelProvider);
    }

    public SelectModel create(List<?> objects)
    {
        return createSelectModel(objects, valueLabelProvider);
    }

    private SelectModel createSelectModel(List<?> objects, ValueLabelProvider<Object> labelProvider)
    {
        final List<OptionModel> options = CollectionFactory.newList();

        for (Object object : objects)
        {
            String label = labelProvider.getLabel(object);

            options.add(new OptionModelImpl(label, object));
        }

        return new SelectModelImpl(null, options);
    }
}
