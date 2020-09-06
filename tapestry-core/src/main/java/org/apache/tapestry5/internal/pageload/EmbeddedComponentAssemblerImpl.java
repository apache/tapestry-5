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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmbeddedComponentAssemblerImpl implements EmbeddedComponentAssembler
{
    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentAssemblerSource assemblerSource;

    private final ComponentResourceSelector selector;

    private final ComponentModel componentModel;

    private final Location location;

    private final Map<String, Instantiator> mixinIdToInstantiator = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, String[]> mixinsIdToOrderConstraints = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Maps parameter names (both simple, and qualified with the mixin id) to the corresponding QualifiedParameterName.
     */
    private final Map<String, ParameterBinder> parameterNameToBinder = CollectionFactory.newCaseInsensitiveMap();

    // The id of the mixin to receive informal parameters. If null, the component itself recieves them.
    // If the component doesn't support them, they are quietly dropped.

    private final String informalParametersMixinId;

    private final String componentPsuedoMixinId;

    private Map<String, Boolean> bound;

    /**
     * @param assemblerSource
     * @param instantiatorSource
     *         used to access component models
     * @param componentClassResolver
     *         used to convert mixin types to component models
     * @param componentClassName
     *         class name of embedded component
     * @param selector
     *         used to select template and other resources
     * @param embeddedModel
     *         embedded model (may be null for components defined in the template)
     * @param templateMixins
     *         list of mixins from the t:mixins element (possibly null)
     * @param location
     *         location of components element in its container's template
     * @param strictMixinParameters
     *         if true (e.g., the 5.4 DTD) then mixin parameters must be fully qualified
     */
    public EmbeddedComponentAssemblerImpl(ComponentAssemblerSource assemblerSource,
                                          ComponentInstantiatorSource instantiatorSource, ComponentClassResolver componentClassResolver,
                                          String componentClassName, ComponentResourceSelector selector, EmbeddedComponentModel embeddedModel,
                                          String templateMixins, Location location, boolean strictMixinParameters)
    {
        this.assemblerSource = assemblerSource;
        this.instantiatorSource = instantiatorSource;
        this.selector = selector;
        this.location = location;

        componentModel = getModel(componentClassName);

        // Add the implementation mixins defined by the component model.

        for (String className : componentModel.getMixinClassNames())
        {
            addMixin(className, componentModel.getOrderForMixin(className));
        }

        // If there's an embedded model (i.e., there was an @Component annotation)
        // then it may define some mixins.

        if (embeddedModel != null)
        {
            for (String className : embeddedModel.getMixinClassNames())
            {
                addMixin(className, embeddedModel.getConstraintsForMixin(className));
            }
        }

        // And the template may include a t:mixins element to define yet more mixins.
        // Template strings specified as:
        for (String mixinDef : TapestryInternalUtils.splitAtCommas(templateMixins))
        {
            Orderable<String> order = TapestryInternalUtils.mixinTypeAndOrder(mixinDef);
            String className = componentClassResolver.resolveMixinTypeToClassName(order.getId());

            addMixin(className, order.getConstraints());
        }

        // Finally (new in 5.3, for TAP5-1680), the component itself can sometimes acts as a mixin;
        // this allows for dealing with parameter name conflicts between the component and the mixin
        // (especially where informal parameters are involved).

        componentPsuedoMixinId = InternalUtils.lastTerm(componentClassName);

        // A bit ugly ... side-effects PLUS a return value ... but that's final variables
        // for you.
        informalParametersMixinId = prescanMixins(strictMixinParameters);
    }

    private String prescanMixins(boolean strictMixinParameters)
    {
        // Mixin id found to support informal parameters

        String supportsInformals = null;

        for (Map.Entry<String, Instantiator> entry : mixinIdToInstantiator.entrySet())
        {
            String mixinId = entry.getKey();
            ComponentModel mixinModel = entry.getValue().getModel();

            updateParameterNameToQualified(mixinId, mixinModel, strictMixinParameters);

            if (supportsInformals == null && mixinModel.getSupportsInformalParameters())
                supportsInformals = mixinId;
        }

        // The component comes last and overwrites simple names from the others.

        updateParameterNameToQualified(null, componentModel, false);

        return supportsInformals;
    }

    private void updateParameterNameToQualified(String mixinId, ComponentModel model, boolean strictMixinParameters)
    {
        for (String parameterName : model.getParameterNames())
        {
            String defaultBindingPrefix = model.getParameterModel(parameterName).getDefaultBindingPrefix();

            ParameterBinderImpl binder = new ParameterBinderImpl(mixinId, parameterName, defaultBindingPrefix);

            if (mixinId == null)
            {
                // This is a formal parameter of the root component so it's always unqualified.
                parameterNameToBinder.put(parameterName, binder);
            } else
            {
                // This is a formal parameter of a mixin, it must be qualified.
                parameterNameToBinder.put(mixinId + "." + parameterName, binder);

                // When not strict (the older DTDs, before 5.4), then register an unqualified alias.
                if (!strictMixinParameters)
                {
                    parameterNameToBinder.put(parameterName, binder);
                }
            }
        }
    }

    private void addMixin(String className, String... order)
    {
        Instantiator mixinInstantiator = instantiatorSource.getInstantiator(className);

        String mixinId = InternalUtils.lastTerm(className);

        if (mixinIdToInstantiator.containsKey(mixinId))
            throw new TapestryException(String.format("Mixins applied to a component must be unique. Mixin '%s' has already been applied.", mixinId), location, null);

        mixinIdToInstantiator.put(mixinId, mixinInstantiator);
        mixinsIdToOrderConstraints.put(mixinId, order);
    }

    private ComponentModel getModel(String className)
    {
        return instantiatorSource.getInstantiator(className).getModel();
    }

    public ComponentAssembler getComponentAssembler()
    {
        return assemblerSource.getAssembler(componentModel.getComponentClassName(), selector);
    }


    public ParameterBinder createParameterBinder(String qualifiedParameterName)
    {
        int dotx = qualifiedParameterName.indexOf('.');

        if (dotx < 0)
        {
            return createParameterBinderFromSimpleParameterName(qualifiedParameterName);
        }

        return createParameterBinderFromQualifiedParameterName(qualifiedParameterName, qualifiedParameterName.substring(0, dotx),
                qualifiedParameterName.substring(dotx + 1));
    }

    private ParameterBinder createParameterBinderFromSimpleParameterName(String parameterName)
    {

        // Look for a *formal* parameter with the simple name on the component itself.

        ParameterBinder binder = getComponentAssembler().getBinder(parameterName);

        if (binder != null)
        {
            return binder;
        }

        // Next see if any mixin has a formal parameter with this simple name.

        binder = parameterNameToBinder.get(parameterName);

        if (binder != null)
        {
            return binder;
        }


        // So, is there an mixin that's claiming all informal parameters?

        if (informalParametersMixinId != null)
        {
            return new ParameterBinderImpl(informalParametersMixinId, parameterName, null);
        }

        // Maybe the component claims informal parameters?
        if (componentModel.getSupportsInformalParameters())
            return new ParameterBinderImpl(null, parameterName, null);

        // Otherwise, informal parameter are not supported by the component or any mixin.

        return null;
    }

    private ParameterBinder createParameterBinderFromQualifiedParameterName(String qualifiedParameterName, String mixinId, String parameterName)
    {

        if (mixinId.equalsIgnoreCase(componentPsuedoMixinId))
        {
            return createParameterBinderForComponent(qualifiedParameterName, parameterName);
        }

        if (!mixinIdToInstantiator.containsKey(mixinId))
        {
            throw new TapestryException(
                    String.format("Mixin id for parameter '%s' not found. Attached mixins: %s.", qualifiedParameterName,
                            InternalUtils.joinSorted(mixinIdToInstantiator.keySet())), location,
                    null);
        }

        ParameterBinder binder = parameterNameToBinder.get(qualifiedParameterName);

        if (binder != null)
        {
            return binder;
        }

        // Ok, so perhaps this is a qualified name for an informal parameter of the mixin.

        Instantiator instantiator = mixinIdToInstantiator.get(mixinId);

        assert instantiator != null;

        return bindInformalParameter(qualifiedParameterName, mixinId, parameterName, instantiator.getModel());
    }

    private ParameterBinder bindInformalParameter(String qualifiedParameterName, String mixinId, String parameterName, ComponentModel model)
    {
        if (model.getSupportsInformalParameters())
        {
            return new ParameterBinderImpl(mixinId, parameterName, null);
        }

        // Pretty sure this was not caught as an error in 5.2.

        throw new TapestryException(String.format("Binding parameter %s as an informal parameter does not make sense, as %s does not support informal parameters.",
                qualifiedParameterName, model.getComponentClassName()), location, null);
    }

    private ParameterBinder createParameterBinderForComponent(String qualifiedParameterName, String parameterName)
    {
        ParameterBinder binder = getComponentAssembler().getBinder(parameterName);

        if (binder != null)
        {
            return binder;
        }

        return bindInformalParameter(qualifiedParameterName, null, parameterName, componentModel);
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

    public int addMixinsToElement(ComponentPageElement newElement)
    {
        for (Map.Entry<String, Instantiator> entry : mixinIdToInstantiator.entrySet())
        {
            String mixinId = entry.getKey();
            Instantiator instantiator = entry.getValue();

            newElement.addMixin(mixinId, instantiator, mixinsIdToOrderConstraints.get(mixinId));
        }

        return mixinIdToInstantiator.size();
    }

    public Location getLocation()
    {
        return location;
    }

    public Set<String> getFormalParameterNames()
    {
        return new HashSet<String>(componentModel.getParameterNames());
    }
}
