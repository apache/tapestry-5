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

package org.apache.tapestry5.internal.translator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.services.FormSupport;

public class StringTranslator extends AbstractTranslator<String>
{
    public StringTranslator()
    {
        super("string", String.class, "a-string-is-a-string");
    }

    /**
     * Does nothing.
     */
    public void render(Field field, String message, MarkupWriter writer, FormSupport formSupport)
    {
    }

    /**
     * Passes the clientValue through unchanged.
     */
    public String parseClient(Field field, String clientValue, String message)
            throws ValidationException
    {
        return clientValue;
    }

    /**
     * Passes the value through unchanged.
     */
    public String toClient(String value)
    {
        return value;
    }
}
