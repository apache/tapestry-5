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

package org.apache.tapestry5.test;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Id;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.internal.services.MapMessages;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import static org.apache.tapestry5.internal.test.CodeEq.codeEq;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.model.ParameterModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import org.easymock.IAnswer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import static java.lang.Thread.sleep;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

/**
 * Base test case that adds a number of convienience factory and training methods for the public interfaces of
 * Tapestry.
 */
public abstract class TapestryTestCase extends IOCTestCase
{

    /**
     * Creates a new markup writer instance (not a markup writer mock). Output can be directed at the writer, which uses
     * the default (HTML) markup model. The writer's toString() value represents all the collected markup in the
     * writer.
     */
    protected final MarkupWriter createMarkupWriter()
    {
        return new MarkupWriterImpl();
    }

    protected final void train_getAliasesForMode(AliasManager manager, String mode, Map<Class, Object> configuration)
    {
        expect(manager.getAliasesForMode(mode)).andReturn(configuration);
    }

    protected final ApplicationStateCreator mockApplicationStateCreator()
    {
        return newMock(ApplicationStateCreator.class);
    }

    protected final ApplicationStatePersistenceStrategy mockApplicationStatePersistenceStrategy()
    {
        return newMock(ApplicationStatePersistenceStrategy.class);
    }

    protected final ApplicationStatePersistenceStrategySource mockApplicationStatePersistenceStrategySource()
    {
        return newMock(ApplicationStatePersistenceStrategySource.class);
    }

    protected final Asset mockAsset()
    {
        return newMock(Asset.class);
    }

    protected final AssetFactory mockAssetFactory()
    {
        return newMock(AssetFactory.class);
    }

    protected final AssetSource mockAssetSource()
    {
        return newMock(AssetSource.class);
    }

    protected final Binding mockBinding()
    {
        return newMock(Binding.class);
    }

    protected final BindingFactory mockBindingFactory()
    {
        return newMock(BindingFactory.class);
    }

    protected final BindingSource mockBindingSource()
    {
        return newMock(BindingSource.class);
    }

    protected final Block mockBlock()
    {
        return newMock(Block.class);
    }

    protected final ClasspathAssetAliasManager mockClasspathAssetAliasManager()
    {
        return newMock(ClasspathAssetAliasManager.class);
    }

    protected final ClassTransformation mockClassTransformation()
    {
        return newMock(ClassTransformation.class);
    }

    protected final Component mockComponent()
    {
        return newMock(Component.class);
    }

    protected final ComponentClassResolver mockComponentClassResolver()
    {
        return newMock(ComponentClassResolver.class);
    }

    protected final ComponentEventCallback mockComponentEventHandler()
    {
        return newMock(ComponentEventCallback.class);
    }

    protected final ComponentModel mockComponentModel()
    {
        return newMock(ComponentModel.class);
    }

    protected final ComponentResources mockComponentResources()
    {
        return newMock(ComponentResources.class);
    }

    protected final Context mockContext()
    {
        return newMock(Context.class);
    }

    protected final Environment mockEnvironment()
    {
        return newMock(Environment.class);
    }

    protected final Field mockField()
    {
        return newMock(Field.class);
    }

    protected final FieldValidator mockFieldValidator()
    {
        return newMock(FieldValidator.class);
    }

    protected FieldValidatorSource mockFieldValidatorSource()
    {
        return newMock(FieldValidatorSource.class);
    }

    protected final Field mockFieldWithLabel(String label)
    {
        Field field = mockField();

        train_getLabel(field, label);

        return field;
    }

    protected final Heartbeat mockHeartbeat()
    {
        return newMock(Heartbeat.class);
    }

    protected final HttpServletRequest mockHttpServletRequest()
    {
        return newMock(HttpServletRequest.class);
    }

    protected final HttpServletResponse mockHttpServletResponse()
    {
        return newMock(HttpServletResponse.class);
    }

    protected final HttpSession mockHttpSession()
    {
        return newMock(HttpSession.class);
    }

    protected final Inject mockInject()
    {
        return newMock(Inject.class);
    }

    protected final Link mockLink()
    {
        return newMock(Link.class);
    }

    protected final MarkupWriter mockMarkupWriter()
    {
        return newMock(MarkupWriter.class);
    }

    protected final MutableComponentModel mockMutableComponentModel()
    {
        return newMock(MutableComponentModel.class);
    }

    protected final ParameterModel mockParameterModel()
    {
        return newMock(ParameterModel.class);
    }

    protected final Path mockPath()
    {
        return newMock(Path.class);
    }

    protected final PropertyConduit mockPropertyConduit()
    {
        return newMock(PropertyConduit.class);
    }

    protected final PropertyModel mockPropertyModel()
    {
        return newMock(PropertyModel.class);
    }

    protected final Request mockRequest()
    {
        return newMock(Request.class);
    }

    protected final RequestHandler mockRequestHandler()
    {
        return newMock(RequestHandler.class);
    }

    protected final ResourceDigestGenerator mockResourceDigestGenerator()
    {
        return newMock(ResourceDigestGenerator.class);
    }

    protected final Response mockResponse()
    {
        return newMock(Response.class);
    }

    protected final Session mockSession()
    {
        return newMock(Session.class);
    }

    protected final Translator mockTranslator()
    {
        return newMock(Translator.class);
    }

    protected final ValidationConstraintGenerator mockValidationConstraintGenerator()
    {
        return newMock(ValidationConstraintGenerator.class);
    }

    protected final ValidationMessagesSource mockValidationMessagesSource()
    {
        return newMock(ValidationMessagesSource.class);
    }

    protected final ValidationTracker mockValidationTracker()
    {
        return newMock(ValidationTracker.class);
    }

    protected final Validator mockValidator()
    {
        return newMock(Validator.class);
    }

    /**
     * Writes a change to a file.
     */
    protected final void touch(File f) throws Exception
    {
        long startModified = f.lastModified();

        while (true)
        {
            OutputStream o = new FileOutputStream(f);
            o.write(0);
            o.close();

            long newModified = f.lastModified();

            if (newModified != startModified) return;

            // Sleep 1/20 second and try again

            sleep(50);
        }
    }

    protected final void train_addField(ClassTransformation transformation, int modifiers, String type,
                                        String suggestedName, String actualName)
    {
        expect(transformation.addField(modifiers, type, suggestedName)).andReturn(actualName);
    }

    protected final void train_addInjectedField(ClassTransformation ct, Class type, String suggestedName, Object value,
                                                String fieldName)
    {
        expect(ct.addInjectedField(type, suggestedName, value)).andReturn(fieldName);
    }

    protected final void train_addMethod(ClassTransformation transformation, TransformMethodSignature signature,
                                         String... body)
    {
        transformation.addMethod(eq(signature), codeEq(join(body)));
    }

    protected final void train_buildConstraints(ValidationConstraintGenerator generator, Class propertyType,
                                                AnnotationProvider provider, String... constraints)
    {
        expect(generator.buildConstraints(propertyType, provider)).andReturn(Arrays.asList(constraints));
    }

    protected final <T> void train_create(ApplicationStateCreator<T> creator, T aso)
    {
        expect(creator.create()).andReturn(aso);
    }

    protected final void train_createAsset(AssetFactory factory, Resource resource, Asset asset)
    {
        expect(factory.createAsset(resource)).andReturn(asset);
    }

    protected final void train_createValidator(FieldValidatorSource source, Field field, String validatorType,
                                               String constraintValue, String overrideId, Messages overrideMessages,
                                               Locale locale, FieldValidator result)
    {
        expect(source.createValidator(field, validatorType, constraintValue, overrideId, overrideMessages,
                                      locale)).andReturn(result);
    }

    protected final void train_encodeRedirectURL(Response response, String URI, String encoded)
    {
        expect(response.encodeRedirectURL(URI)).andReturn(encoded);
    }

    protected final void train_encodeURL(Response response, String inputURL, String outputURL)
    {
        expect(response.encodeURL(inputURL)).andReturn(outputURL);
    }

    protected final <T> void train_exists(ApplicationStatePersistenceStrategy strategy, Class<T> asoClass,
                                          boolean exists)
    {
        expect(strategy.exists(asoClass)).andReturn(exists);
    }

    protected final void train_extendConstructor(ClassTransformation transformation, String... body)
    {
        transformation.extendConstructor(codeEq(join(body)));
    }

    protected final void train_extendMethod(ClassTransformation transformation, TransformMethodSignature signature,
                                            String... body)
    {
        transformation.extendMethod(eq(signature), codeEq(join(body)));
    }

    protected final void train_getAsset(AssetSource source, Resource root, String path, Locale locale, Asset asset)
    {
        expect(source.getAsset(root, path, locale)).andReturn(asset);
    }

    protected final void train_findFieldsWithAnnotation(ClassTransformation transformation,
                                                        Class<? extends Annotation> annotationClass,
                                                        List<String> fieldNames)
    {
        expect(transformation.findFieldsWithAnnotation(annotationClass)).andReturn(fieldNames);
    }

    protected final void train_findFieldsWithAnnotation(ClassTransformation transformation,
                                                        Class<? extends Annotation> annotationClass,
                                                        String... fieldNames)
    {
        train_findFieldsWithAnnotation(transformation, annotationClass, Arrays.asList(fieldNames));
    }

    protected final void train_findMethods(ClassTransformation transformation,
                                           final TransformMethodSignature... signatures)
    {
        IAnswer<List<TransformMethodSignature>> answer = new IAnswer<List<TransformMethodSignature>>()
        {
            public List<TransformMethodSignature> answer() throws Throwable
            {
                // Can't think of a way to do this without duplicating some code out of
                // InternalClassTransformationImpl

                List<TransformMethodSignature> result = newList();
                MethodFilter filter = (MethodFilter) EasyMock.getCurrentArguments()[0];

                for (TransformMethodSignature sig : signatures)
                {
                    if (filter.accept(sig)) result.add(sig);
                }

                // We don't have to sort them for testing purposes. Usually there's just going to be
                // one in there.

                return result;
            }
        };

        expect(transformation.findMethods(EasyMock.isA(MethodFilter.class))).andAnswer(answer);
    }

    protected final void train_findMethodsWithAnnotation(ClassTransformation tf,
                                                         Class<? extends Annotation> annotationType,
                                                         List<TransformMethodSignature> sigs)
    {
        expect(tf.findMethodsWithAnnotation(annotationType)).andReturn(sigs);
    }

    protected final void train_findUnclaimedFields(ClassTransformation transformation, String... fieldNames)
    {
        expect(transformation.findUnclaimedFields()).andReturn(Arrays.asList(fieldNames));
    }

    protected final void train_generateChecksum(ResourceDigestGenerator generator, URL url, String digest)
    {
        expect(generator.generateDigest(url)).andReturn(digest);
    }

    protected final <T> void train_get(ApplicationStatePersistenceStrategy strategy, Class<T> asoClass,
                                       ApplicationStateCreator<T> creator, T aso)
    {
        expect(strategy.get(asoClass, creator)).andReturn(aso);
    }

    protected final void train_get(ApplicationStatePersistenceStrategySource source, String strategyName,
                                   ApplicationStatePersistenceStrategy strategy)
    {
        expect(source.get(strategyName)).andReturn(strategy).atLeastOnce();
    }

    protected final void train_get(Binding binding, Object value)
    {
        expect(binding.get()).andReturn(value);
    }

    protected void train_getAttribute(HttpSession session, String attributeName, Object value)
    {
        expect(session.getAttribute(attributeName)).andReturn(value);
    }

    protected final void train_getAttribute(Session session, String name, Object attribute)
    {
        expect(session.getAttribute(name)).andReturn(attribute);
    }

    protected final void train_getAttributeNames(Session session, String prefix, String... names)
    {
        expect(session.getAttributeNames(prefix)).andReturn(Arrays.asList(names));
    }

    protected final void train_getBaseResource(ComponentModel model, Resource resource)
    {
        expect(model.getBaseResource()).andReturn(resource).atLeastOnce();
    }

    protected final void train_getClassName(ClassTransformation transformation, String className)
    {
        expect(transformation.getClassName()).andReturn(className).atLeastOnce();
    }

    protected final void train_getClasspathAsset(AssetSource source, String path, Asset asset)
    {
        expect(source.getClasspathAsset(path)).andReturn(asset);
    }

    protected final void train_getClasspathAsset(AssetSource source, String path, Locale locale, Asset asset)
    {
        expect(source.getClasspathAsset(path, locale)).andReturn(asset);
    }

    protected final void train_getCompleteId(ComponentResourcesCommon resources, String completeId)
    {
        expect(resources.getCompleteId()).andReturn(completeId).atLeastOnce();
    }

    protected final void train_getComponent(ComponentResources resources, Component component)
    {
        expect(resources.getComponent()).andReturn(component).atLeastOnce();
    }

    protected final void train_getComponentClassName(ComponentModel model, String className)
    {
        expect(model.getComponentClassName()).andReturn(className).atLeastOnce();
    }

    protected final void train_getComponentResources(Component component, ComponentResources resources)
    {
        expect(component.getComponentResources()).andReturn(resources).atLeastOnce();
    }

    protected final void train_getConduit(PropertyModel model, PropertyConduit conduit)
    {
        expect(model.getConduit()).andReturn(conduit).atLeastOnce();
    }

    protected <C, T> void train_getConstraintType(Validator<C, T> validator, Class<C> constraintType)
    {
        expect(validator.getConstraintType()).andReturn(constraintType).atLeastOnce();
    }

    protected final void train_getContainer(ComponentResources resources, Component container)
    {
        expect(resources.getContainer()).andReturn(container).atLeastOnce();
    }

    protected final void train_getContainerMessages(ComponentResources resources, Messages containerMessages)
    {
        expect(resources.getContainerMessages()).andReturn(containerMessages).atLeastOnce();
    }

    protected final void train_getContainerResources(ComponentResources resources,
                                                     ComponentResources containerResources)
    {
        expect(resources.getContainerResources()).andReturn(containerResources).atLeastOnce();
    }

    protected final void train_getDateHeader(Request request, String name, long value)
    {
        expect(request.getDateHeader(name)).andReturn(value).atLeastOnce();
    }

    protected final <T extends Annotation> void train_getFieldAnnotation(ClassTransformation transformation,
                                                                         String fieldName, Class<T> annotationClass,
                                                                         T annotation)
    {
        expect(transformation.getFieldAnnotation(fieldName, annotationClass)).andReturn(annotation);
    }

    protected final void train_getFieldPersistenceStrategy(ComponentModel model, String fieldName, String fieldStrategy)
    {
        expect(model.getFieldPersistenceStrategy(fieldName)).andReturn(fieldStrategy).atLeastOnce();
    }

    protected final void train_getFieldType(ClassTransformation transformation, String fieldName, String type)
    {
        expect(transformation.getFieldType(fieldName)).andReturn(type).atLeastOnce();
    }

    protected final void train_getId(ComponentResources resources, String id)
    {
        expect(resources.getId()).andReturn(id).atLeastOnce();
    }

    protected final void train_getLabel(Field field, String label)
    {
        expect(field.getLabel()).andReturn(label).atLeastOnce();
    }

    protected final void train_getLocale(ComponentResourcesCommon resources, Locale locale)
    {
        expect(resources.getLocale()).andReturn(locale).atLeastOnce();
    }

    protected final void train_getLocale(Request request, Locale locale)
    {
        expect(request.getLocale()).andReturn(locale).atLeastOnce();
    }

    protected void train_getMessageKey(Validator validator, String messageKey)
    {
        expect(validator.getMessageKey()).andReturn(messageKey).atLeastOnce();
    }

    protected final void train_getMessages(ComponentResources resources, Messages messages)
    {
        expect(resources.getMessages()).andReturn(messages).atLeastOnce();
    }

    protected final void train_getMeta(ComponentModel model, String key, String value)
    {
        expect(model.getMeta(key)).andReturn(value).atLeastOnce();
    }

    protected final <T extends Annotation> void train_getMethodAnnotation(ClassTransformation ct,
                                                                          TransformMethodSignature signature,
                                                                          Class<T> annotationClass, T annotation)
    {
        expect(ct.getMethodAnnotation(signature, annotationClass)).andReturn(annotation)
                .atLeastOnce();
    }

    protected final void train_getMethodIdentifier(ClassTransformation transformation,
                                                   TransformMethodSignature signature, String id)
    {
        expect(transformation.getMethodIdentifier(signature)).andReturn(id);
    }

    protected final void train_getOutputStream(HttpServletResponse response, ServletOutputStream stream)
    {
        try
        {
            expect(response.getOutputStream()).andReturn(stream);
        }
        catch (IOException e)
        {
            fail(e.getMessage(), e);
        }
    }

    protected final void train_getPage(ComponentResources resources, Component page)
    {
        expect(resources.getPage()).andReturn(page).atLeastOnce();
    }

    protected final void train_getParameterModel(ComponentModel model, String parameterName,
                                                 ParameterModel parameterModel)
    {
        expect(model.getParameterModel(parameterName)).andReturn(parameterModel).atLeastOnce();
    }

    protected final void train_getParameterNames(ComponentModel model, String... names)
    {
        expect(model.getParameterNames()).andReturn(Arrays.asList(names));
    }

    protected final void train_getParentModel(ComponentModel model, ComponentModel parentModel)
    {
        expect(model.getParentModel()).andReturn(parentModel).atLeastOnce();
    }

    protected final void train_getPath(Request request, String path)
    {
        expect(request.getPath()).andReturn(path).atLeastOnce();
    }

    protected final void train_getPersistentFieldNames(ComponentModel model, String... names)
    {
        expect(model.getPersistentFieldNames()).andReturn(Arrays.asList(names)).atLeastOnce();
    }

    protected final void train_getResourcesFieldName(ClassTransformation transformation, String name)
    {
        expect(transformation.getResourcesFieldName()).andReturn(name).atLeastOnce();
    }

    protected final void train_getRootResource(AssetFactory factory, Resource rootResource)
    {
        expect(factory.getRootResource()).andReturn(rootResource);
    }

    protected final void train_getSession(HttpServletRequest request, boolean create, HttpSession session)
    {
        expect(request.getSession(create)).andReturn(session);
    }

    protected void train_getSession(Request request, boolean create, Session session)
    {
        expect(request.getSession(create)).andReturn(session);
    }

    protected final void train_getSupportsInformalParameters(ComponentModel model, boolean supports)
    {
        expect(model.getSupportsInformalParameters()).andReturn(supports).atLeastOnce();
    }

    protected final void train_getValidationMessages(ValidationMessagesSource messagesSource, Locale locale,
                                                     Messages messages)
    {
        expect(messagesSource.getValidationMessages(locale)).andReturn(messages).atLeastOnce();
    }

    protected final void train_getValueType(Validator validator, Class valueType)
    {
        expect(validator.getValueType()).andReturn(valueType).atLeastOnce();
    }

    @SuppressWarnings("unchecked")
    protected final void train_handleResult(ComponentEventCallback handler, Object result,
                                            boolean abort)
    {
        expect(handler.handleResult(result)).andReturn(abort);
    }

    protected final void train_inError(ValidationTracker tracker, Field field, boolean inError)
    {
        expect(tracker.inError(field)).andReturn(inError);
    }

    protected final void train_isRequired(Validator validator, boolean isRequired)
    {
        expect(validator.isRequired()).andReturn(isRequired).atLeastOnce();
    }

    protected final void train_isInvariant(Binding binding, boolean isInvariant)
    {
        expect(binding.isInvariant()).andReturn(isInvariant);
    }

    protected final void train_isRequired(ParameterModel model, boolean isRequired)
    {
        expect(model.isRequired()).andReturn(isRequired);
    }

    protected final void train_isRootClass(MutableComponentModel model, boolean isRootClass)
    {
        expect(model.isRootClass()).andReturn(isRootClass);
    }

    protected final void train_name(Parameter parameter, String name)
    {
        expect(parameter.name()).andReturn(name).atLeastOnce();
    }

    protected final void train_newBinding(BindingFactory factory, String description, ComponentResources container,
                                          ComponentResources component, String expression, Location l, Binding binding)
    {
        expect(factory.newBinding(description, container, component, expression, l)).andReturn(binding);
    }

    protected void train_newBinding(BindingSource bindingSource, String description,
                                    ComponentResources componentResources, String defaultBindingPrefix,
                                    String expression, Binding binding)
    {
        expect(bindingSource.newBinding(description, componentResources, defaultBindingPrefix, expression)).andReturn(
                binding);
    }

    protected final void train_newMemberName(ClassTransformation transformation, String suggested, String name)
    {
        expect(transformation.newMemberName(suggested)).andReturn(name);
    }

    protected final void train_newMemberName(ClassTransformation transformation, String prefix, String baseName,
                                             String name)
    {
        expect(transformation.newMemberName(prefix, baseName)).andReturn(name);
    }

    protected final <T> void train_peek(Environment env, Class<T> type, T value)
    {
        expect(env.peek(type)).andReturn(value);
    }

    protected final <T> void train_peekRequired(Environment env, Class<T> type, T value)
    {
        expect(env.peekRequired(type)).andReturn(value);
    }

    protected final void train_provideInjection(InjectionProvider provider, String fieldName, Class fieldType,
                                                ObjectLocator locator, ClassTransformation transformation,
                                                MutableComponentModel model, boolean result)
    {
        expect(provider.provideInjection(fieldName, fieldType, locator, transformation, model))
                .andReturn(result);
    }

    @SuppressWarnings("unchecked")
    protected final void train_renderInformalParameters(ComponentResources resources, final MarkupWriter writer,
                                                        final Object... informals)
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

        setAnswer(answer);
    }

    protected final void train_requiresDigest(ResourceDigestGenerator generator, String path, boolean requiresDigest)
    {
        expect(generator.requiresDigest(path)).andReturn(requiresDigest);
    }

    protected final void train_service(RequestHandler handler, Request request, Response response, boolean result)
            throws IOException
    {
        expect(handler.service(request, response)).andReturn(result);
    }

    protected final void train_setContentLength(HttpServletResponse response, int length)
    {
        response.setContentLength(length);
    }

    protected final void train_setContentType(HttpServletResponse response, String contentType)
    {
        response.setContentType(contentType);
    }

    protected final void train_setDateHeader(HttpServletResponse response, String headerName, long date)
    {
        response.setDateHeader(headerName, date);
    }

    protected final void train_toClass(ClassTransformation transformation, String type, Class classForType)
    {
        expect(transformation.toClass(type)).andReturn(classForType);
    }

    protected final void train_toClientURL(Asset asset, String URL)
    {
        expect(asset.toClientURL()).andReturn(URL).atLeastOnce();
    }

    protected final void train_toClientURL(ClasspathAssetAliasManager manager, String resourcePath, String clientURL)
    {
        expect(manager.toClientURL(resourcePath)).andReturn(clientURL);
    }

    protected final void train_toRedirectURI(Link link, String URI)
    {
        expect(link.toRedirectURI()).andReturn(URI).atLeastOnce();
    }

    protected final void train_toResourcePath(ClasspathAssetAliasManager manager, String clientURL, String resourcePath)
    {
        expect(manager.toResourcePath(clientURL)).andReturn(resourcePath).atLeastOnce();
    }

    protected final void train_value(Id annotation, String value)
    {
        expect(annotation.value()).andReturn(value).atLeastOnce();
    }

    protected final void train_value(Path annotation, String value)
    {
        expect(annotation.value()).andReturn(value).atLeastOnce();
    }

    protected final void train_create(BeanModelSource source, Class beanClass, boolean filterReadOnly,
                                      Messages messages, BeanModel model)
    {
        expect(source.create(beanClass, filterReadOnly, messages)).andReturn(model);
    }

    protected final void train_getBoundType(ComponentResources resources, String parameterName, Class type)
    {
        expect(resources.getBoundType(parameterName)).andReturn(type);
    }

    protected final BeanModel mockBeanModel()
    {
        return newMock(BeanModel.class);
    }

    protected final BeanModelSource mockBeanModelSource()
    {
        return newMock(BeanModelSource.class);
    }

    public final void train_getLocation(Locatable locatable, Location location)
    {
        expect(locatable.getLocation()).andReturn(location).atLeastOnce();
    }

    public final void train_getResource(Location location, Resource resource)
    {
        expect(location.getResource()).andReturn(resource).atLeastOnce();
    }

    public final void train_getLine(Location location, int line)
    {
        expect(location.getLine()).andReturn(line).atLeastOnce();
    }

    protected final void train_getParameter(Request request, String elementName, String value)
    {
        expect(request.getParameter(elementName)).andReturn(value).atLeastOnce();
    }

    protected final void train_getPageName(ComponentResourcesCommon resources, String pageName)
    {
        expect(resources.getPageName()).andReturn(pageName).atLeastOnce();
    }

    protected final FormSupport mockFormSupport()
    {
        return newMock(FormSupport.class);
    }

    /**
     * Provides access to component messages, suitable for testing. Reads the associated .properties file for the class
     * (NOT any localization of it). Only the messages directly in the .properties file is available.
     *
     * @param componentClass component class whose messages are needed *
     * @return the Messages instance
     */
    protected final Messages messagesFor(Class componentClass) throws IOException
    {
        String file = componentClass.getSimpleName() + ".properties";

        Properties properties = new Properties();

        InputStream is = null;

        try
        {
            is = componentClass.getResourceAsStream(file);

            if (is == null) throw new RuntimeException(
                    String.format("Class %s does not have a message catalog.", componentClass.getName()));

            properties.load(is);
        }
        finally
        {
            InternalUtils.close(is);
        }

        Map<String, String> map = CollectionFactory.newCaseInsensitiveMap();

        for (Object key : properties.keySet())
        {

            String skey = (String) key;

            map.put(skey, properties.getProperty(skey));
        }

        return new MapMessages(Locale.ENGLISH, map);
    }

    protected final FieldValidationSupport mockFieldValidationSupport()
    {
        return newMock(FieldValidationSupport.class);
    }

    protected final RenderSupport mockRenderSupport()
    {
        return newMock(RenderSupport.class);
    }

    protected final void train_getInheritInformalParameters(EmbeddedComponentModel model, boolean inherits)
    {
        expect(model.getInheritInformalParameters()).andReturn(inherits).atLeastOnce();
    }

    protected final ApplicationStateManager mockApplicationStateManager()
    {
        return newMock(ApplicationStateManager.class);
    }

    protected final <T> void train_get(ApplicationStateManager manager, Class<T> asoClass, T aso)
    {
        expect(manager.get(asoClass)).andReturn(aso);
    }

    protected final void train_getInput(ValidationTracker tracker, Field field, String input)
    {
        expect(tracker.getInput(field)).andReturn(input);
    }

    protected final void train_isXHR(Request request, boolean isXHR)
    {
        expect(request.isXHR()).andReturn(isXHR).atLeastOnce();
    }

    protected void train_getPathInfo(HttpServletRequest request, String pathInfo)
    {
        expect(request.getPathInfo()).andReturn(pathInfo).atLeastOnce();
    }

    protected final void train_service(HttpServletRequestHandler handler, HttpServletRequest request,
                                       HttpServletResponse response, boolean result) throws IOException
    {
        expect(handler.service(request, response)).andReturn(result);
    }

    protected final void train_getServletPath(HttpServletRequest request, String path)
    {
        expect(request.getServletPath()).andReturn(path).atLeastOnce();
    }

    protected final HttpServletRequestHandler mockHttpServletRequestHandler()
    {
        return newMock(HttpServletRequestHandler.class);
    }

    protected final NullFieldStrategy mockNullFieldStrategy()
    {
        return newMock(NullFieldStrategy.class);
    }

    protected final ValueEncoderSource mockValueEncoderSource()
    {
        return newMock(ValueEncoderSource.class);
    }

    protected final ValueEncoder mockValueEncoder()
    {
        return newMock(ValueEncoder.class);
    }

    protected final void train_toClient(ValueEncoder valueEncoder, Object value, String encoded)
    {
        expect(valueEncoder.toClient(value)).andReturn(encoded);
    }

    protected final void train_getValueEncoder(ValueEncoderSource source, Class type, ValueEncoder valueEncoder)
    {
        expect(source.getValueEncoder(type)).andReturn(valueEncoder).atLeastOnce();
    }

    protected final void train_toValue(ValueEncoder valueEncoder, String clientValue, Object value)
    {
        expect(valueEncoder.toValue(clientValue)).andReturn(value);
    }

    protected <T> void train_findMeta(MetaDataLocator locator, String key,
                                      ComponentResources resources, Class<T> expectedType, T value)
    {
        expect(locator.findMeta(key, resources, expectedType)).andReturn(value).atLeastOnce();
    }

    protected MetaDataLocator mockMetaDataLocator()
    {
        return newMock(MetaDataLocator.class);
    }

    protected final void train_isSecure(Request request, boolean isSecure)
    {
        expect(request.isSecure()).andReturn(isSecure).atLeastOnce();
    }

    protected final void train_getBaseURL(BaseURLSource baseURLSource, boolean secure, String baseURL)
    {
        expect(baseURLSource.getBaseURL(secure)).andReturn(baseURL);
    }

    protected final BaseURLSource mockBaseURLSource()
    {
        return newMock(BaseURLSource.class);
    }

    protected final void train_getAttribute(Request request, String attibuteName, Object value)
    {
        expect(request.getAttribute(attibuteName)).andReturn(value);
    }

    protected final void train_getBlockParameter(ComponentResources resources, String name, Block block)
    {
        expect(resources.getBlockParameter(name)).andReturn(block).atLeastOnce();
    }

    protected final PropertyOverrides mockPropertyOverrides()
    {
        return newMock(PropertyOverrides.class);
    }

    protected void train_getOverrideBlock(PropertyOverrides overrides, String name, Block block)
    {
        expect(overrides.getOverrideBlock(name)).andReturn(block).atLeastOnce();
    }

    protected final void train_getOverrideMessages(PropertyOverrides overrides, Messages messages)
    {
        expect(overrides.getOverrideMessages()).andReturn(messages);
    }

    protected final void train_isDisabled(Field field, boolean disabled)
    {
        expect(field.isDisabled()).andReturn(disabled);
    }

    protected final ValidationDecorator mockValidationDecorator()
    {
        return newMock(ValidationDecorator.class);
    }

    protected final void train_isRequired(Field field, boolean required)
    {
        expect(field.isRequired()).andReturn(required);
    }

    protected final void train_getClientId(ClientElement element, String clientId)
    {
        expect(element.getClientId()).andReturn(clientId);
    }

    protected final FieldTranslator mockFieldTranslator()
    {
        return newMock(FieldTranslator.class);
    }


    protected final Translator mockTranslator(String name, Class type)
    {
        Translator translator = mockTranslator();

        train_getName(translator, name);
        train_getType(translator, type);

        return translator;
    }

    protected final void train_getName(Translator translator, String name)
    {
        expect(translator.getName()).andReturn(name).atLeastOnce();
    }

    protected final void train_getType(Translator translator, Class type)
    {
        expect(translator.getType()).andReturn(type).atLeastOnce();
    }

    protected final void train_createDefaultTranslator(FieldTranslatorSource source, ComponentResources resources,
                                                       String parameterName, FieldTranslator translator)
    {
        expect(source.createDefaultTranslator(resources, parameterName)).andReturn(translator);
    }

    protected final TranslatorSource mockTranslatorSource()
    {
        return newMock(TranslatorSource.class);
    }

    protected final void train_get(TranslatorSource translatorSource, String name, Translator translator)
    {
        expect(translatorSource.get(name)).andReturn(translator).atLeastOnce();
    }

    protected final void train_getMessageKey(Translator translator, String messageKey)
    {
        expect(translator.getMessageKey()).andReturn(messageKey).atLeastOnce();
    }

    protected final void train_findByType(TranslatorSource ts, Class propertyType, Translator translator)
    {
        expect(ts.findByType(propertyType)).andReturn(translator);
    }

    protected final void train_toURI(Link link, String URI)
    {
        expect(link.toURI()).andReturn(URI);
    }

    protected final void train_createEditModel(BeanModelSource source, Class beanClass, Messages messages,
                                               BeanModel model)
    {
        expect(source.createEditModel(beanClass, messages)).andReturn(model);
    }

    protected final ComponentEventResultProcessor mockComponentEventResultProcessor()
    {
        return newMock(ComponentEventResultProcessor.class);
    }

    protected final void train_getFormComponentId(FormSupport formSupport, String componentId)
    {
        expect(formSupport.getFormComponentId()).andReturn(componentId).atLeastOnce();
    }

    protected final void train_getFormValidationId(FormSupport formSupport, String validationId)
    {
        expect(formSupport.getFormValidationId()).andReturn(validationId).atLeastOnce();
    }

    protected final void train_isAllowNull(ParameterModel model, boolean allowNull)
    {
        expect(model.isAllowNull()).andReturn(allowNull).atLeastOnce();
    }

    protected final void train_isInvalidated(Session session, boolean invalidated)
    {
        expect(session.isInvalidated()).andReturn(invalidated);
    }

    protected final ComponentEventRequestHandler mockComponentEventRequestHandler()
    {
        return newMock(ComponentEventRequestHandler.class);
    }

    protected final ComponentRequestHandler mockComponentRequestHandler()
    {
        return newMock(ComponentRequestHandler.class);
    }

    protected final Asset2 mockAsset2()
    {
        return newMock(Asset2.class);
    }
}
