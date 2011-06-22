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

import java.util.List;
import java.util.Map;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;

public class JavaScriptStackSourceImpl implements JavaScriptStackSource
{
    private final Map<String, JavaScriptStack> configuration;

    public JavaScriptStackSourceImpl(Map<String, JavaScriptStack> configuration)
    {
        this.configuration = configuration;
    }

    public JavaScriptStack getStack(String name)
    {
        JavaScriptStack stack = configuration.get(name);

        if (stack == null)
            throw new UnknownValueException(String.format("No JavaScriptStack with name '%s'.", name),
                    new AvailableValues("Configured JavaScript stacks", configuration));

        return stack;
    }

    public List<String> getStackNames()
    {
        return F.flow(configuration.keySet()).sort().toList();
    }
}
