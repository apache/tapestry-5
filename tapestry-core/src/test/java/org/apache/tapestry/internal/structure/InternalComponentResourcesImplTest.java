// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.Binding;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.ParameterModel;
import org.apache.tapestry.runtime.Component;
import org.testng.annotations.Test;

public class InternalComponentResourcesImplTest extends InternalBaseTestCase
{
    @Test
    public void render_informal_parameters_no_bindings()
    {
        ComponentPageElement element = mockComponentPageElement();
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        MarkupWriter writer = mockMarkupWriter();
        TypeCoercer coercer = mockTypeCoercer();
        ComponentModel model = mockComponentModel();

        train_getModel(ins, model);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(element, null,
                ins, coercer, null);

        resources.renderInformalParameters(writer);

        verify();
    }

    @Test
    public void render_informal_parameters_skips_formal_parameters()
    {
        ComponentPageElement element = mockComponentPageElement();
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        MarkupWriter writer = mockMarkupWriter();
        TypeCoercer coercer = mockTypeCoercer();
        ComponentModel model = mockComponentModel();
        ParameterModel pmodel = mockParameterModel();
        Binding binding = mockBinding();

        train_getModel(ins, model);

        train_getParameterModel(model, "fred", pmodel);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(element, null,
                ins, coercer, null);

        resources.bindParameter("fred", binding);

        resources.renderInformalParameters(writer);

        verify();
    }

    @Test
    public void render_an_informal_parameter()
    {
        ComponentPageElement element = mockComponentPageElement();
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        MarkupWriter writer = mockMarkupWriter();
        TypeCoercer coercer = mockTypeCoercer();
        ComponentModel model = mockComponentModel();
        Binding binding = mockBinding();
        Object rawValue = new Object();
        String convertedValue = "*converted*";

        train_getModel(ins, model);

        train_getParameterModel(model, "fred", null);

        train_get(binding, rawValue);

        train_coerce(coercer, rawValue, String.class, convertedValue);

        writer.attributes("fred", convertedValue);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(element, null,
                ins, coercer, null);

        resources.bindParameter("fred", binding);

        resources.renderInformalParameters(writer);

        verify();
    }
}
