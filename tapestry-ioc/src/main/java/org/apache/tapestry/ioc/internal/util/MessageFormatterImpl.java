// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.util;

import org.apache.tapestry.ioc.MessageFormatter;

/**
 *
 */
public class MessageFormatterImpl implements MessageFormatter
{
    private final String _format;

    public MessageFormatterImpl(String format)
    {
        _format = format;
    }

    public String format(Object... args)
    {
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];

            if (Throwable.class.isInstance(arg))
            {
                Throwable t = (Throwable) arg;
                String message = t.getMessage();

                args[i] = message != null ? message : t.getClass().getName();
            }
        }

        // Might be tempting to create a Formatter object and just keep reusing it ... but
        // Formatters are not threadsafe.

        return String.format(_format, args);
    }

}
