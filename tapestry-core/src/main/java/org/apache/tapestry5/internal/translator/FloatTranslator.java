// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ValidationException;

public class FloatTranslator extends DecimalNumberTranslator<Float>
{
    public FloatTranslator()
    {
        super("float", Float.class);
    }

    public String toClient(Float value)
    {
        return value.toString();
    }

    public Float parseClient(Field field, String clientValue, String message)
            throws ValidationException
    {
        try
        {
            return new Float(clientValue.trim());
        }
        catch (NumberFormatException ex)
        {
            throw new ValidationException(message);
        }
    }
}
