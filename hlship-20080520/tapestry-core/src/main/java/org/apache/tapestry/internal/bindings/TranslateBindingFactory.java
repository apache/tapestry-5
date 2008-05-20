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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.TranslatorSource;

/**
 * Interprets the binding expression as the name of a {@link Translator} provided by the {@link TranslatorSource}.
 */
public class TranslateBindingFactory implements BindingFactory
{
    private final TranslatorSource source;

    public TranslateBindingFactory(TranslatorSource source)
    {
        this.source = source;
    }

    public Binding newBinding(String description, ComponentResources container,
                              ComponentResources component, String expression, Location location)
    {
        Translator translator = source.get(expression);

        return new LiteralBinding(description, translator, location);
    }

}
