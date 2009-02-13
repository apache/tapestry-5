// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.PageElementFactory;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.ParameterModel;

import java.util.Map;

public class EmbeddedComponentAssemblerImpl implements EmbeddedComponentAssembler
{
    private final Map<String, ComponentModel> mixinIdToComponentModel = CollectionFactory.newCaseInsensitiveMap();

    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentModel componentModel;

    private final PageElementFactory elementFactory;

    private final Location location;

    private Map<String, Boolean> bound;

    interface BindingCreator
    {
        Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                           ComponentResources embeddedComponentResources, String defaultBindingPrefix);
    }

    public EmbeddedComponentAssemblerImpl(ComponentInstantiatorSource instantiatorSource,
                                          PageElementFactory elementFactory,
                                          String componentClassName,
                                          Location location)
    {
        this.instantiatorSource = instantiatorSource;
        this.elementFactory = elementFactory;
        this.location = location;

        componentModel = getModel(componentClassName);

        // Add the implementation mixins defined by the component model.

        for (String className : componentModel.getMixinClassNames())
        {
            addInstanceMixin(getModel(className));
        }

        // Instance mixins will be added later.
    }

    private ComponentModel getModel(String className)
    {
        return instantiatorSource.getInstantiator(className).getModel();
    }

    public void addInstanceMixin(ComponentModel mixinModel)
    {
        String mixinId = InternalUtils.lastTerm(mixinModel.getComponentClassName());

        // TODO: Check for conflicts here?

        mixinIdToComponentModel.put(mixinId, mixinModel);
    }

    public ParameterBinder createBinder(String parameterName, final String parameterValue, String defaultBindingPrefix)
    {
        BindingCreator creator = new BindingCreator()
        {
            public Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                                      ComponentResources embeddedComponentResources, String defaultBindingPrefix)
            {
                return elementFactory.newBinding(parameterName, loadingComponentResources, embeddedComponentResources,
                                                 defaultBindingPrefix, parameterValue, location);
            }
        };


        return createBinder(parameterName, defaultBindingPrefix, creator);
    }

    public ParameterBinder createBinder(String parameterName, final Binding binding)
    {
        BindingCreator creator = new BindingCreator()
        {
            public Binding newBinding(String parameterName, ComponentResources loadingComponentResources,
                                      ComponentResources embeddedComponentResources, String defaultBindingPrefix)
            {
                return binding;
            }
        };

        return createBinder(parameterName, null, creator);
    }


    private ParameterBinder createBinder(String parameterName, String defaultBindingPrefix,
                                         BindingCreator creator)
    {
        int dotx = parameterName.indexOf('.');

        if (dotx > 0)
            return createQualifiedParameterBinder(parameterName.substring(0, dotx),
                                                  parameterName.substring(dotx + 1),
                                                  defaultBindingPrefix,
                                                  creator);

        // OK, see if its a parameter of the component (that takes precedence).

        ParameterModel pmodel = componentModel.getParameterModel(parameterName);

        if (pmodel != null)
            return createBinder(null, parameterName, pmodel.getDefaultBindingPrefix(), creator);

        String informalMixinId = null;

        for (Map.Entry<String, ComponentModel> me : mixinIdToComponentModel.entrySet())
        {
            String mixinId = me.getKey();
            ComponentModel model = me.getValue();

            if (informalMixinId == null && model.getSupportsInformalParameters())
                informalMixinId = mixinId;

            pmodel = model.getParameterModel(parameterName);

            if (pmodel != null)
                return createBinder(mixinId, parameterName, pmodel.getDefaultBindingPrefix(), creator);
        }

        // OK, it doesn't match any formal parameter of the component or any mixin.

        // If neither the component nor any of its mixins supports informal parameters,
        // then return null to ignore the parameter.

        if (informalMixinId == null && !componentModel.getSupportsInformalParameters()) return null;

        // Add as an informal parameter either to a mixin (if an mixin supprting informatl parameters
        // was found) or to the component itself (otherwise).

        return createBinder(informalMixinId, parameterName, defaultBindingPrefix, creator);
    }

    private ParameterBinder createQualifiedParameterBinder(String mixinId, String parameterName,
                                                           String defaultBindingPrefix,
                                                           BindingCreator creator)
    {
        ComponentModel mixinModel = mixinIdToComponentModel.get(mixinId);

        if (mixinModel == null)
        {
            String message = String.format(
                    "Parameter '%s.%s' does not match any defined mixins for this component.  Available mixins: %s.",
                    mixinId, parameterName,
                    InternalUtils.sortedKeys(mixinIdToComponentModel));

            throw new TapestryException(message, location, null);
        }

        return createBinder(mixinId, mixinModel, parameterName, defaultBindingPrefix, creator);
    }

    private ParameterBinder createBinder(String mixinId,
                                         ComponentModel model,
                                         String parameterName,
                                         String defaultBindingPrefix,
                                         BindingCreator creator)
    {
        ParameterModel pmodel = model.getParameterModel(parameterName);

        // Ignore informal parameters for mixins that don't support them.

        if (pmodel == null && !model.getSupportsInformalParameters())
            return null;

        final String bindingPrefix = pmodel == null ? defaultBindingPrefix : pmodel.getDefaultBindingPrefix();

        return createBinder(mixinId, parameterName, bindingPrefix, creator);
    }

    private ParameterBinder createBinder(final String mixinId,
                                         final String parameterName,
                                         final String defaultBindingPrefix,
                                         final BindingCreator creator)
    {
        return new ParameterBinder()
        {
            public void bind(ComponentPageElement container, ComponentPageElement embedded)
            {
                Binding binding =
                        creator.newBinding(parameterName,
                                           container.getComponentResources(),
                                           embedded.getComponentResources(),
                                           defaultBindingPrefix);

                if (mixinId == null)
                    embedded.bindParameter(parameterName, binding);
                else
                    embedded.bindMixinParameter(mixinId, parameterName, binding);
            }
        };
    }

    public boolean isBound(String parameterName)
    {
        return InternalUtils.get(bound, parameterName) != null;
    }

    public void setBound(String parameterName)
    {
        if (bound == null)
            bound = CollectionFactory.newCaseInsensitiveMap();

        bound.put(parameterName, true);
    }
}
