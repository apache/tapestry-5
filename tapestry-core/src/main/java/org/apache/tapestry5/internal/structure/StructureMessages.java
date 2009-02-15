// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

final class StructureMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(StructureMessages.class);

    private StructureMessages()
    {
    }

    static String missingParameters(List<String> parameters, ComponentPageElement element)
    {
        return MESSAGES.format("missing-parameters", InternalUtils.joinSorted(parameters), element
                .getComponentResources().getComponentModel().getComponentClassName());
    }

    static String noSuchComponent(ComponentPageElement parent, String embeddedId, Set<String> components)
    {
        return MESSAGES.format("no-such-component", parent.getCompleteId(), embeddedId,
                               InternalUtils.joinSorted(components));
    }

    static String getParameterFailure(String parameterName, String componentId, Throwable cause)
    {
        return MESSAGES.format("get-parameter-failure", parameterName, componentId, cause);
    }

    static String writeParameterFailure(String parameterName, String componentId, Throwable cause)
    {
        return MESSAGES.format("write-parameter-failure", parameterName, componentId, cause);
    }

    static String unknownMixin(String componentId, String mixinClassName)
    {
        return MESSAGES.format("unknown-mixin", componentId, mixinClassName);
    }

    static String detachFailure(Object listener, Throwable cause)
    {
        return MESSAGES.format("detach-failure", listener, cause);
    }

    static String wrongPhaseResultType(Class expectedType)
    {
        return MESSAGES.format("wrong-phase-result-type", expectedType.getName());
    }

    static String blockNotFound(String componentId, String blockId)
    {
        return MESSAGES.format("block-not-found", componentId, blockId);
    }

    static String unbalancedElements(String componentId)
    {
        return MESSAGES.format("unbalanced-elements", componentId);
    }

    static String pageIsDirty(Page page)
    {
        return MESSAGES.format("page-is-dirty", page);
    }

    static String duplicateChildComponent(ComponentPageElement container, String childId)
    {
        return MESSAGES.format("duplicate-child-component", container.getCompleteId(), childId);
    }

    static String originalChildComponent(ComponentPageElement container, String childId, Location originalLocation)
    {
        return MESSAGES.format("original-child-component", container.getCompleteId(), childId,
                               originalLocation.getResource().getPath(), originalLocation.getLine());
    }

    static String duplicateBlock(ComponentPageElement component, String blockId)
    {
        return MESSAGES.format("duplicate-block", component.getCompleteId(), blockId);
    }

    static String fieldPersistFailure(String componentId, String fieldName, Throwable cause)
    {
        return MESSAGES.format("field-persist-failure", componentId, fieldName, cause);
    }

    static String missingRenderVariable(String componentId, String name, Collection<String> names)
    {

        return MESSAGES.format("missing-render-variable", componentId, name, InternalUtils.joinSorted(names));
    }

    static String renderVariableSetWhenNotRendering(String completeId, String name)
    {
        return MESSAGES.format("render-variable-set-when-not-rendering", completeId, name);
    }

    static String persistChangeBeforeLoadComplete()
    {
        return MESSAGES.get("persist-change-before-load-complete");
    }
}
