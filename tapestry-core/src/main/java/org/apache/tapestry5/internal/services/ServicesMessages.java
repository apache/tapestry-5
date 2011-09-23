// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.RenderCommand;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ServicesMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(ServicesMessages.class);

    public static String markupWriterNoCurrentElement()
    {
        return MESSAGES.get("markup-writer-no-current-element");
    }

    public static String missingTemplateResource(Resource resource)
    {
        return MESSAGES.format("missing-template-resource", resource);
    }

    public static String contentInsideBodyNotAllowed(Location location)
    {
        return MESSAGES.format("content-inside-body-not-allowed", location);
    }

    public static String renderQueueError(RenderCommand command, Throwable cause)
    {
        return MESSAGES.format("render-queue-error", command, cause);
    }

    public static String contextIndexOutOfRange(String methodDescription)
    {
        return MESSAGES.format("context-index-out-of-range", methodDescription);
    }

    public static String pageNameUnresolved(String pageClassName)
    {
        return MESSAGES.format("page-name-unresolved", pageClassName);
    }

    public static String exceptionInMethodParameter(String methodDescription, int index, Throwable cause)
    {
        return MESSAGES.format("exception-in-method-parameter", methodDescription, index + 1, cause);
    }

    public static String componentEventIsAborted(String methodDescription)
    {
        return MESSAGES.format("component-event-is-aborted", methodDescription);
    }

    public static String componentInstanceIsNotAPage(Component result)
    {
        return MESSAGES.format("component-instance-is-not-a-page", result.getComponentResources().getCompleteId());
    }

    public static String failureReadingMessages(Resource url, Throwable cause)
    {
        return MESSAGES.format("failure-reading-messages", url, cause);
    }

    public static String unknownAssetPrefix(String path)
    {
        return MESSAGES.format("unknown-asset-prefix", path);
    }

    public static String assetDoesNotExist(Resource resource)
    {
        return MESSAGES.format("asset-does-not-exist", resource);
    }

    public static String wrongAssetDigest(Resource resource)
    {
        return MESSAGES.format("wrong-asset-digest", resource.getPath());
    }

    public static String unknownValidatorType(String validatorType, List<String> knownValidatorTypes)
    {
        return MESSAGES.format("unknown-validator-type", validatorType, InternalUtils.join(knownValidatorTypes));
    }

    public static String validatorSpecificationParseError(int cursor, String specification)
    {
        return MESSAGES.format("validator-specification-parse-error", specification.charAt(cursor), cursor + 1,
                specification);
    }

    public static String mixinsInvalidWithoutIdOrType(String elementName)
    {
        return MESSAGES.format("mixins-invalid-without-id-or-type", elementName);
    }

    public static String undefinedTapestryAttribute(String elementName, String attributeName,
                                                    String allowedAttributeName)
    {
        return MESSAGES.format("undefined-tapestry-attribute", elementName, attributeName, allowedAttributeName);
    }

    public static String parameterElementNameRequired()
    {
        return MESSAGES.get("parameter-element-name-required");
    }

    public static String missingApplicationStatePersistenceStrategy(String name, Collection<String> availableNames)
    {
        return MESSAGES.format("missing-application-state-persistence-strategy", name, InternalUtils
                .joinSorted(availableNames));
    }

    public static String requestException(Throwable cause)
    {
        return MESSAGES.format("request-exception", cause);
    }

    public static String clientStateMustBeSerializable(Object newValue)
    {
        return MESSAGES.format("client-state-must-be-serializable", newValue);
    }

    public static String corruptClientState()
    {
        return MESSAGES.get("corrupt-client-state");
    }

    public static String unclosedAttributeExpression(String expression)
    {
        return MESSAGES.format("unclosed-attribute-expression", expression);
    }

    public static String noDisplayForDataType(String datatype)
    {
        return MESSAGES.format("no-display-for-data-type", datatype);
    }

    public static String noEditForDataType(String datatype)
    {
        return MESSAGES.format("no-edit-for-data-type", datatype);
    }

    public static String missingValidatorConstraint(String validatorType, Class type, String perFormMessageKey,
                                                    String generalMessageKey)
    {
        return MESSAGES.format("missing-validator-constraint", validatorType, type.getName(), perFormMessageKey,
                generalMessageKey);
    }

    public static String resourcesAccessForbidden(String URI)
    {
        return MESSAGES.format("resource-access-forbidden", URI);
    }

    public static String noMarkupFromPageRender(Page page)
    {
        return MESSAGES.format("no-markup-from-page-render", page.getName());
    }

    public static String baseClassInWrongPackage(String parentClassName, String className, String suggestedPackage)
    {
        return MESSAGES.format("base-class-in-wrong-package", parentClassName, className, suggestedPackage);
    }

    public static String invalidId(String messageKey, String idValue)
    {
        return MESSAGES.format(messageKey, idValue);
    }

    public static String noTranslatorForType(Class valueType, Collection<String> typeNames)
    {
        return MESSAGES.format("no-translator-for-type", ClassFabUtils.toJavaClassName(valueType), InternalUtils
                .joinSorted(typeNames));
    }

    public static String eventNotHandled(ComponentPageElement element, String eventName)
    {
        return MESSAGES.format("event-not-handled", eventName, element.getCompleteId());
    }

    public static String documentMissingHTMLRoot(String rootElementName)
    {
        return MESSAGES.format("document-missing-html-root", rootElementName);
    }

    public static String parameterElementDoesNotAllowAttributes()
    {
        return MESSAGES.get("parameter-element-does-not-allow-attributes");
    }

    public static String invalidPathForLibraryNamespace(String URI)
    {
        return MESSAGES.format("invalid-path-for-library-namespace", URI);
    }

    public static String literalConduitNotUpdateable()
    {
        return MESSAGES.get("literal-conduit-not-updateable");
    }

    public static String markupWriterAttributeNameOrValueOmitted(String element, Object[] namesAndValues)
    {
        return MESSAGES.format("markup-writer-attribute-name-or-value-omitted", element, InternalUtils.join(Arrays
                .asList(namesAndValues)));
    }
}
