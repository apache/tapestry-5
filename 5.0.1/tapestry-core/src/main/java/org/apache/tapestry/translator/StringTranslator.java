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

package org.apache.tapestry.translator;

import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.ioc.Messages;

public class StringTranslator implements Translator<String>
{
    /** Returns the client value (or the empty string, if the client value is null). */
    public String parseClient(String clientValue, Messages messages) throws ValidationException
    {
        return clientValue == null ? "" : clientValue;
    }

    /** Returns the value, or the empty string if value is null. */
    public String toClient(String value)
    {
        return value == null ? "" : value;
    }

}
