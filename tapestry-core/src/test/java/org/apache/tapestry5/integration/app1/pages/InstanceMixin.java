// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.integration.app1.components.Output;
import org.apache.tapestry5.integration.app1.mixins.Emphasis;

import java.sql.Date;
import java.text.DateFormat;
import java.text.Format;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class InstanceMixin
{
    @SuppressWarnings("unused")
    @Component(parameters =
            { "value=date2", "format=format", "test=showEmphasis" })
    @Mixins("Emphasis")
    private Output output2;

    @SuppressWarnings("unused")
    @Component(parameters =
            { "value=date3", "format=format", "test=showEmphasis" })
    @MixinClasses(Emphasis.class)
    private Output output3;

    @Retain
    private final Format format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);

    @Retain
    private final Date date1 = newDate(99, Calendar.JUNE, 13);

    @Retain
    private final Date date2 = newDate(101, Calendar.JULY, 15);

    @Retain
    private final Date date3 = newDate(105, Calendar.DECEMBER, 4);

    @Persist
    private boolean showEmphasis;

    public Format getFormat()
    {
        return format;
    }

    private static Date newDate(int yearSince1900, int month, int day)
    {
        return new Date(new GregorianCalendar(1900 + yearSince1900, month, day).getTimeInMillis());
    }

    public Date getDate1()
    {
        return date1;
    }

    public Date getDate2()
    {
        return date2;
    }

    public Date getDate3()
    {
        return date3;
    }

    public boolean getShowEmphasis()
    {
        return showEmphasis;
    }

    @OnEvent(component = "toggle")
    void toggle()
    {
        showEmphasis = !showEmphasis;
    }
}
