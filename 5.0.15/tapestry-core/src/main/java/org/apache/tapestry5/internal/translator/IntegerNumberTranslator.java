//  Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.services.FormSupport;

/**
 * Base class for all non-decimal numeric types.
 */
public abstract class IntegerNumberTranslator<T> extends AbstractTranslator<T>
{
    protected IntegerNumberTranslator(String name, Class<T> type)
    {
        super(name, type, "integer-format-exception");
    }

    public void render(Field field, String message, MarkupWriter writer, FormSupport formSupport)
    {
        formSupport.addValidation(field, "integernumber", message, null);
    }
}
