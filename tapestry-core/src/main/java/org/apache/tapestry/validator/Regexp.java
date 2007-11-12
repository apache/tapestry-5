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

package org.apache.tapestry.validator;

import org.apache.tapestry.*;
import static org.apache.tapestry.TapestryUtils.quote;
import org.apache.tapestry.ioc.MessageFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regexp implements Validator<Pattern, String>
{
    public Class<Pattern> getConstraintType()
    {
        return Pattern.class;
    }

    public String getMessageKey()
    {
        return "regexp";
    }

    public Class<String> getValueType()
    {
        return String.class;
    }

    public boolean invokeIfBlank()
    {
        return false;
    }

    private String buildMessage(MessageFormatter formatter, Field field, Pattern constraintValue)
    {
        return formatter.format(constraintValue.toString(), field.getLabel());
    }

    public void render(Field field, Pattern constraintValue, MessageFormatter formatter,
                       MarkupWriter writer, PageRenderSupport pageRenderSupport)
    {
        pageRenderSupport.addScript(
                "Tapestry.Field.regexp('%s', %s, %s);",
                field.getClientId(),
                quote(constraintValue.pattern()),
                quote(buildMessage(formatter, field, constraintValue)));

    }

    public void validate(Field field, Pattern constraintValue, MessageFormatter formatter,
                         String value) throws ValidationException
    {
        Matcher matcher = constraintValue.matcher(value);

        if (!matcher.matches())
            throw new ValidationException(buildMessage(formatter, field, constraintValue));
    }

}