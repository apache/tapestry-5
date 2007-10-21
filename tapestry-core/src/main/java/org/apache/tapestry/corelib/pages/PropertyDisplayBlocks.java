// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.pages;

import java.text.DateFormat;
import java.util.Locale;

import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.PropertyOutputContext;

public class PropertyDisplayBlocks
{
    @Environmental
    private PropertyOutputContext _context;

    @Inject
    private Locale _locale;

    private final DateFormat _dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, _locale);

    public String getConvertedEnumValue()
    {
        Enum value = (Enum) _context.getPropertyValue();

        if (value == null) return null;

        return TapestryInternalUtils.getLabelForEnum(_context.getMessages(), value);
    }

    public DateFormat getDateFormat()
    {
        return _dateFormat;
    }

    public PropertyOutputContext getContext()
    {
        return _context;
    }
}
