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
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.structure.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.ComponentClassResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;

class ComponentAssemblerImpl implements ComponentAssembler
{
    private final ComponentAssemblerSource assemblerSource;

    private final ComponentInstantiatorSource instantiatorSource;

    private final ComponentClassResolver componentClassResolver;

    private final Instantiator instantiator;

    private final Locale locale;

    private final ComponentPageElementResources resources;

    private final List<PageAssemblyAction> actions = CollectionFactory.newList();

    private final IdAllocator allocator = new IdAllocator();

    private Map<String, String> publishedParameterToEmbeddedId;

    private Map<String, EmbeddedComponentAssembler> embeddedIdToAssembler;

    public ComponentAssemblerImpl(ComponentAssemblerSource assemblerSource,
                                  ComponentInstantiatorSource instantiatorSource,
                                  ComponentClassResolver componentClassResolver,
                                  Instantiator instantiator,
                                  ComponentPageElementResources resources, Locale locale)
    {
        this.assemblerSource = assemblerSource;
        this.instantiatorSource = instantiatorSource;
        this.componentClassResolver = componentClassResolver;
        this.instantiator = instantiator;
        this.resources = resources;
        this.locale = locale;
    }

    public ComponentPageElement assembleRootComponent(Page page)
    {
        PageAssembly pageAssembly = new PageAssembly(page);

        try
        {
            ComponentPageElement newElement = new ComponentPageElementImpl(pageAssembly.page, instantiator, resources);

            pageAssembly.componentName.push(new ComponentName(pageAssembly.page.getName()));

            pageAssembly.page.addLifecycleListener(newElement);

            addRootComponentMixins(newElement);

            pushNewElement(pageAssembly, newElement);

            runActions(pageAssembly);

            popNewElement(pageAssembly);

            // Execute the deferred actions in reverse order to how they were added. This makes
            // sense, as (currently) all deferred actions are related to inheriting informal parameters;
            // those are added deepest component to shallowed (root) component, but should be executed
            // in the opposite order to ensure that chained inherited parameters resolve correctly.

            int count = pageAssembly.deferred.size();
            for (int i = count - 1; i >= 0; i--)
            {
                PageAssemblyAction action = pageAssembly.deferred.get(i);

                action.execute(pageAssembly);
            }


            return pageAssembly.createdElement.peek();
        }
        catch (RuntimeException ex)
        {
            throw new RuntimeException(String.format("Exception assembling root component of page %s: %s",
                                                     pageAssembly.page.getName(),
                                                     InternalUtils.toMessage(ex)),
                                       ex);
        }
    }

    private void addRootComponentMixins(ComponentPageElement element)
    {
        for (String className : instantiator.getModel().getMixinClassNames())
        {
            Instantiator mixinInstantiator = instantiatorSource.getInstantiator(className);

            element.addMixin(InternalUtils.lastTerm(className), mixinInstantiator);
        }
    }

    public void assembleEmbeddedComponent(PageAssembly pageAssembly, EmbeddedComponentAssembler embeddedAssembler,
                                          String embeddedId, String elementName,
                                          Location location)
    {
        ComponentPageElement container = pageAssembly.activeElement.peek();

        try
        {
            ComponentName containerName = pageAssembly.componentName.peek();

            ComponentName embeddedName = containerName.child(embeddedId.toLowerCase());

            pageAssembly.componentName.push(embeddedName);

            ComponentPageElement newElement = container.newChild(embeddedId,
                                                                 embeddedName.nestedId,
                                                                 embeddedName.completeId,
                                                                 elementName,
                                                                 instantiator,
                                                                 location);

            pageAssembly.page.addLifecycleListener(newElement);

            pushNewElement(pageAssembly, newElement);

            embeddedAssembler.addMixinsToElement(newElement);

            runActions(pageAssembly);

            popNewElement(pageAssembly);

            pageAssembly.componentName.pop();
        }
        catch (RuntimeException ex)
        {
            String message = String.format("Exception assembling embedded component '%s' (of type %s, within %s): %s",
                                           embeddedId,
                                           instantiator.getModel().getComponentClassName(),
                                           container.getCompleteId(),
                                           InternalUtils.toMessage(ex));

            throw new TapestryException(message, location, ex);
        }
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
            action.execute(pageAssembly);
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

            throw new RuntimeException(
                    String.format(
                            "Embedded component(s) %s are defined within component class %s (or a super-class of %s), " +
                                    "but are not present in the component template (%s).",
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
                                                              EmbeddedComponentModel embeddedModel, String mixins,
                                                              Location location)
    {
        try
        {
            EmbeddedComponentAssemblerImpl embedded = new EmbeddedComponentAssemblerImpl(assemblerSource,
                                                                                         instantiatorSource,
                                                                                         componentClassResolver,
                                                                                         componentClassName,
                                                                                         locale, embeddedModel,
                                                                                         mixins,
                                                                                         location);


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
                        String message = String.format(
                                "Parameter '%s' of embedded component '%s' can not be published as a parameter of component %s, as it has previously been published by embedded component '%s'.",
                                publishedParameterName,
                                embeddedId,
                                instantiator.getModel().getComponentClassName(),
                                existingEmbeddedId);

                        throw new TapestryException(message, location, null);
                    }

                    publishedParameterToEmbeddedId.put(publishedParameterName, embeddedId);
                }

            }

            return embedded;
        }
        catch (Exception ex)
        {
            String message = String.format("Failure creating embedded component '%s' of %s: %s",
                                           embeddedId,
                                           instantiator.getModel().getComponentClassName(),
                                           InternalUtils.toMessage(ex));

            throw new TapestryException(message, location, ex);
        }
    }

    public ParameterBinder getBinder(final String parameterName)
    {
        final String embeddedId = InternalUtils.get(publishedParameterToEmbeddedId, parameterName);

        if (embeddedId == null) return null;

        final EmbeddedComponentAssembler embededdedComponentAssembler = embeddedIdToAssembler.get(embeddedId);

        final ComponentAssembler embeddedAssembler = embededdedComponentAssembler.getComponentAssembler();

        final ParameterBinder embeddedBinder = embeddedAssembler.getBinder(parameterName);

        // The complex case: a re-publish!  Yes you can go deep here if you don't
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
            String message = String.format(
                    "Parameter '%s' of component %s is improperly published from embedded component '%s' (where it does not exist). " +
                            "This may be a typo in the publishParameters attribute of the @Component annotation.",
                    parameterName,
                    instantiator.getModel().getComponentClassName(),
                    embeddedId);

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

    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public String toString()
    {
        return String.format("ComponentAssembler[%s]", instantiator.getModel().getComponentClassName());
    }
}
