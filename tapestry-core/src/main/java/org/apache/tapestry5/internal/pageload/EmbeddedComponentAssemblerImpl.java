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

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;

import java.util.Locale;
import java.util.Map;

public class EmbeddedComponentAssemblerImpl implements EmbeddedComponentAssembler
{
    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentAssemblerSource assemblerSource;

    private final Locale locale;

    private final ComponentModel componentModel;

    private final Location location;

    private final Map<String, Instantiator> mixinIdToInstantiator = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Maps parameter names (both simple, and qualified with the mixin id) to the corresponding QualifiedParameterName.
     */
    private final Map<String, ParameterBinder> parameterNameToBinder = CollectionFactory.newCaseInsensitiveMap();

    // The id of the mixin to receive informal parameters.  If null, the component itself recieves them.
    // If the component doesn't support them, they are quietly dropped.

    private final String informalParametersMixinId;

    private Map<String, Boolean> bound;

    /**
     * @param assemblerSource
     * @param instantiatorSource     used to access component models
     * @param componentClassResolver used to convert mixin types to component models
     * @param componentClassName     class name of embedded component
     * @param locale
     * @param embeddedModel          embedded model (may be null for components defined in the template)
     * @param templateMixins         list of mixins from the t:mixins element (possibly null)
     * @param location               location of components element in its container's template
     */
    public EmbeddedComponentAssemblerImpl(ComponentAssemblerSource assemblerSource,
                                          ComponentInstantiatorSource instantiatorSource,
                                          ComponentClassResolver componentClassResolver,
                                          String componentClassName,
                                          Locale locale,
                                          EmbeddedComponentModel embeddedModel,
                                          String templateMixins,
                                          Location location)
    {
        this.assemblerSource = assemblerSource;
        this.instantiatorSource = instantiatorSource;
        this.locale = locale;
        this.location = location;

        componentModel = getModel(componentClassName);

        // Add the implementation mixins defined by the component model.

        for (String className : componentModel.getMixinClassNames())
        {
            addMixin(className);
        }

        // If there's an embedded model (i.e., there was an @Component annotation)
        // then it may define some mixins.

        if (embeddedModel != null)
        {
            for (String className : embeddedModel.getMixinClassNames())
            {
                addMixin(className);
            }
        }

        // And the template may include a t:mixins element to define yet more mixin.

        for (String mixinType : TapestryInternalUtils.splitAtCommas(templateMixins))
        {
            String className = componentClassResolver.resolveMixinTypeToClassName(mixinType);

            addMixin(className);
        }

        informalParametersMixinId = prescanMixins();

    }

    private String prescanMixins()
    {
        // Mixin id found to support informal parameters

        String supportsInformals = null;

        for (Map.Entry<String, Instantiator> entry : mixinIdToInstantiator.entrySet())
        {
            String mixinId = entry.getKey();
            ComponentModel mixinModel = entry.getValue().getModel();

            updateParameterNameToQualified(mixinId, mixinModel);

            if (supportsInformals == null && mixinModel.getSupportsInformalParameters())
                supportsInformals = mixinId;
        }

        // The component comes last and overwrites simple names from the others.

        updateParameterNameToQualified(null, componentModel);

        return supportsInformals;
    }

    private void updateParameterNameToQualified(String mixinId, ComponentModel model)
    {
        for (String parameterName : model.getParameterNames())
        {
            String defaultBindingPrefix = model.getParameterModel(parameterName).getDefaultBindingPrefix();

            ParameterBinderImpl binder = new ParameterBinderImpl(mixinId, parameterName, defaultBindingPrefix);

            parameterNameToBinder.put(parameterName,
                                      binder);

            if (mixinId != null)
                parameterNameToBinder.put(mixinId + "." + parameterName, binder);
        }
    }

    private void addMixin(String className)
    {
        Instantiator mixinInstantiator = instantiatorSource.getInstantiator(className);

        String mixinId = InternalUtils.lastTerm(className);

        if (mixinIdToInstantiator.containsKey(mixinId))
            throw new TapestryException(
                    String.format("Mixins applied to a component must be unique. Mixin '%s' has already been applied.",
                                  mixinId),
                    location, null);


        mixinIdToInstantiator.put(mixinId, mixinInstantiator);
    }

    private ComponentModel getModel(String className)
    {
        return instantiatorSource.getInstantiator(className).getModel();
    }

    public ComponentAssembler getComponentAssembler()
    {
        return assemblerSource.getAssembler(componentModel.getComponentClassName(), locale);
    }

    public ParameterBinder createParameterBinder(String parameterName)
    {
        int dotx = parameterName.indexOf('.');
        if (dotx >= 0)
        {
            String mixinId = parameterName.substring(0, dotx);
            if (!mixinIdToInstantiator.containsKey(mixinId))
            {
                String message = String.format("Mixin id for parameter '%s' not found. Attached mixins: %s.",
                                               parameterName,
                                               InternalUtils.joinSorted(mixinIdToInstantiator.keySet()));

                throw new TapestryException(message, location, null);
            }
        }
        else
        {
            // Unqualified parameter name. May be a reference not to a parameter of this component, but a published
            // parameter of a component embedded in this component. The ComponentAssembler for this component
            // will know.

            ComponentAssembler assembler = assemblerSource.getAssembler(componentModel.getComponentClassName(), locale);

            ParameterBinder binder = assembler.getBinder(parameterName);

            if (binder != null) return binder;
        }

        final ParameterBinder binder = parameterNameToBinder.get(parameterName);

        if (binder != null)
            return binder;

        // Informal parameter: Is there a mixin for that?

        if (informalParametersMixinId != null)
            return new ParameterBinderImpl(informalParametersMixinId, parameterName, null);

        if (componentModel.getSupportsInformalParameters())
            return new ParameterBinderImpl(null, parameterName, null);

        // Otherwise, informal parameter and not supported by the component or any mixin.

        return null;
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

    public void addMixinsToElement(ComponentPageElement newElement)
    {
        for (Map.Entry<String, Instantiator> entry : mixinIdToInstantiator.entrySet())
        {
            String mixinId = entry.getKey();
            Instantiator instantiator = entry.getValue();

            newElement.addMixin(mixinId, instantiator);
        }
    }

    public Location getLocation()
    {
        return location;
    }
}
