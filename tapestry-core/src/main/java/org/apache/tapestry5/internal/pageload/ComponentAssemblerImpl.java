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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.structure.*;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.List;
import java.util.Map;

class ComponentAssemblerImpl implements ComponentAssembler
{
    private final ComponentAssemblerSource assemblerSource;

    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentClassResolver componentClassResolver;

    private final Instantiator instantiator;

    private final ComponentPageElementResources resources;

    private final List<PageAssemblyAction> actions = CollectionFactory.newList();

    private final IdAllocator allocator = new IdAllocator();

    private final OperationTracker tracker;

    private final boolean strictMixinParameters;

    private Map<String, String> publishedParameterToEmbeddedId;

    private Map<String, EmbeddedComponentAssembler> embeddedIdToAssembler;

    public ComponentAssemblerImpl(ComponentAssemblerSource assemblerSource,
                                  ComponentInstantiatorSource instantiatorSource, ComponentClassResolver componentClassResolver,
                                  Instantiator instantiator, ComponentPageElementResources resources, OperationTracker tracker, boolean strictMixinParameters)
    {
        this.assemblerSource = assemblerSource;
        this.instantiatorSource = instantiatorSource;
        this.componentClassResolver = componentClassResolver;
        this.instantiator = instantiator;
        this.resources = resources;
        this.tracker = tracker;
        this.strictMixinParameters = strictMixinParameters;
    }

    public ComponentPageElement assembleRootComponent(final Page page)
    {
        return tracker.invoke("Assembling root component for page " + page.getName(),
                new Invokable<ComponentPageElement>()
                {
                    public ComponentPageElement invoke()
                    {
                        return performAssembleRootComponent(page);
                    }
                });
    }

    private ComponentPageElement performAssembleRootComponent(Page page)
    {
        PageAssembly pageAssembly = new PageAssembly(page);

        long startTime = System.currentTimeMillis();

        try
        {
            pageAssembly.componentCount++;
            pageAssembly.weight++;

            ComponentPageElement newElement = new ComponentPageElementImpl(pageAssembly.page, instantiator, resources);

            pageAssembly.componentName.push(new ComponentName(pageAssembly.page.getName()));

            addRootComponentMixins(pageAssembly, newElement);

            pushNewElement(pageAssembly, newElement);

            runActions(pageAssembly);

            popNewElement(pageAssembly);

            // Execute the deferred actions in reverse order to how they were added. This makes
            // sense, as (currently) all deferred actions are related to inheriting informal parameters;
            // those are added deepest component to shallowest (root) component, but should be executed
            // in the opposite order to ensure that chained inherited parameters resolve correctly.

            int count = pageAssembly.deferred.size();
            for (int i = count - 1; i >= 0; i--)
            {
                PageAssemblyAction action = pageAssembly.deferred.get(i);

                pageAssembly.weight++;

                action.execute(pageAssembly);
            }

            page.setStats(new Page.Stats(System.currentTimeMillis() - startTime, pageAssembly.componentCount, pageAssembly.weight));

            return pageAssembly.createdElement.peek();
        } catch (RuntimeException ex)
        {
            throw new RuntimeException(String.format("Exception assembling root component of page %s: %s", pageAssembly.page.getName(), ExceptionUtils.toMessage(ex)), ex);
        }
    }

    private void addRootComponentMixins(PageAssembly assembly, ComponentPageElement element)
    {
        for (String className : instantiator.getModel().getMixinClassNames())
        {
            assembly.weight++;

            Instantiator mixinInstantiator = instantiatorSource.getInstantiator(className);

            ComponentModel model = instantiator.getModel();
            element.addMixin(InternalUtils.lastTerm(className), mixinInstantiator, model.getOrderForMixin(className));
        }
    }

    public void assembleEmbeddedComponent(final PageAssembly pageAssembly,
                                          final EmbeddedComponentAssembler embeddedAssembler, final String embeddedId, final String elementName,
                                          final Location location)
    {
        ComponentName containerName = pageAssembly.componentName.peek();

        final ComponentName embeddedName = containerName.child(embeddedId.toLowerCase());

        final String componentClassName = instantiator.getModel().getComponentClassName();

        String description = String.format("Assembling component %s (%s)", embeddedName.completeId, componentClassName);

        tracker.run(description, new Runnable()
        {
            public void run()
            {
                ComponentPageElement container = pageAssembly.activeElement.peek();

                try
                {
                    pageAssembly.componentName.push(embeddedName);

                    ComponentPageElement newElement = container.newChild(embeddedId, embeddedName.nestedId,
                            embeddedName.completeId, elementName, instantiator, location);

                    pageAssembly.componentCount++;
                    pageAssembly.weight++;

                    pushNewElement(pageAssembly, newElement);

                    int mixinCount = embeddedAssembler.addMixinsToElement(newElement);

                    pageAssembly.weight += mixinCount;

                    runActions(pageAssembly);

                    popNewElement(pageAssembly);

                    pageAssembly.componentName.pop();
                } catch (RuntimeException ex)
                {
                    throw new TapestryException(String.format("Exception assembling embedded component '%s' (of type %s, within %s): %s",
                            embeddedId,
                            componentClassName,
                            container.getCompleteId(),
                            ExceptionUtils.toMessage(ex)), location, ex);
                }
            }
        });
    }

    private void pushNewElement(PageAssembly pageAssembly, final ComponentPageElement componentElement)
    {
        // This gets popped after all actions have executed.
        pageAssembly.activeElement.push(componentElement);

        // The container pops this one.
        pageAssembly.createdElement.push(componentElement);

        BodyPageElement shunt = new BodyPageElement()
        {
            public void addToBody(RenderCommand element)
            {
                componentElement.addToTemplate(element);
            }
        };

        pageAssembly.bodyElement.push(shunt);
    }

    private void popNewElement(PageAssembly pageAssembly)
    {
        pageAssembly.bodyElement.pop();
        pageAssembly.activeElement.pop();

        // But the component itself stays on the createdElement stack!
    }

    private void runActions(PageAssembly pageAssembly)
    {
        for (PageAssemblyAction action : actions)
        {
            pageAssembly.weight++;
            action.execute(pageAssembly);
        }
    }

    public ComponentModel getModel()
    {
        return instantiator.getModel();
    }

    public void add(PageAssemblyAction action)
    {
        actions.add(action);
    }

    public void validateEmbeddedIds(Map<String, Location> componentIds, Resource templateResource)
    {
        Map<String, Boolean> embeddedIds = CollectionFactory.newCaseInsensitiveMap();

        for (String id : getModel().getEmbeddedComponentIds())
            embeddedIds.put(id, true);

        for (String id : componentIds.keySet())
        {
            allocator.allocateId(id);
            embeddedIds.remove(id);
        }

        if (!embeddedIds.isEmpty())
        {

            String className = getModel().getComponentClassName();

            throw new RuntimeException(String.format("Embedded component(s) %s are defined within component class %s (or a super-class of %s), but are not present in the component template (%s).",
                    InternalUtils.joinSorted(embeddedIds.keySet()),
                    className,
                    InternalUtils.lastTerm(className),
                    templateResource));
        }
    }

    public String generateEmbeddedId(String componentType)
    {
        // Component types may be in folders; strip off the folder part for starters.

        int slashx = componentType.lastIndexOf("/");

        String baseId = componentType.substring(slashx + 1).toLowerCase();

        // The idAllocator is pre-loaded with all the component ids from the template, so even
        // if the lower-case type matches the id of an existing component, there won't be a name
        // collision.

        return allocator.allocateId(baseId);
    }

    public EmbeddedComponentAssembler createEmbeddedAssembler(String embeddedId, String componentClassName,
                                                              EmbeddedComponentModel embeddedModel, String mixins, Location location)
    {
        try
        {

            if (InternalUtils.isBlank(componentClassName))
            {
                throw new TapestryException(
                        "You must specify the type via t:type, the element, or @Component annotation.", location, null);
            }

            EmbeddedComponentAssemblerImpl embedded = new EmbeddedComponentAssemblerImpl(assemblerSource,
                    instantiatorSource, componentClassResolver, componentClassName, getSelector(), embeddedModel,
                    mixins, location, strictMixinParameters);

            if (embeddedIdToAssembler == null)
                embeddedIdToAssembler = CollectionFactory.newMap();

            embeddedIdToAssembler.put(embeddedId, embedded);

            if (embeddedModel != null)
            {
                for (String publishedParameterName : embeddedModel.getPublishedParameters())
                {
                    if (publishedParameterToEmbeddedId == null)
                        publishedParameterToEmbeddedId = CollectionFactory.newCaseInsensitiveMap();

                    String existingEmbeddedId = publishedParameterToEmbeddedId.get(publishedParameterName);

                    if (existingEmbeddedId != null)
                    {
                        throw new TapestryException(
                                String.format("Parameter '%s' of embedded component '%s' can not be published as a parameter of component %s, as it has previously been published by embedded component '%s'.",
                                        publishedParameterName,
                                        embeddedId,
                                        instantiator
                                                .getModel().getComponentClassName(),
                                        existingEmbeddedId), location, null);
                    }

                    publishedParameterToEmbeddedId.put(publishedParameterName, embeddedId);
                }

            }

            return embedded;
        } catch (Exception ex)
        {
            throw new TapestryException(String.format("Failure creating embedded component '%s' of %s: %s", embeddedId, instantiator
                    .getModel().getComponentClassName(), ExceptionUtils.toMessage(ex)), location, ex);
        }
    }

    public ParameterBinder getBinder(final String parameterName)
    {
        final String embeddedId = InternalUtils.get(publishedParameterToEmbeddedId, parameterName);

        if (embeddedId == null)
            return null;

        final EmbeddedComponentAssembler embededdedComponentAssembler = embeddedIdToAssembler.get(embeddedId);

        final ComponentAssembler embeddedAssembler = embededdedComponentAssembler.getComponentAssembler();

        final ParameterBinder embeddedBinder = embeddedAssembler.getBinder(parameterName);

        // The complex case: a re-publish! Yes you can go deep here if you don't
        // value your sanity!

        if (embeddedBinder != null)
        {
            return new ParameterBinder()
            {
                public void bind(ComponentPageElement element, Binding binding)
                {
                    ComponentPageElement subelement = element.getEmbeddedElement(embeddedId);

                    embeddedBinder.bind(subelement, binding);
                }

                public String getDefaultBindingPrefix(String metaDefault)
                {
                    return embeddedBinder.getDefaultBindingPrefix(metaDefault);
                }
            };
        }

        final ParameterBinder innerBinder = embededdedComponentAssembler.createParameterBinder(parameterName);

        if (innerBinder == null)
        {
            String message = String.format("Parameter '%s' of component %s is improperly published from embedded component '%s' " +
                    "(where it does not exist). This may be a typo in the publishParameters attribute of " +
                    "the @Component annotation.", parameterName, instantiator.getModel()
                    .getComponentClassName(), embeddedId);

            throw new TapestryException(message, embededdedComponentAssembler.getLocation(), null);
        }
        // The simple case, publishing a parameter of a subcomponent as if it were a parameter
        // of this component.

        return new ParameterBinder()
        {
            public void bind(ComponentPageElement element, Binding binding)
            {
                ComponentPageElement subelement = element.getEmbeddedElement(embeddedId);

                innerBinder.bind(subelement, binding);
            }

            public String getDefaultBindingPrefix(String metaDefault)
            {
                return innerBinder.getDefaultBindingPrefix(metaDefault);
            }
        };
    }

    public ComponentResourceSelector getSelector()
    {
        return resources.getSelector();
    }

    @Override
    public String toString()
    {
        return String.format("ComponentAssembler[%s %s]", instantiator.getModel().getComponentClassName(), getSelector());
    }
}
