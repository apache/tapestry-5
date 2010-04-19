// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import java.util.Map;

import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.javascript.JavascriptStack;
import org.apache.tapestry5.services.javascript.JavascriptStackSource;

public class JavascriptStackSourceImpl implements JavascriptStackSource
{
    private final Map<String, JavascriptStack> configuration;

    public JavascriptStackSourceImpl(Map<String, JavascriptStack> configuration)
    {
        this.configuration = configuration;
    }

    public JavascriptStack getStack(String name)
    {
        JavascriptStack stack = configuration.get(name);

        if (stack == null)
            throw new UnknownValueException(String.format("No JavascriptStack with name '%s'.", name),
                    new AvailableValues("JavaScript stacks", configuration));

        return stack;
    }

}
