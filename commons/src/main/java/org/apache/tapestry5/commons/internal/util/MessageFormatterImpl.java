// Copyright 2006-2013 The Apache Software Foundation
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

package org.apache.tapestry5.commons.internal.util;

import java.util.Locale;

import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.util.ExceptionUtils;


public class MessageFormatterImpl implements MessageFormatter
{
    private final String format;

    private final Locale locale;

    public MessageFormatterImpl(String format, Locale locale)
    {
        this.format = format;
        this.locale = locale;
    }

    @Override
    public String format(Object... args)
    {
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];

            if (Throwable.class.isInstance(arg))
            {
                args[i] = ExceptionUtils.toMessage((Throwable) arg);
            }
        }

        // Might be tempting to create a Formatter object and just keep reusing it ... but
        // Formatters are not threadsafe.

        return String.format(locale, format, args);
    }

    /**
     * Returns the underlying format string for this formatter.
     *
     * @since 5.4
     */
    @Override
    public String toString()
    {
        return format;
    }

}
