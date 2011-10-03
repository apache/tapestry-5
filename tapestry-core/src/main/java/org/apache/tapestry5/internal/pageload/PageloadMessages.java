// Copyright 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.internal.parser.TokenType;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.model.ComponentModel;

/**
 * Localized exception message support for pageload-related classes.
 *
 * @since 5.2.0
 */
final class PageloadMessages
{

    private static final Messages MESSAGES = MessagesImpl.forClass(PageloadMessages.class);

    private PageloadMessages()
    {
    }

    public static String uniqueMixinRequired(String mixinId)
    {
        return MESSAGES.format("unique_mixin_required", mixinId);
    }

    public static String missingComponentType()
    {
        return MESSAGES.get("missing_component_type");
    }

    public static String noMoreTokens()
    {
        return MESSAGES.get("no_more_tokens");
    }

    public static String couldNotFindOverride(String extensionPointId)
    {
        return MESSAGES.format("could_not_find_override", extensionPointId);
    }

    public static String noParentForExtension(ComponentModel model)
    {
        return MESSAGES.format("no_parent_for_extension", model.getComponentClassName());
    }

    public static String tokenNotImplemented(TokenType type)
    {
        return MESSAGES.format("token_not_implemented", type.toString());
    }

    public static String redundantEmbeddedComponentTypes(String embeddedId, String embeddedType, String modelType)
    {
        return MESSAGES.format("redundant_embedded_component_types", embeddedId, embeddedType, modelType);
    }

    public static String recursiveTemplate(String componentClassName)
    {
        return MESSAGES.format("recursive_template", componentClassName);
    }

    public static String compositeRenderCommandMethodNotImplemented(String methodName)
    {
        return MESSAGES.format("composite_render_command_method_not_implemented", methodName);
    }

    public static String exceptionAssemblingRootComponent(String pageName, String exceptionMessage)
    {
        return MESSAGES.format("exception_assembling_root_component", pageName, exceptionMessage);
    }

    public static String exceptionAssemblingEmbeddedComponent(
            String embeddedId,
            String embeddedClassName,
            String containerId,
            String exception)
    {
        return MESSAGES.format("exception_assembling_embedded_component",
                embeddedId,
                embeddedClassName,
                containerId,
                exception);
    }

    public static String embeddedComponentsNotInTemplate(
            String joinedComponentIds,
            String qualifiedClassName,
            String simpleClassName,
            Resource templateResource)
    {
        return MESSAGES.format("embedded_components_not_in_template",
                joinedComponentIds,
                qualifiedClassName,
                simpleClassName,
                templateResource);
    }

    public static String parameterAlreadyPublished(
            String publishedParameterName,
            String embeddedId,
            String componentClassName,
            String existingEmbeddedId)
    {
        return MESSAGES.format("parameter_already_published",
                publishedParameterName,
                embeddedId,
                componentClassName,
                existingEmbeddedId);
    }

    public static String failureCreatingEmbeddedComponent(String embeddedId, String containerClass, String exception)
    {
        return MESSAGES.format("failure_creating_embedded_component", embeddedId, containerClass, exception);
    }

    public static String publishedParameterNonexistant(String parameterName, String publishingClass, String embeddedId)
    {
        return MESSAGES.format("published_parameter_nonexistant", parameterName, publishingClass, embeddedId);
    }
}
