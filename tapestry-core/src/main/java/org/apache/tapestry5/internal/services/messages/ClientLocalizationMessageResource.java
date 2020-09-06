// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.messages;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.util.VirtualResource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Provides a number of symbols related to client-side localization; by exposing these in the global message catalog,
 * they are available to the client (via the "t5/core/messages" module).
 *
 * @since 5.4
 */
public class ClientLocalizationMessageResource extends VirtualResource
{
    private final Locale locale;

    public ClientLocalizationMessageResource()
    {
        this(null);
    }

    ClientLocalizationMessageResource(Locale locale)
    {
        this.locale = locale;
    }

    @Override
    public Resource withExtension(String extension)
    {
        return this;
    }

    @Override
    public String getPath()
    {
        return String.format("<Client localization symbols for locale %s>", locale == null ? "(none)" : locale);
    }

    @Override
    public String getFile()
    {
        return null;
    }

    @Override
    public URL toURL()
    {
        return null;
    }

    @Override
    public Resource forLocale(Locale locale)
    {
        return new ClientLocalizationMessageResource(locale);
    }

    public InputStream openStream() throws IOException
    {
        DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance(locale);

        Map<String, Object> symbols = CollectionFactory.newMap();

        symbols.put("decimal-symbols.group", decimalSymbols.getGroupingSeparator());
        symbols.put("decimal-symbols.minus", decimalSymbols.getMinusSign());
        symbols.put("decimal-symbols.decimal", decimalSymbols.getDecimalSeparator());

        DateFormatSymbols dateSymbols = new DateFormatSymbols(locale);

        List<String> months = Arrays.asList(dateSymbols.getMonths()).subList(0, 12);

        // Comma-separated list, starting with January
        symbols.put("date-symbols.months", InternalUtils.join(months, ","));

        List<String> days = Arrays.asList(dateSymbols.getWeekdays()).subList(1, 8);

        // Comma-separated list, starting with Sunday
        symbols.put("date-symbols.days", InternalUtils.join(days, ","));

        Calendar c = Calendar.getInstance(locale);

        // First day of the week, usually 0 for Sunday (e.g., in the US) or 1 for Monday
        // (e.g., France).
        symbols.put("date-symbols.first-day", c.getFirstDayOfWeek() - 1);


        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Object> entry : symbols.entrySet())
        {
            write(builder, entry.getKey(), entry.getValue());
        }

        return toInputStream(builder.toString());
    }

    private void write(StringBuilder builder, String name, Object value)
    {
        builder.append(name).append('=').append(value).append('\n');
    }
}
