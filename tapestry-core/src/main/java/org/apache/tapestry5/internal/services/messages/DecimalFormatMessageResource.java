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

import org.apache.tapestry5.internal.util.VirtualResource;
import org.apache.tapestry5.ioc.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Provides a number of symbols related to formatting numbers; by exposing these in the global message catalog,
 * they are available to the client (via the "core/messages" module).
 *
 * @since 5.4
 */
public class DecimalFormatMessageResource extends VirtualResource
{
    private final Locale locale;

    public DecimalFormatMessageResource()
    {
        this(null);
    }

    DecimalFormatMessageResource(Locale locale)
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
        return String.format("<Virtual DecimalFormat symbols for locale %s>", locale == null ? "(none)" : locale);
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
        return new DecimalFormatMessageResource(locale);
    }

    @Override
    public InputStream openStream() throws IOException
    {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);

        StringBuilder builder = new StringBuilder(200);

        write(builder, "group", symbols.getGroupingSeparator());
        write(builder, "minus", symbols.getMinusSign());
        write(builder, "decimal", symbols.getDecimalSeparator());

        return toInputStream(builder.toString());
    }

    private void write(StringBuilder builder, String name, char value)
    {
        builder.append("decimal-symbols.").append(name).append("=").append(value).append("\n");
    }
}
