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

import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.structure.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.runtime.RenderCommand;

import java.util.List;
import java.util.Map;

class ComponentAssemblerImpl implements ComponentAssembler
{
    private final Instantiator instantiator;

    private final ComponentPageElementResources resources;

    private final List<PageAssemblyAction> actions = CollectionFactory.newList();

    private final IdAllocator allocator = new IdAllocator();

    public ComponentAssemblerImpl(Instantiator instantiator, ComponentPageElementResources resources)
    {
        this.instantiator = instantiator;
        this.resources = resources;
    }

    public ComponentPageElement assembleRootComponent(Page page)
    {
        PageAssembly pageAssembly = new PageAssembly(page);

        try
        {
            ComponentPageElement newElement = new ComponentPageElementImpl(pageAssembly.page, instantiator, resources);

            buildRecursively(pageAssembly, newElement);

            for (PageAssemblyAction action : pageAssembly.deferred)
            {
                action.execute(pageAssembly);
            }

            return pageAssembly.createdElement.peek();
        }
        catch (RuntimeException ex)
        {
            throw new RuntimeException(String.format("Exception assembling root component of page %s: %s",
                                                     pageAssembly.page.getLogicalName(),
                                                     InternalUtils.toMessage(ex)),
                                       ex);
        }
    }

    public void assembleEmbeddedComponent(PageAssembly pageAssembly, String embeddedId, String elementName,
                                          Location location)
    {
        ComponentPageElement container = pageAssembly.activeElement.peek();

        try
        {
            ComponentPageElement newElement = container.newChild(embeddedId, elementName, instantiator, location);

            buildRecursively(pageAssembly, newElement);
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

    private void buildRecursively(PageAssembly pageAssembly, ComponentPageElement newElement)
    {
        pageAssembly.page.addLifecycleListener(newElement);

        pushNewElement(pageAssembly, newElement);

        runActions(pageAssembly);

        popNewElement(pageAssembly);
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


    public void validateEmbeddedIds(ComponentTemplate template)
    {
        Map<String, Boolean> embeddedIds = CollectionFactory.newCaseInsensitiveMap();

        for (String id : getModel().getEmbeddedComponentIds())
            embeddedIds.put(id, true);

        for (String id : template.getComponentIds().keySet())
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
                            template.getResource()));
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

    @Override
    public String toString()
    {
        return String.format("ComponentAssembler[%s]", instantiator.getModel().getComponentClassName());
    }
}
