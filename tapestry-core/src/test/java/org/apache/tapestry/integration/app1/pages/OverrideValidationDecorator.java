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


package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.integration.app1.ChattyValidationDecorator;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.Environment;

public class OverrideValidationDecorator
{
    private String _value;

    private long _requiredValue;

    @Inject
    private Environment _environment;

    void beginRender(MarkupWriter writer)
    {
        ValidationDecorator existing = _environment.peekRequired(ValidationDecorator.class);

        _environment.push(ValidationDecorator.class, new ChattyValidationDecorator(writer, existing));
    }

    void afterRender()
    {
        _environment.pop(ValidationDecorator.class);
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }

    public long getRequiredValue()
    {
        return _requiredValue;
    }

    public void setRequiredValue(long requiredValue)
    {
        _requiredValue = requiredValue;
    }
}
