// Copyright 2010 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ValueEncoderSource;

public class SelectModelFactoryImpl implements SelectModelFactory
{
    private final PropertyAccess propertyAccess;
    private final ValueEncoderSource valueEncoderSource;

    public SelectModelFactoryImpl(final PropertyAccess propertyAccess,
            final ValueEncoderSource valueEncoderSource)
    {
        super();
        this.propertyAccess = propertyAccess;
        this.valueEncoderSource = valueEncoderSource;
    }

    @SuppressWarnings("unchecked")
    public SelectModel create(final List<?> objects, final String labelProperty)
    {
        final List<OptionModel> options = CollectionFactory.newList();

        for (final Object object : objects)
        {
            final ClassPropertyAdapter classPropertyAdapter = this.propertyAccess
                    .getAdapter(object);

            final PropertyAdapter propertyAdapter = classPropertyAdapter.getPropertyAdapter(labelProperty);

            final ValueEncoder encoder = this.valueEncoderSource.getValueEncoder(propertyAdapter.getType());

            final Object label = propertyAdapter.get(object);

            options.add(new OptionModelImpl(encoder.toClient(label), object));

        }

        return new SelectModelImpl(null, options);
    }
}
