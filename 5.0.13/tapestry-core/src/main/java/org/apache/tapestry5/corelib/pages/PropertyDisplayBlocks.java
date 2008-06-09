// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Renderable;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PropertyOutputContext;

import java.text.DateFormat;
import java.util.Locale;

public class PropertyDisplayBlocks
{
    @Environmental
    private PropertyOutputContext context;

    @Inject
    private Locale locale;

    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

    public String getConvertedEnumValue()
    {
        Enum value = (Enum) context.getPropertyValue();

        if (value == null) return null;

        return TapestryInternalUtils.getLabelForEnum(context.getMessages(), value);
    }

    public DateFormat getDateFormat()
    {
        return dateFormat;
    }

    public PropertyOutputContext getContext()
    {
        return context;
    }

    public Renderable getPasswordRenderer()
    {
        return new Renderable()
        {
            public void render(MarkupWriter writer)
            {

                Object value = context.getPropertyValue();

                int length = value == null ? 0 : value.toString().length();

                for (int i = 0; i < length; i++) writer.write("*");
            }
        };
    }
}
