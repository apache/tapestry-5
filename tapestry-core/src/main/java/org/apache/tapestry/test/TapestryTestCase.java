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

package org.apache.tapestry.test;

import static java.lang.Thread.sleep;
import static org.apache.tapestry.internal.test.CodeEq.codeEq;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.easymock.EasyMock.eq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tapestry.AnnotationProvider;
import org.apache.tapestry.Asset;
import org.apache.tapestry.Binding;
import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ComponentResourcesCommon;
import org.apache.tapestry.Field;
import org.apache.tapestry.FieldValidator;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationTracker;
import org.apache.tapestry.Validator;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.internal.services.MarkupWriterImpl;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.model.ParameterModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ApplicationStateCreator;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategySource;
import org.apache.tapestry.services.AssetFactory;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.BindingFactory;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ClasspathAssetAliasManager;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.Context;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.FieldValidatorSource;
import org.apache.tapestry.services.Heartbeat;
import org.apache.tapestry.services.InjectionProvider;
import org.apache.tapestry.services.MethodFilter;
import org.apache.tapestry.services.MethodSignature;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.ResourceDigestGenerator;
import org.apache.tapestry.services.Response;
import org.apache.tapestry.services.Session;
import org.apache.tapestry.services.ValidationConstraintGenerator;
import org.apache.tapestry.services.ValidationMessagesSource;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Base test case that adds a number of convienience factory and training methods for the public
 * interfaces of Tapestry.
 */
public abstract class TapestryTestCase extends IOCTestCase
{

    protected final void train_findFieldsWithAnnotation(ClassTransformation transformation,
            Class<? extends Annotation> annotationClass, String... fieldNames)
    {
        train_findFieldsWithAnnotation(transformation, annotationClass, Arrays.asList(fieldNames));
    }

    protected final void train_findFieldsWithAnnotation(ClassTransformation transformation,
            Class<? extends Annotation> annotationClass, List<String> fieldNames)
    {
        expect(transformation.findFieldsWithAnnotation(annotationClass)).andReturn(fieldNames);
    }

    protected final <T extends Annotation> void train_getFieldAnnotation(
            ClassTransformation transformation, String fieldName, Class<T> annotationClass,
            T annotation)
    {
        expect(transformation.getFieldAnnotation(fieldName, annotationClass)).andReturn(annotation);
    }

    protected final MutableComponentModel newMutableComponentModel()
    {
        return newMock(MutableComponentModel.class);
    }

    protected final ClassTransformation newClassTransformation()
    {
        return newMock(ClassTransformation.class);
    }

    protected final void train_addInjectedField(ClassTransformation ct, Class type,
            String suggestedName, Object value, String fieldName)
    {
        expect(ct.addInjectedField(type, suggestedName, value)).andReturn(fieldName);
    }

    protected final void train_findUnclaimedFields(ClassTransformation transformation,
            String... fieldNames)
    {
        expect(transformation.findUnclaimedFields()).andReturn(Arrays.asList(fieldNames));
    }

    /** Writes a change to a file. */
    protected final void touch(File f) throws Exception
    {
        long startModified = f.lastModified();

        while (true)
        {
            OutputStream o = new FileOutputStream(f);
            o.write(0);
            o.close();

            long newModified = f.lastModified();

            if (newModified != startModified)
                return;

            // Sleep 1/20 second and try again

            sleep(50);
        }
    }

    protected final ComponentClassResolver newComponentClassResolver()
    {
        return newMock(ComponentClassResolver.class);
    }

    protected final void train_newBinding(BindingFactory factory, String description,
            ComponentResources container, ComponentResources component, String expression,
            Location l, Binding binding)
    {
        expect(factory.newBinding(description, container, component, expression, l)).andReturn(
                binding);
    }

    protected final ComponentResources newComponentResources()
    {
        return newMock(ComponentResources.class);
    }

    protected final Binding newBinding()
    {
        return newMock(Binding.class);
    }

    protected final BindingFactory newBindingFactory()
    {
        return newMock(BindingFactory.class);
    }

    protected final ComponentModel newComponentModel()
    {
        return newMock(ComponentModel.class);
    }

    protected final void train_addMethod(ClassTransformation transformation,
            MethodSignature signature, String... body)
    {
        transformation.addMethod(eq(signature), codeEq(join(body)));
    }

    protected final void train_newMemberName(ClassTransformation transformation, String suggested,
            String name)
    {
        expect(transformation.newMemberName(suggested)).andReturn(name);
    }

    protected final void train_newMemberName(ClassTransformation transformation, String prefix,
            String baseName, String name)
    {
        expect(transformation.newMemberName(prefix, baseName)).andReturn(name);
    }

    protected final void train_extendMethod(ClassTransformation transformation,
            MethodSignature signature, String... body)
    {
        transformation.extendMethod(eq(signature), codeEq(join(body)));
    }

    protected final void train_extendConstructor(ClassTransformation transformation, String... body)
    {
        transformation.extendConstructor(codeEq(join(body)));
    }

    protected final void train_getResourcesFieldName(ClassTransformation transformation, String name)
    {
        expect(transformation.getResourcesFieldName()).andReturn(name).atLeastOnce();
    }

    protected final void train_addField(ClassTransformation transformation, int modifiers,
            String type, String suggestedName, String actualName)
    {
        expect(transformation.addField(modifiers, type, suggestedName)).andReturn(actualName);
    }

    protected final void train_getFieldType(ClassTransformation transformation, String fieldName,
            String type)
    {
        expect(transformation.getFieldType(fieldName)).andReturn(type).atLeastOnce();

    }

    protected final void train_name(Parameter parameter, String name)
    {
        expect(parameter.name()).andReturn(name).atLeastOnce();
    }

    protected final void train_findMethodsWithAnnotation(ClassTransformation tf,
            Class<? extends Annotation> annotationType, List<MethodSignature> sigs)
    {
        expect(tf.findMethodsWithAnnotation(annotationType)).andReturn(sigs);
    }

    protected final Request newRequest()
    {
        return newMock(Request.class);
    }

    protected final void train_provideInjection(InjectionProvider provider, String fieldName,
            String fieldType, ServiceLocator locator, ClassTransformation transformation,
            MutableComponentModel model, boolean result)
    {
        expect(provider.provideInjection(fieldName, fieldType, locator, transformation, model))
                .andReturn(result);
    }

    protected final void train_toClass(ClassTransformation transformation, String type,
            Class classForType)
    {
        expect(transformation.toClass(type)).andReturn(classForType);
    }

    protected final ParameterModel newParameterModel()
    {
        return newMock(ParameterModel.class);
    }

    protected final void train_getComponentClassName(ComponentModel model, String className)
    {
        expect(model.getComponentClassName()).andReturn(className).atLeastOnce();
    }

    protected final void train_isRequired(ParameterModel model, boolean isRequired)
    {
        expect(model.isRequired()).andReturn(isRequired);
    }

    protected final void train_getParameterModel(ComponentModel model, String parameterName,
            ParameterModel parameterModel)
    {
        expect(model.getParameterModel(parameterName)).andReturn(parameterModel);
    }

    protected final void train_getParameterNames(ComponentModel model, String... names)
    {
        expect(model.getParameterNames()).andReturn(Arrays.asList(names));
    }

    protected final void train_isInvariant(Binding binding, boolean isInvariant)
    {
        expect(binding.isInvariant()).andReturn(isInvariant);
    }

    protected final MarkupWriter newMarkupWriter()
    {
        return newMock(MarkupWriter.class);
    }

    protected final void train_get(Binding binding, Object value)
    {
        expect(binding.get()).andReturn(value);
    }

    protected final void train_encodeURL(Response response, String inputURL, String outputURL)
    {
        expect(response.encodeURL(inputURL)).andReturn(outputURL);
    }

    protected final Response newResponse()
    {
        return newMock(Response.class);
    }

    protected void train_getAttribute(HttpSession session, String attributeName, Object value)
    {
        expect(session.getAttribute(attributeName)).andReturn(value);
    }

    protected final HttpServletRequest newHttpServletRequest()
    {
        return newMock(HttpServletRequest.class);
    }

    protected final void train_getSession(HttpServletRequest request, boolean create,
            HttpSession session)
    {
        expect(request.getSession(create)).andReturn(session);
    }

    protected final HttpSession newHttpSession()
    {
        return newMock(HttpSession.class);
    }

    protected final void train_getAttributeNames(Session session, String prefix, String... names)
    {
        expect(session.getAttributeNames(prefix)).andReturn(Arrays.asList(names));
    }

    protected final void train_getAttribute(Session session, String name, Object attribute)
    {
        expect(session.getAttribute(name)).andReturn(attribute);
    }

    protected final Session newSession()
    {
        return newMock(Session.class);
    }

    protected final void train_getSession(Request request, boolean create, Session session)
    {
        expect(request.getSession(create)).andReturn(session);
    }

    protected final void train_encodeRedirectURL(Response response, String URI, String encoded)
    {
        expect(response.encodeRedirectURL(URI)).andReturn(encoded);
    }

    protected final void train_getPage(ComponentResources resources, Component page)
    {
        expect(resources.getPage()).andReturn(page).atLeastOnce();
    }

    protected final void train_getCompleteId(ComponentResources resources, String completeId)
    {
        expect(resources.getCompleteId()).andReturn(completeId).atLeastOnce();
    }

    protected final void train_getComponentResources(Component component,
            ComponentResources resources)
    {
        expect(component.getComponentResources()).andReturn(resources).atLeastOnce();
    }

    protected final void train_getContainer(ComponentResources resources, Component container)
    {
        expect(resources.getContainer()).andReturn(container).atLeastOnce();
    }

    protected final RequestHandler newRequestHandler()
    {
        return newMock(RequestHandler.class);
    }

    protected final void train_service(RequestHandler handler, Request request, Response response,
            boolean result) throws IOException
    {
        expect(handler.service(request, response)).andReturn(result);
    }

    protected final void train_getLocale(Request request, Locale locale)
    {
        expect(request.getLocale()).andReturn(locale).atLeastOnce();
    }

    protected final void train_getParentModel(ComponentModel model, ComponentModel parentModel)
    {
        expect(model.getParentModel()).andReturn(parentModel).atLeastOnce();
    }

    protected final void train_getBaseResource(ComponentModel model, Resource resource)
    {
        expect(model.getBaseResource()).andReturn(resource).atLeastOnce();
    }

    protected final Asset newAsset()
    {
        return newMock(Asset.class);
    }

    protected final void train_createAsset(AssetFactory factory, Resource resource, Asset asset)
    {
        expect(factory.createAsset(resource)).andReturn(asset);
    }

    protected final void train_getRootResource(AssetFactory factory, Resource rootResource)
    {
        expect(factory.getRootResource()).andReturn(rootResource);
    }

    protected final AssetFactory newAssetFactory()
    {
        return newMock(AssetFactory.class);
    }

    protected final Context newContext()
    {
        return newMock(Context.class);
    }

    protected final void train_getClassName(ClassTransformation transformation, String className)
    {
        expect(transformation.getClassName()).andReturn(className).atLeastOnce();
    }

    protected final void train_value(Inject annotation, String value)
    {
        expect(annotation.value()).andReturn(value).atLeastOnce();
    }

    protected final <T extends Annotation> void train_getMethodAnnotation(ClassTransformation ct,
            MethodSignature signature, Class<T> annotationClass, T annotation)
    {
        expect(ct.getMethodAnnotation(signature, annotationClass)).andReturn(annotation)
                .atLeastOnce();
    }

    protected final ClasspathAssetAliasManager newClasspathAssetAliasManager()
    {
        return newMock(ClasspathAssetAliasManager.class);
    }

    protected final void train_toClientURL(ClasspathAssetAliasManager manager, String resourcePath,
            String clientURL)
    {
        expect(manager.toClientURL(resourcePath)).andReturn(clientURL);
    }

    protected final ResourceDigestGenerator newResourceDigestGenerator()
    {
        return newMock(ResourceDigestGenerator.class);
    }

    protected final void train_generateChecksum(ResourceDigestGenerator generator, URL url,
            String digest)
    {
        expect(generator.generateDigest(url)).andReturn(digest);
    }

    protected final void train_requiresDigest(ResourceDigestGenerator generator, String path,
            boolean requiresDigest)
    {
        expect(generator.requiresDigest(path)).andReturn(requiresDigest);
    }

    protected final void train_getPath(Request request, String path)
    {
        expect(request.getPath()).andReturn(path).atLeastOnce();
    }

    protected final void train_toResourcePath(ClasspathAssetAliasManager manager, String clientURL,
            String resourcePath)
    {
        expect(manager.toResourcePath(clientURL)).andReturn(resourcePath).atLeastOnce();
    }

    protected final void train_getDateHeader(Request request, String name, long value)
    {
        expect(request.getDateHeader(name)).andReturn(value).atLeastOnce();
    }

    protected final void train_findMethods(ClassTransformation transformation,
            final MethodSignature... signatures)
    {
        IAnswer<List<MethodSignature>> answer = new IAnswer<List<MethodSignature>>()
        {
            public List<MethodSignature> answer() throws Throwable
            {
                // Can't think of a way to do this without duplicating some code out of
                // InternalClassTransformationImpl

                List<MethodSignature> result = newList();
                MethodFilter filter = (MethodFilter) EasyMock.getCurrentArguments()[0];

                for (MethodSignature sig : signatures)
                {
                    if (filter.accept(sig))
                        result.add(sig);
                }

                // We don't have to sort them for testing purposes. Usually there's just going to be
                // one in there.

                return result;
            }

        };

        expect(transformation.findMethods(EasyMock.isA(MethodFilter.class))).andAnswer(answer);
    }

    protected final void train_isRootClass(MutableComponentModel model, boolean isRootClass)
    {
        expect(model.isRootClass()).andReturn(isRootClass);
    }

    protected final void train_getValidationMessages(ValidationMessagesSource messagesSource,
            Locale locale, Messages messages)
    {
        expect(messagesSource.getValidationMessages(locale)).andReturn(messages).atLeastOnce();
    }

    protected final void train_getLocale(ComponentResourcesCommon resources, Locale locale)
    {
        expect(resources.getLocale()).andReturn(locale).atLeastOnce();
    }

    protected <C, T> void train_getConstraintType(Validator<C, T> validator, Class<C> constraintType)
    {
        expect(validator.getConstraintType()).andReturn(constraintType);
    }

    protected final Validator newValidator()
    {
        return newMock(Validator.class);
    }

    protected final ValidationMessagesSource newValidationMessagesSource()
    {
        return newMock(ValidationMessagesSource.class);
    }

    protected final FieldValidator newFieldValidator()
    {
        return newMock(FieldValidator.class);
    }

    protected FieldValidatorSource newFieldValidatorSource()
    {
        return newMock(FieldValidatorSource.class);
    }

    protected final Component newComponent()
    {
        return newMock(Component.class);
    }

    protected final void train_getComponent(ComponentResources resources, Component component)
    {
        expect(resources.getComponent()).andReturn(component).atLeastOnce();
    }

    protected final void train_getPersistentFieldNames(ComponentModel model, String... names)
    {
        expect(model.getPersistentFieldNames()).andReturn(Arrays.asList(names)).atLeastOnce();
    }

    protected final BindingSource newBindingSource()
    {
        return newMock(BindingSource.class);
    }

    protected final void train_getClasspathAsset(AssetSource source, String path, Locale locale,
            Asset asset)
    {
        expect(source.getClasspathAsset(path, locale)).andReturn(asset);
    }

    protected final void toClientURL(Asset asset, String clientURL)
    {
        expect(asset.toClientURL()).andReturn(clientURL);
    }

    protected final <T> void train_peek(Environment env, Class<T> type, T value)
    {
        expect(env.peek(type)).andReturn(value);
    }

    protected final <T> void train_peekRequired(Environment env, Class<T> type, T value)
    {
        expect(env.peekRequired(type)).andReturn(value);
    }

    protected final Environment newEnvironment()
    {
        return newMock(Environment.class);
    }

    protected final AssetSource newAssetSource()
    {
        return newMock(AssetSource.class);
    }

    protected final Translator newTranslator()
    {
        return newMock(Translator.class);
    }

    protected final ValidationTracker newValidationTracker()
    {
        return newMock(ValidationTracker.class);
    }

    protected final Field newField()
    {
        return newMock(Field.class);
    }

    protected final void train_inError(ValidationTracker tracker, Field field, boolean inError)
    {
        expect(tracker.inError(field)).andReturn(inError);
    }

    protected final ComponentEventHandler newComponentEventHandler()
    {
        return newMock(ComponentEventHandler.class);
    }

    @SuppressWarnings("unchecked")
    protected final void train_handleResult(ComponentEventHandler handler, Object result,
            Component component, String methodDescription, boolean abort)
    {
        expect(handler.handleResult(result, component, methodDescription)).andReturn(abort);
    }

    protected final void train_toRedirectURI(Link link, String URI)
    {
        expect(link.toRedirectURI()).andReturn(URI).atLeastOnce();
    }

    protected final Link newLink()
    {
        return newMock(Link.class);
    }

    protected final Block newBlock()
    {
        return newMock(Block.class);
    }

    protected final void train_getSupportsInformalParameters(ComponentModel model, boolean supports)
    {
        expect(model.getSupportsInformalParameters()).andReturn(supports);
    }

    protected final Inject newInject()
    {
        return newMock(Inject.class);
    }

    protected final void train_findFieldsOfType(ClassTransformation transformation, String type,
            String... fieldNames)
    {
        expect(transformation.findFieldsOfType(type)).andReturn(Arrays.asList(fieldNames));
    }

    protected final Heartbeat newHeartbeat()
    {
        return newMock(Heartbeat.class);
    }

    protected void train_getMessageKey(Validator validator, String messageKey)
    {
        expect(validator.getMessageKey()).andReturn(messageKey).atLeastOnce();
    }

    protected final Field newFieldWithLabel(String label)
    {
        Field field = newField();

        train_getLabel(field, label);

        return field;
    }

    protected final void train_getLabel(Field field, String label)
    {
        expect(field.getLabel()).andReturn(label).atLeastOnce();
    }

    protected final void train_getContainerResources(ComponentResources resources,
            ComponentResources containerResources)
    {
        expect(resources.getContainerResources()).andReturn(containerResources).atLeastOnce();
    }

    protected final void train_getContainerMessages(ComponentResources resources,
            Messages containerMessages)
    {
        expect(resources.getContainerMessages()).andReturn(containerMessages).atLeastOnce();
    }

    protected final void train_getId(ComponentResources resources, String id)
    {
        expect(resources.getId()).andReturn(id).atLeastOnce();
    }

    protected final void train_getMessages(ComponentResources resources, Messages messages)
    {
        expect(resources.getMessages()).andReturn(messages).atLeastOnce();
    }

    protected final void train_getValueType(Validator validator, Class valueType)
    {
        expect(validator.getValueType()).andReturn(valueType).atLeastOnce();
    }

    protected final void train_invokeIfBlank(Validator validator, boolean invokeIfBlank)
    {
        expect(validator.invokeIfBlank()).andReturn(invokeIfBlank).atLeastOnce();
    }

    protected final void train_getFieldPersistenceStrategy(ComponentModel model, String fieldName,
            String fieldStrategy)
    {
        expect(model.getFieldPersistenceStrategy(fieldName)).andReturn(fieldStrategy).atLeastOnce();
    }

    protected final ApplicationStatePersistenceStrategy newApplicationStatePersistenceStrategy()
    {
        return newMock(ApplicationStatePersistenceStrategy.class);
    }

    protected final ApplicationStateCreator newApplicationStateCreator()
    {
        return newMock(ApplicationStateCreator.class);
    }

    protected final ApplicationStatePersistenceStrategySource newApplicationStatePersistenceStrategySource()
    {
        return newMock(ApplicationStatePersistenceStrategySource.class);
    }

    protected final <T> void train_get(ApplicationStatePersistenceStrategy strategy,
            Class<T> asoClass, ApplicationStateCreator<T> creator, T aso)
    {
        expect(strategy.get(asoClass, creator)).andReturn(aso);
    }

    protected final <T> void train_create(ApplicationStateCreator<T> creator, T aso)
    {
        expect(creator.create()).andReturn(aso);
    }

    protected final void train_get(ApplicationStatePersistenceStrategySource source,
            String strategyName, ApplicationStatePersistenceStrategy strategy)
    {
        expect(source.get(strategyName)).andReturn(strategy).atLeastOnce();
    }

    protected final <T extends Annotation> void train_getAnnotation(AnnotationProvider provider,
            Class<T> annotationClass, T annotation)
    {
        expect(provider.getAnnotation(annotationClass)).andReturn(annotation).atLeastOnce();
    }

    protected final void train_getConduit(PropertyModel model, PropertyConduit conduit)
    {
        expect(model.getConduit()).andReturn(conduit).atLeastOnce();
    }

    protected final PropertyConduit newPropertyConduit()
    {
        return newMock(PropertyConduit.class);
    }

    protected final PropertyModel newPropertyEditModel()
    {
        return newMock(PropertyModel.class);
    }

    protected final AnnotationProvider newAnnotationProvider()
    {
        return newMock(AnnotationProvider.class);
    }

    protected final void train_createValidator(FieldValidatorSource source, Field field,
            String validatorType, String constraintValue, String overrideId,
            Messages overrideMessages, Locale locale, FieldValidator result)
    {
        expect(
                source.createValidator(
                        field,
                        validatorType,
                        constraintValue,
                        overrideId,
                        overrideMessages,
                        locale)).andReturn(result);
    }

    protected final void train_buildConstraints(ValidationConstraintGenerator generator,
            Class propertyType, AnnotationProvider provider, String... constraints)
    {
        expect(generator.buildConstraints(propertyType, provider)).andReturn(
                Arrays.asList(constraints));
    }

    protected final ValidationConstraintGenerator newValidationConstraintGenerator()
    {
        return newMock(ValidationConstraintGenerator.class);
    }

    protected final void train_getMeta(ComponentModel model, String key, String value)
    {
        expect(model.getMeta(key)).andReturn(value).atLeastOnce();
    }

    protected void train_newBinding(BindingSource bindingSource, String description, ComponentResources componentResources, String defaultBindingPrefix, String expression, Binding binding)
    {
        expect(
                bindingSource.newBinding(
                        description,
                        componentResources,
                        defaultBindingPrefix,
                        expression)).andReturn(binding);
    }

    /**
     * Creates a new markup writer instance (not a markup writer mock). Output can be directed at
     * the writer, which uses the default (HTML) markup model. The writer's toString() value
     * represents all the collected markup in the writer.
     * 
     * @return
     */
    protected final MarkupWriter createMarkupWriter()
    {
        return new MarkupWriterImpl();
    }

    @SuppressWarnings("unchecked")
    protected final void train_renderInformalParameters(ComponentResources resources, final MarkupWriter writer, final String... informals)
    {
        resources.renderInformalParameters(writer);
        IAnswer answer = new IAnswer()
        {
            public Object answer() throws Throwable
            {
                writer.attributes(informals);
    
                return null;
            }
        };
    
        getMocksControl().andAnswer(answer);
    }
}
