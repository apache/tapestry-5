// Copyright 2006-2012 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.*;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.beaneditor.DataTypeConstants;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.grid.GridConstants;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.internal.*;
import org.apache.tapestry5.internal.alerts.AlertManagerImpl;
import org.apache.tapestry5.internal.beaneditor.EnvironmentMessages;
import org.apache.tapestry5.internal.beaneditor.MessagesConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.PrimitiveFieldConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.ValidateAnnotationConstraintGenerator;
import org.apache.tapestry5.internal.bindings.*;
import org.apache.tapestry5.internal.dynamic.DynamicTemplateParserImpl;
import org.apache.tapestry5.internal.grid.CollectionGridDataSource;
import org.apache.tapestry5.internal.grid.NullDataSource;
import org.apache.tapestry5.internal.gzip.GZipFilter;
import org.apache.tapestry5.internal.renderers.*;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.services.ajax.AjaxFormUpdateFilter;
import org.apache.tapestry5.internal.services.ajax.AjaxResponseRendererImpl;
import org.apache.tapestry5.internal.services.ajax.MultiZoneUpdateEventResultProcessor;
import org.apache.tapestry5.internal.services.assets.AssetPathConstructorImpl;
import org.apache.tapestry5.internal.services.assets.ClasspathAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.ContextAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.StackAssetRequestHandler;
import org.apache.tapestry5.internal.services.linktransform.LinkTransformerImpl;
import org.apache.tapestry5.internal.services.linktransform.LinkTransformerInterceptor;
import org.apache.tapestry5.internal.services.messages.PropertiesFileParserImpl;
import org.apache.tapestry5.internal.services.meta.ContentTypeExtractor;
import org.apache.tapestry5.internal.services.meta.MetaAnnotationExtractor;
import org.apache.tapestry5.internal.services.meta.MetaWorkerImpl;
import org.apache.tapestry5.internal.services.security.ClientWhitelistImpl;
import org.apache.tapestry5.internal.services.security.LocalhostOnly;
import org.apache.tapestry5.internal.services.templates.DefaultTemplateLocator;
import org.apache.tapestry5.internal.services.templates.PageTemplateLocator;
import org.apache.tapestry5.internal.transform.*;
import org.apache.tapestry5.internal.translator.NumericTranslator;
import org.apache.tapestry5.internal.translator.NumericTranslatorSupport;
import org.apache.tapestry5.internal.translator.StringTranslator;
import org.apache.tapestry5.internal.util.RenderableAsBlock;
import org.apache.tapestry5.internal.util.StringRenderable;
import org.apache.tapestry5.internal.validator.ValidatorMacroImpl;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.assets.AssetsModule;
import org.apache.tapestry5.services.compatibility.CompatibilityModule;
import org.apache.tapestry5.services.dynamic.DynamicTemplate;
import org.apache.tapestry5.services.dynamic.DynamicTemplateParser;
import org.apache.tapestry5.services.javascript.*;
import org.apache.tapestry5.services.linktransform.ComponentEventLinkTransformer;
import org.apache.tapestry5.services.linktransform.LinkTransformer;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.apache.tapestry5.services.meta.FixedExtractor;
import org.apache.tapestry5.services.meta.MetaDataExtractor;
import org.apache.tapestry5.services.meta.MetaWorker;
import org.apache.tapestry5.services.pageload.PageLoadModule;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.InjectionProvider2;
import org.apache.tapestry5.validator.*;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The root module for Tapestry.
 */
@Marker(Core.class)
@SubModule(
        {InternalModule.class, AssetsModule.class, PageLoadModule.class, JavaScriptModule.class, CompatibilityModule.class})
public final class TapestryModule
{
    private final PipelineBuilder pipelineBuilder;

    private final ApplicationGlobals applicationGlobals;

    private final PropertyShadowBuilder shadowBuilder;

    private final Environment environment;

    private final StrategyBuilder strategyBuilder;

    private final PropertyAccess propertyAccess;

    private final ChainBuilder chainBuilder;

    private final Request request;

    private final Response response;

    private final RequestGlobals requestGlobals;

    private final EnvironmentalShadowBuilder environmentalBuilder;

    private final EndOfRequestEventHub endOfRequestEventHub;

    /**
     * We inject all sorts of common dependencies (including builders) into the
     * module itself (note: even though some of
     * these service are defined by the module itself, that's ok because
     * services are always lazy proxies). This isn't
     * about efficiency (it may be slightly more efficient, but not in any
     * noticeable way), it's about eliminating the
     * need to keep injecting these dependencies into individual service builder
     * and contribution methods.
     */
    public TapestryModule(PipelineBuilder pipelineBuilder,

                          PropertyShadowBuilder shadowBuilder,

                          RequestGlobals requestGlobals,

                          ApplicationGlobals applicationGlobals,

                          ChainBuilder chainBuilder,

                          Environment environment,

                          StrategyBuilder strategyBuilder,

                          PropertyAccess propertyAccess,

                          Request request,

                          Response response,

                          EnvironmentalShadowBuilder environmentalBuilder,

                          EndOfRequestEventHub endOfRequestEventHub)
    {
        this.pipelineBuilder = pipelineBuilder;
        this.shadowBuilder = shadowBuilder;
        this.requestGlobals = requestGlobals;
        this.applicationGlobals = applicationGlobals;
        this.chainBuilder = chainBuilder;
        this.environment = environment;
        this.strategyBuilder = strategyBuilder;
        this.propertyAccess = propertyAccess;
        this.request = request;
        this.response = response;
        this.environmentalBuilder = environmentalBuilder;
        this.endOfRequestEventHub = endOfRequestEventHub;
    }

    // A bunch of classes "promoted" from inline inner class to nested classes,
    // just so that the stack trace would be more readable. Most of these
    // are terminators for pipeline services.

    /**
     * @since 5.1.0.0
     */
    private class ApplicationInitializerTerminator implements ApplicationInitializer
    {
        public void initializeApplication(Context context)
        {
            applicationGlobals.storeContext(context);
        }
    }

    /**
     * @since 5.1.0.0
     */
    private class HttpServletRequestHandlerTerminator implements HttpServletRequestHandler
    {
        private final RequestHandler handler;
        private final String applicationCharset;
        private final TapestrySessionFactory sessionFactory;

        public HttpServletRequestHandlerTerminator(RequestHandler handler, String applicationCharset,
                                                   TapestrySessionFactory sessionFactory)
        {
            this.handler = handler;
            this.applicationCharset = applicationCharset;
            this.sessionFactory = sessionFactory;
        }

        public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
                throws IOException
        {
            requestGlobals.storeServletRequestResponse(servletRequest, servletResponse);

            // Should have started doing this a long time ago: recoding attributes into
            // the request for things that may be needed downstream, without having to extend
            // Request.

            servletRequest.setAttribute("servletAPI.protocol", servletRequest.getProtocol());
            servletRequest.setAttribute("servletAPI.characterEncoding", servletRequest.getCharacterEncoding());
            servletRequest.setAttribute("servletAPI.contentLength", servletRequest.getContentLength());
            servletRequest.setAttribute("servletAPI.authType", servletRequest.getAuthType());
            servletRequest.setAttribute("servletAPI.contentType", servletRequest.getContentType());
            servletRequest.setAttribute("servletAPI.scheme", servletRequest.getScheme());

            Request request = new RequestImpl(servletRequest, applicationCharset, sessionFactory);
            Response response = new ResponseImpl(servletRequest, servletResponse);

            // TAP5-257: Make sure that the "initial guess" for request/response
            // is available, even if
            // some filter in the RequestHandler pipeline replaces them.

            requestGlobals.storeRequestResponse(request, response);

            // Transition from the Servlet API-based pipeline, to the
            // Tapestry-based pipeline.

            return handler.service(request, response);
        }
    }

    /**
     * @since 5.1.0.0
     */
    private class ServletApplicationInitializerTerminator implements ServletApplicationInitializer
    {
        private final ApplicationInitializer initializer;

        public ServletApplicationInitializerTerminator(ApplicationInitializer initializer)
        {
            this.initializer = initializer;
        }

        public void initializeApplication(ServletContext servletContext)
        {
            applicationGlobals.storeServletContext(servletContext);

            // And now, down the (Web) ApplicationInitializer pipeline ...

            ContextImpl context = new ContextImpl(servletContext);

            applicationGlobals.storeContext(context);

            initializer.initializeApplication(context);
        }
    }

    /**
     * @since 5.1.0.0
     */
    private class RequestHandlerTerminator implements RequestHandler
    {
        private final Dispatcher masterDispatcher;

        public RequestHandlerTerminator(Dispatcher masterDispatcher)
        {
            this.masterDispatcher = masterDispatcher;
        }

        public boolean service(Request request, Response response) throws IOException
        {
            // Update RequestGlobals with the current request/response (in case
            // some filter replaced the
            // normal set).
            requestGlobals.storeRequestResponse(request, response);

            return masterDispatcher.dispatch(request, response);
        }
    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(ClasspathAssetAliasManager.class, ClasspathAssetAliasManagerImpl.class);
        binder.bind(PersistentLocale.class, PersistentLocaleImpl.class);
        binder.bind(ApplicationStateManager.class, ApplicationStateManagerImpl.class);
        binder.bind(ApplicationStatePersistenceStrategySource.class,
                ApplicationStatePersistenceStrategySourceImpl.class);
        binder.bind(BindingSource.class, BindingSourceImpl.class);
        binder.bind(FieldValidatorSource.class, FieldValidatorSourceImpl.class);
        binder.bind(ApplicationGlobals.class, ApplicationGlobalsImpl.class);
        binder.bind(AssetSource.class, AssetSourceImpl.class);
        binder.bind(Cookies.class, CookiesImpl.class);
        binder.bind(FieldValidatorDefaultSource.class, FieldValidatorDefaultSourceImpl.class);
        binder.bind(RequestGlobals.class, RequestGlobalsImpl.class);
        binder.bind(ResourceDigestGenerator.class, ResourceDigestGeneratorImpl.class);
        binder.bind(ValidationConstraintGenerator.class, ValidationConstraintGeneratorImpl.class);
        binder.bind(EnvironmentalShadowBuilder.class, EnvironmentalShadowBuilderImpl.class);
        binder.bind(ComponentSource.class, ComponentSourceImpl.class);
        binder.bind(BeanModelSource.class, BeanModelSourceImpl.class);
        binder.bind(BeanBlockSource.class, BeanBlockSourceImpl.class);
        binder.bind(ComponentDefaultProvider.class, ComponentDefaultProviderImpl.class);
        binder.bind(MarkupWriterFactory.class, MarkupWriterFactoryImpl.class);
        binder.bind(FieldValidationSupport.class, FieldValidationSupportImpl.class);
        binder.bind(ObjectRenderer.class, LocationRenderer.class).withSimpleId();
        binder.bind(ObjectProvider.class, AssetObjectProvider.class).withSimpleId();
        binder.bind(RequestExceptionHandler.class, DefaultRequestExceptionHandler.class);
        binder.bind(ComponentEventResultProcessor.class, ComponentInstanceResultProcessor.class).withSimpleId();
        binder.bind(NullFieldStrategySource.class, NullFieldStrategySourceImpl.class);
        binder.bind(HttpServletRequestFilter.class, IgnoredPathsFilter.class).withSimpleId();
        binder.bind(ContextValueEncoder.class, ContextValueEncoderImpl.class);
        binder.bind(BaseURLSource.class, BaseURLSourceImpl.class);
        binder.bind(BeanBlockOverrideSource.class, BeanBlockOverrideSourceImpl.class);
        binder.bind(HiddenFieldLocationRules.class, HiddenFieldLocationRulesImpl.class);
        binder.bind(PageDocumentGenerator.class, PageDocumentGeneratorImpl.class);
        binder.bind(ResponseRenderer.class, ResponseRendererImpl.class);
        binder.bind(FieldTranslatorSource.class, FieldTranslatorSourceImpl.class);
        binder.bind(BindingFactory.class, MessageBindingFactory.class).withSimpleId();
        binder.bind(BindingFactory.class, ValidateBindingFactory.class).withSimpleId();
        binder.bind(BindingFactory.class, TranslateBindingFactory.class).withSimpleId();
        binder.bind(BindingFactory.class, AssetBindingFactory.class).withSimpleId();
        binder.bind(BindingFactory.class, ContextBindingFactory.class).withSimpleId();
        binder.bind(BindingFactory.class, NullFieldStrategyBindingFactory.class).withSimpleId();
        binder.bind(BindingFactory.class, SymbolBindingFactory.class).withSimpleId();
        binder.bind(URLEncoder.class, URLEncoderImpl.class);
        binder.bind(ContextPathEncoder.class, ContextPathEncoderImpl.class);
        binder.bind(ApplicationStatePersistenceStrategy.class, SessionApplicationStatePersistenceStrategy.class).withSimpleId();
        binder.bind(TapestrySessionFactory.class, TapestrySessionFactoryImpl.class);
        binder.bind(AssetPathConverter.class, IdentityAssetPathConverter.class);
        binder.bind(NumericTranslatorSupport.class);
        binder.bind(ClientDataEncoder.class, ClientDataEncoderImpl.class);
        binder.bind(ComponentEventLinkEncoder.class, ComponentEventLinkEncoderImpl.class);
        binder.bind(PageRenderLinkSource.class, PageRenderLinkSourceImpl.class);
        binder.bind(ValidatorMacro.class, ValidatorMacroImpl.class);
        binder.bind(PropertiesFileParser.class, PropertiesFileParserImpl.class);
        binder.bind(PageActivator.class, PageActivatorImpl.class);
        binder.bind(Dispatcher.class, AssetDispatcher.class).withSimpleId();
        binder.bind(AssetPathConstructor.class, AssetPathConstructorImpl.class);
        binder.bind(TranslatorAlternatesSource.class, TranslatorAlternatesSourceImpl.class);
        binder.bind(MetaWorker.class, MetaWorkerImpl.class);
        binder.bind(LinkTransformer.class, LinkTransformerImpl.class);
        binder.bind(SelectModelFactory.class, SelectModelFactoryImpl.class);
        binder.bind(DynamicTemplateParser.class, DynamicTemplateParserImpl.class);
        binder.bind(AjaxResponseRenderer.class, AjaxResponseRendererImpl.class);
        binder.bind(AlertManager.class, AlertManagerImpl.class);
        binder.bind(ValidationDecoratorFactory.class, ValidationDecoratorFactoryImpl.class);
        binder.bind(PropertyConduitSource.class, PropertyConduitSourceImpl.class);
        binder.bind(ClientWhitelist.class, ClientWhitelistImpl.class);
        binder.bind(AssetFactory.class, ClasspathAssetFactory.class).withSimpleId();
        binder.bind(MetaDataLocator.class, MetaDataLocatorImpl.class);
        binder.bind(ComponentClassCache.class, ComponentClassCacheImpl.class);
        binder.bind(PageActivationContextCollector.class, PageActivationContextCollectorImpl.class);
        binder.bind(StringInterner.class, StringInternerImpl.class);
        binder.bind(ValueEncoderSource.class, ValueEncoderSourceImpl.class);
    }

    // ========================================================================
    //
    // Service Builder Methods (static)
    //
    // ========================================================================

    // ========================================================================
    //
    // Service Contribution Methods (static)
    //
    // ========================================================================

    /**
     * Contributes the factory for serveral built-in binding prefixes ("asset",
     * "block", "component", "literal", prop",
     * "nullfieldstrategy", "message", "validate", "translate", "var").
     */
    public static void contributeBindingSource(MappedConfiguration<String, BindingFactory> configuration,

                                               @InjectService("PropBindingFactory")
                                               BindingFactory propBindingFactory,

                                               @InjectService("MessageBindingFactory")
                                               BindingFactory messageBindingFactory,

                                               @InjectService("ValidateBindingFactory")
                                               BindingFactory validateBindingFactory,

                                               @InjectService("TranslateBindingFactory")
                                               BindingFactory translateBindingFactory,

                                               @InjectService("AssetBindingFactory")
                                               BindingFactory assetBindingFactory,

                                               @InjectService("NullFieldStrategyBindingFactory")
                                               BindingFactory nullFieldStrategyBindingFactory,

                                               @InjectService("ContextBindingFactory")
                                               BindingFactory contextBindingFactory,

                                               @InjectService("SymbolBindingFactory")
                                               BindingFactory symbolBindingFactory)
    {
        configuration.add(BindingConstants.LITERAL, new LiteralBindingFactory());
        configuration.add(BindingConstants.COMPONENT, new ComponentBindingFactory());
        configuration.add(BindingConstants.VAR, new RenderVariableBindingFactory());
        configuration.add(BindingConstants.BLOCK, new BlockBindingFactory());

        configuration.add(BindingConstants.PROP, propBindingFactory);
        configuration.add(BindingConstants.MESSAGE, messageBindingFactory);
        configuration.add(BindingConstants.VALIDATE, validateBindingFactory);
        configuration.add(BindingConstants.TRANSLATE, translateBindingFactory);
        configuration.add(BindingConstants.ASSET, assetBindingFactory);
        configuration.add(BindingConstants.NULLFIELDSTRATEGY, nullFieldStrategyBindingFactory);
        configuration.add(BindingConstants.CONTEXT, contextBindingFactory);
        configuration.add(BindingConstants.SYMBOL, symbolBindingFactory);
    }

    @Contribute(ClasspathAssetAliasManager.class)
    public static void addMappingsForLibraryVirtualFolders(MappedConfiguration<String, String> configuration,
                                                           ComponentClassResolver resolver)
    {
        // Each library gets a mapping or its folder automatically

        Map<String, String> folderToPackageMapping = resolver.getFolderToPackageMapping();

        for (String folder : folderToPackageMapping.keySet())
        {
            // This is the 5.3 version, which is still supported:
            configuration.add(folder, toPackagePath(folderToPackageMapping.get(folder)));

            // This is the 5.4 version; once 5.3 support is dropped, this can be simplified, and the
            // "meta/" prefix stripped out.

            String folderSuffix = folder.equals("") ? folder : "/" + folder;

            configuration.add("meta" + folderSuffix, "META-INF/assets" + folderSuffix);
        }
    }

    @Contribute(ClasspathAssetAliasManager.class)
    public static void addApplicationAndTapestryMappings(MappedConfiguration<String, String> configuration,

                                                         @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                         String appPackage)
    {
        configuration.add("tapestry", "org/apache/tapestry5");

        configuration.add("app", toPackagePath(appPackage));
    }

    /**
     * Contributes an handler for each mapped classpath alias, as well handlers for context assets
     * and stack assets (combined {@link JavaScriptStack} files).
     */
    @Contribute(Dispatcher.class)
    @AssetRequestDispatcher
    public static void provideBuiltinAssetDispatchers(MappedConfiguration<String, AssetRequestHandler> configuration,

                                                      @ContextProvider
                                                      AssetFactory contextAssetFactory,

                                                      @Autobuild
                                                      StackAssetRequestHandler stackAssetRequestHandler,

                                                      ClasspathAssetAliasManager classpathAssetAliasManager, ResourceStreamer streamer,
                                                      AssetResourceLocator assetResourceLocator)
    {
        Map<String, String> mappings = classpathAssetAliasManager.getMappings();

        for (String folder : mappings.keySet())
        {
            String path = mappings.get(folder);

            configuration.add(folder, new ClasspathAssetRequestHandler(streamer, assetResourceLocator, path));
        }

        configuration.add(RequestConstants.CONTEXT_FOLDER,
                new ContextAssetRequestHandler(streamer, contextAssetFactory.getRootResource()));

        configuration.add(RequestConstants.STACK_FOLDER, stackAssetRequestHandler);

    }

    private static String toPackagePath(String packageName)
    {
        return packageName.replace('.', '/');
    }

    @Contribute(ComponentClassResolver.class)
    public static void setupCoreAndAppLibraries(Configuration<LibraryMapping> configuration,
                                                @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                String appRootPackage)
    {
        configuration.add(new LibraryMapping(InternalConstants.CORE_LIBRARY, "org.apache.tapestry5.corelib"));
        configuration.add(new LibraryMapping("t5internal", "org.apache.tapestry5.internal.t5internal"));
        configuration.add(new LibraryMapping("", appRootPackage));
    }

    /**
     * Adds a number of standard component class transform workers:
     * <dl>
     * <dt>Parameter</dt>
     * <dd>Identifies parameters based on the {@link org.apache.tapestry5.annotations.Parameter} annotation</dd>
     * <dt>BindParameter</dt>
     * <dd>Support for the {@link BindParameter} annotation</dd>
     * <dt>Property</dt>
     * <dd>Generates accessor methods if {@link org.apache.tapestry5.annotations.Property} annotation is present</dd>
     * <dt>Import</dt>
     * <dd>Supports the {@link Import} annotation</dd>
     * <dt>UnclaimedField</dt>
     * <dd>Manages unclaimed fields, storing their value in a {@link PerThreadValue}</dd>
     * <dt>OnEvent</dt>
     * <dd>Handle the @OnEvent annotation, and related naming convention</dd>
     * <dt>RenderCommand</dt>
     * <dd>Ensures all components also implement {@link org.apache.tapestry5.runtime.RenderCommand}</dd>
     * <dt>SupportsInformalParameters</dt>
     * <dd>Checks for the annotation</dd>
     * <dt>RenderPhase</dt>
     * <dd>Link in render phase methods</dd>
     * <dt>Retain</dt>
     * <dd>Allows fields to retain their values between requests</dd>
     * <dt>Meta</dt>
     * <dd>Checks for meta data annotations and adds it to the component model</dd>
     * <dt>PageActivationContext</dt> <dd>Support for {@link PageActivationContext} annotation</dd>
     * <dt>DiscardAfter</dt> <dd>Support for {@link DiscardAfter} method annotation </dd>
     * <dt>MixinAfter</dt> <dd>Support for the {@link MixinAfter} mixin class annotation</dd>
     * <dt>PageReset</dt>
     * <dd>Checks for the {@link PageReset} annotation</dd>
     * <dt>Mixin</dt>
     * <dd>Adds a mixin as part of a component's implementation</dd>
     * <dt>Cached</dt>
     * <dd>Checks for the {@link org.apache.tapestry5.annotations.Cached} annotation</dd>
     * <dt>ActivationRequestParameter</dt>
     * <dd>Support for the {@link ActivationRequestParameter} annotation</dd>
     * <dt>PageLoaded, PageAttached, PageDetached</dt>
     * <dd>Support for annotations {@link PageLoaded}, {@link PageAttached}, {@link PageDetached}</dd>
     * <dt>InjectService</dt>
     * <dd>Handles the {@link org.apache.tapestry5.ioc.annotations.InjectService} annotation</dd>
     * <dt>Component</dt>
     * <dd>Defines embedded components based on the {@link org.apache.tapestry5.annotations.Component} annotation</dd>
     * <dt>Environment</dt>
     * <dd>Allows fields to contain values extracted from the {@link org.apache.tapestry5.services.Environment} service</dd>
     * <dt>ApplicationState</dt>
     * <dd>Converts fields that reference application state objects</dd>
     * <dt>Persist</dt>
     * <dd>Allows fields to store their their value persistently between requests via {@link Persist}</dd>
     * <dt>SessionAttribute</dt>
     * <dd>Support for the {@link SessionAttribute}</dd>
     * <dt>Log</dt>
     * <dd>Checks for the {@link org.apache.tapestry5.annotations.Log} annotation</dd>
     * <dt>HeartbeatDeferred
     * <dd>Support for the {@link HeartbeatDeferred} annotation, which defers method invocation to the end of the {@link Heartbeat}
     * <dt>Inject</dt>
     * <dd>Used with the {@link org.apache.tapestry5.ioc.annotations.Inject} annotation, when a value is supplied</dd>
     * </dl>
     * <dd>Operation</dt> <dd>Support for the {@link Operation} method annotation</dd></dd>
     */
    @Contribute(ComponentClassTransformWorker2.class)
    @Primary
    public static void provideTransformWorkers(
            OrderedConfiguration<ComponentClassTransformWorker2> configuration,
            MetaWorker metaWorker,
            ComponentClassResolver resolver)
    {
        configuration.add("Property", new PropertyWorker());

        // Order this one pretty early:

        configuration.addInstance("Operation", OperationWorker.class);

        configuration.add("RenderCommand", new RenderCommandWorker());

        configuration.addInstance("OnEvent", OnEventWorker.class);

        configuration.add("MixinAfter", new MixinAfterWorker());

        // These must come after Property, since they actually delete fields
        // that may still have the annotation
        configuration.addInstance("ApplicationState", ApplicationStateWorker.class);
        configuration.addInstance("Environment", EnvironmentalWorker.class);

        configuration.add("Component", new ComponentWorker(resolver));
        configuration.add("Mixin", new MixinWorker(resolver));
        configuration.addInstance("InjectPage", InjectPageWorker.class);
        configuration.addInstance("InjectComponent", InjectComponentWorker.class);
        configuration.addInstance("InjectContainer", InjectContainerWorker.class);

        // Default values for parameters are often some form of injection, so
        // make sure that Parameter fields are processed after injections.

        configuration.addInstance("Parameter", ParameterWorker.class);

        // bind parameter should always go after parameter to make sure all
        // parameters have been properly setup.
        configuration.addInstance("BindParameter", BindParameterWorker.class);

        configuration.add("SupportsInformalParameters", new SupportsInformalParametersWorker());

        configuration.addInstance("RenderPhase", RenderPhaseMethodWorker.class);

        // Import advises methods, usually render phase methods, so it must come after RenderPhase.

        configuration.addInstance("Import", ImportWorker.class);

        configuration.add("Meta", metaWorker.getWorker());

        configuration.add("Retain", new RetainWorker());

        configuration.add("PageActivationContext", new PageActivationContextWorker());
        configuration
                .addInstance("ActivationRequestParameter", ActivationRequestParameterWorker.class);

        configuration.addInstance("Cached", CachedWorker.class);

        configuration.addInstance("DiscardAfter", DiscardAfterWorker.class);

        add(configuration, PageLoaded.class, TransformConstants.CONTAINING_PAGE_DID_LOAD_DESCRIPTION);
        add(configuration, PageAttached.class, TransformConstants.CONTAINING_PAGE_DID_ATTACH_DESCRIPTION);
        add(configuration, PageDetached.class, TransformConstants.CONTAINING_PAGE_DID_DETACH_DESCRIPTION);

        configuration.addInstance("PageReset", PageResetAnnotationWorker.class);
        configuration.addInstance("InjectService", InjectServiceWorker.class);

        configuration.addInstance("Inject", InjectWorker.class);

        configuration.addInstance("Persist", PersistWorker.class);

        configuration.addInstance("SessionAttribute", SessionAttributeWorker.class);

        configuration.addInstance("Log", LogWorker.class);

        configuration.addInstance("HeartbeatDeferred", HeartbeatDeferredWorker.class);

        // This one is always last. Any additional private fields that aren't
        // annotated will
        // be converted to clear out at the end of the request.

        configuration.addInstance("UnclaimedField", UnclaimedFieldWorker.class, "after:*");
    }

    /**
     * <dl>
     * <dt>Annotation</dt>
     * <dd>Checks for {@link org.apache.tapestry5.beaneditor.DataType} annotation</dd>
     * <dt>Default (ordered last)</dt>
     * <dd>
     * {@link org.apache.tapestry5.internal.services.DefaultDataTypeAnalyzer} service (
     * {@link #contributeDefaultDataTypeAnalyzer(org.apache.tapestry5.ioc.MappedConfiguration)} )</dd>
     * </dl>
     */
    public static void contributeDataTypeAnalyzer(OrderedConfiguration<DataTypeAnalyzer> configuration,
                                                  @InjectService("DefaultDataTypeAnalyzer")
                                                  DataTypeAnalyzer defaultDataTypeAnalyzer)
    {
        configuration.add("Annotation", new AnnotationDataTypeAnalyzer());
        configuration.add("Default", defaultDataTypeAnalyzer, "after:*");
    }

    /**
     * Maps property types to data type names:
     * <ul>
     * <li>String --&gt; text
     * <li>Number --&gt; number
     * <li>Enum --&gt; enum
     * <li>Boolean --&gt; boolean
     * <li>Date --&gt; date
     * </ul>
     */
    public static void contributeDefaultDataTypeAnalyzer(MappedConfiguration<Class, String> configuration)
    {
        // This is a special case contributed to avoid exceptions when a
        // property type can't be
        // matched. DefaultDataTypeAnalyzer converts the empty string to null.

        configuration.add(Object.class, "");

        configuration.add(String.class, DataTypeConstants.TEXT);
        configuration.add(Number.class, DataTypeConstants.NUMBER);
        configuration.add(Enum.class, DataTypeConstants.ENUM);
        configuration.add(Boolean.class, DataTypeConstants.BOOLEAN);
        configuration.add(Date.class, DataTypeConstants.DATE);
        configuration.add(Calendar.class, DataTypeConstants.CALENDAR);
    }

    @Contribute(BeanBlockSource.class)
    public static void provideDefaultBeanBlocks(Configuration<BeanBlockContribution> configuration)
    {
        addEditBlock(configuration, DataTypeConstants.TEXT);
        addEditBlock(configuration, DataTypeConstants.NUMBER);
        addEditBlock(configuration, DataTypeConstants.ENUM);
        addEditBlock(configuration, DataTypeConstants.BOOLEAN);
        addEditBlock(configuration, DataTypeConstants.DATE);
        addEditBlock(configuration, DataTypeConstants.PASSWORD);
        addEditBlock(configuration, DataTypeConstants.CALENDAR);

        // longtext uses a text area, not a text field

        addEditBlock(configuration, DataTypeConstants.LONG_TEXT);

        addDisplayBlock(configuration, DataTypeConstants.ENUM);
        addDisplayBlock(configuration, DataTypeConstants.DATE);
        addDisplayBlock(configuration, DataTypeConstants.CALENDAR);

        // Password and long text have special output needs.
        addDisplayBlock(configuration, DataTypeConstants.PASSWORD);
        addDisplayBlock(configuration, DataTypeConstants.LONG_TEXT);
    }

    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType)
    {
        addEditBlock(configuration, dataType, dataType);
    }

    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType, String blockId)
    {
        configuration.add(new EditBlockContribution(dataType, "PropertyEditBlocks", blockId));
    }

    private static void addDisplayBlock(Configuration<BeanBlockContribution> configuration, String dataType)
    {
        addDisplayBlock(configuration, dataType, dataType);
    }

    private static void addDisplayBlock(Configuration<BeanBlockContribution> configuration, String dataType,
                                        String blockId)
    {
        configuration.add(new DisplayBlockContribution(dataType, "PropertyDisplayBlocks", blockId));
    }

    /**
     * Contributes the basic set of validators:
     * <ul>
     * <li>required</li>
     * <li>minlength</li>
     * <li>maxlength</li>
     * <li>min</li>
     * <li>max</li>
     * <li>regexp</li>
     * <li>email</li>
     * <li>none</li>
     * </ul>
     */
    public static void contributeFieldValidatorSource(MappedConfiguration<String, Validator> configuration)
    {
        configuration.add("required", new Required());
        configuration.add("minlength", new MinLength());
        configuration.add("maxlength", new MaxLength());
        configuration.add("min", new Min());
        configuration.add("max", new Max());
        configuration.add("regexp", new Regexp());
        configuration.add("email", new Email());
        configuration.add("none", new None());
    }

    /**
     * <dl>
     * <dt>Default</dt>
     * <dd>based on {@link MasterObjectProvider}</dd>
     * <dt>Named</dt> <dd>Handles fields with the {@link javax.inject.Named} annotation</dd>
     * <dt>Block</dt>
     * <dd>injects fields of type {@link Block}</dd>
     * <dt>CommonResources</dt>
     * <dd>Access to properties of resources (log, messages, etc.)</dd>
     * <dt>Asset</dt>
     * <dd>injection of assets (triggered via {@link Path} annotation), with the path relative to the component class</dd>
     * <dt>Service</dt>
     * <dd>Ordered last, for use when Inject is present and nothing else works, matches field type against Tapestry IoC
     * services</dd>
     * </dl>
     */
    @Contribute(InjectionProvider2.class)
    public static void provideStandardInjectionProviders(OrderedConfiguration<InjectionProvider2> configuration, SymbolSource symbolSource,

                                                         AssetSource assetSource)
    {
        configuration.addInstance("Named", InjectNamedProvider.class);
        configuration.add("Block", new BlockInjectionProvider());
        configuration.add("Asset", new AssetInjectionProvider(assetSource));

        configuration.add("CommonResources", new CommonResourcesInjectionProvider());

        configuration.addInstance("Default", DefaultInjectionProvider.class);

        // This needs to be the last one, since it matches against services
        // and might blow up if there is no match.
        configuration.addInstance("Service", ServiceInjectionProvider.class, "after:*");
    }

    /**
     * Contributes two object providers:
     * <dl>
     * <dt>Asset
     * <dt>
     * <dd>Checks for the {@link Path} annotation, and injects an {@link Asset}</dd>
     * <dt>Service</dt>
     * <dd>Injects based on the {@link Service} annotation, if present</dd>
     * <dt>ApplicationMessages</dt>
     * <dd>Injects the global application messages</dd>
     * </dl>
     */
    public static void contributeMasterObjectProvider(OrderedConfiguration<ObjectProvider> configuration,

                                                      @InjectService("AssetObjectProvider")
                                                      ObjectProvider assetObjectProvider,

                                                      ObjectLocator locator)
    {
        configuration.add("Asset", assetObjectProvider, "before:AnnotationBasedContributions");

        configuration.add("Service", new ServiceAnnotationObjectProvider(), "before:AnnotationBasedContributions");

        configuration.add("ApplicationMessages", new ApplicationMessageCatalogObjectProvider(locator),
                "before:AnnotationBasedContributions");

    }

    /**
     * <dl>
     * <dt>StoreIntoGlobals</dt>
     * <dd>Stores the request and response into {@link org.apache.tapestry5.services.RequestGlobals} at the start of the
     * pipeline</dd>
     * <dt>IgnoredPaths</dt>
     * <dd>Identifies requests that are known (via the IgnoredPathsFilter service's configuration) to be mapped to other
     * applications</dd>
     * <dt>GZip</dt>
     * <dd>Handles GZIP compression of response streams (if supported by client)</dd>
     */
    public void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration,

                                                    @Symbol(SymbolConstants.GZIP_COMPRESSION_ENABLED)
                                                    boolean gzipCompressionEnabled,

                                                    @Autobuild
                                                    GZipFilter gzipFilter,

                                                    @InjectService("IgnoredPathsFilter")
                                                    HttpServletRequestFilter ignoredPathsFilter)
    {
        configuration.add("IgnoredPaths", ignoredPathsFilter);

        configuration.add("GZIP", gzipCompressionEnabled ? gzipFilter : null);

        HttpServletRequestFilter storeIntoGlobals = new HttpServletRequestFilter()
        {
            public boolean service(HttpServletRequest request, HttpServletResponse response,
                                   HttpServletRequestHandler handler) throws IOException
            {
                requestGlobals.storeServletRequestResponse(request, response);

                return handler.service(request, response);
            }
        };

        configuration.add("StoreIntoGlobals", storeIntoGlobals, "before:*");
    }

    /**
     * Continues a number of filters into the RequestHandler service:
     * <dl>
     * <dt>StaticFiles</dt>
     * <dd>Checks to see if the request is for an actual file, if so, returns true to let the servlet container process
     * the request</dd>
     * <dt>CheckForUpdates</dt>
     * <dd>Periodically fires events that checks to see if the file system sources for any cached data has changed (see
     * {@link org.apache.tapestry5.internal.services.CheckForUpdatesFilter}). Starting in 5.3, this filter will be null
     * in production mode (it will only be active in development mode).
     * <dt>ErrorFilter</dt>
     * <dd>Catches request errors and lets the {@link org.apache.tapestry5.services.RequestExceptionHandler} handle them
     * </dd>
     * <dt>StoreIntoGlobals</dt>
     * <dd>Stores the request and response into the {@link org.apache.tapestry5.services.RequestGlobals} service (this
     * is repeated at the end of the pipeline, in case any filter substitutes the request or response).
     * <dt>EndOfRequest</dt>
     * <dd>Notifies internal services that the request has ended</dd>
     * </dl>
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration, Context context,

                                         @Symbol(SymbolConstants.PRODUCTION_MODE)
                                         boolean productionMode)
    {
        RequestFilter staticFilesFilter = new StaticFilesFilter(context);

        RequestFilter storeIntoGlobals = new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler) throws IOException
            {
                requestGlobals.storeRequestResponse(request, response);

                return handler.service(request, response);
            }
        };

        RequestFilter fireEndOfRequestEvent = new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler) throws IOException
            {
                try
                {
                    return handler.service(request, response);
                } finally
                {
                    endOfRequestEventHub.fire();
                }
            }
        };

        if (productionMode)
        {
            configuration.add("CheckForUpdates", null, "before:*");
        } else
        {
            configuration.addInstance("CheckForUpdates", CheckForUpdatesFilter.class, "before:*");
        }

        configuration.add("StaticFiles", staticFilesFilter);

        configuration.add("StoreIntoGlobals", storeIntoGlobals);

        configuration.add("EndOfRequest", fireEndOfRequestEvent);

        configuration.addInstance("ErrorFilter", RequestErrorFilter.class);
    }

    /**
     * Contributes the basic set of translators:
     * <ul>
     * <li>string</li>
     * <li>byte</li>
     * <li>short</li>
     * <li>integer</li>
     * <li>long</li>
     * <li>float</li>
     * <li>double</li>
     * <li>BigInteger</li>
     * <li>BigDecimal</li>
     * </ul>
     */
    public static void contributeTranslatorSource(MappedConfiguration<Class, Translator> configuration,
                                                  NumericTranslatorSupport support)
    {

        configuration.add(String.class, new StringTranslator());

        Class[] types = new Class[]
                {Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class,
                        BigDecimal.class};

        for (Class type : types)
        {
            String name = type.getSimpleName().toLowerCase();

            configuration.add(type, new NumericTranslator(name, type, support));
        }
    }

    /**
     * Adds coercions:
     * <ul>
     * <li>String to {@link SelectModel}
     * <li>Map to {@link SelectModel}
     * <li>Collection to {@link GridDataSource}
     * <li>null to {@link GridDataSource}
     * <li>List to {@link SelectModel}
     * <li>{@link ComponentResourcesAware} (typically, a component) to {@link ComponentResources}
     * <li>{@link ComponentResources} to {@link PropertyOverrides}
     * <li>String to {@link Renderable}
     * <li>{@link Renderable} to {@link Block}
     * <li>String to {@link DateFormat}
     * <li>String to {@link Resource} (via {@link AssetSource#resourceForPath(String)})
     * <li>{@link Renderable} to {@link RenderCommand}</li>
     * <li>String to {@link Pattern}</li>
     * <li>String to {@link DateFormat}</li>
     * <li>{@link Resource} to {@link DynamicTemplate}</li>
     * <li>{@link Asset} to {@link Resource}</li>
     * <li>{@link ValueEncoder} to {@link ValueEncoderFactory}</li>
     * </ul>
     */
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration,

                                             @Builtin
                                             TypeCoercer coercer,

                                             @Builtin
                                             final ThreadLocale threadLocale,

                                             @Core
                                             final AssetSource assetSource,

                                             @Core
                                             final DynamicTemplateParser dynamicTemplateParser)
    {
        configuration.add(CoercionTuple.create(ComponentResources.class, PropertyOverrides.class,
                new Coercion<ComponentResources, PropertyOverrides>()
                {
                    public PropertyOverrides coerce(ComponentResources input)
                    {
                        return new PropertyOverridesImpl(input);
                    }
                }));

        configuration.add(CoercionTuple.create(String.class, SelectModel.class, new Coercion<String, SelectModel>()
        {
            public SelectModel coerce(String input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        }));

        configuration.add(CoercionTuple.create(Map.class, SelectModel.class, new Coercion<Map, SelectModel>()
        {
            @SuppressWarnings("unchecked")
            public SelectModel coerce(Map input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        }));

        configuration.add(CoercionTuple.create(Collection.class, GridDataSource.class,
                new Coercion<Collection, GridDataSource>()
                {
                    public GridDataSource coerce(Collection input)
                    {
                        return new CollectionGridDataSource(input);
                    }
                }));

        configuration.add(CoercionTuple.create(void.class, GridDataSource.class, new Coercion<Void, GridDataSource>()
        {
            private final GridDataSource source = new NullDataSource();

            public GridDataSource coerce(Void input)
            {
                return source;
            }
        }));

        configuration.add(CoercionTuple.create(List.class, SelectModel.class, new Coercion<List, SelectModel>()
        {
            @SuppressWarnings("unchecked")
            public SelectModel coerce(List input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        }));

        configuration.add(CoercionTuple.create(String.class, Pattern.class, new Coercion<String, Pattern>()
        {
            public Pattern coerce(String input)
            {
                return Pattern.compile(input);
            }
        }));

        configuration.add(CoercionTuple.create(ComponentResourcesAware.class, ComponentResources.class,
                new Coercion<ComponentResourcesAware, ComponentResources>()
                {

                    public ComponentResources coerce(ComponentResourcesAware input)
                    {
                        return input.getComponentResources();
                    }
                }));

        configuration.add(CoercionTuple.create(String.class, Renderable.class, new Coercion<String, Renderable>()
        {
            public Renderable coerce(String input)
            {
                return new StringRenderable(input);
            }
        }));

        configuration.add(CoercionTuple.create(Renderable.class, Block.class, new Coercion<Renderable, Block>()
        {
            public Block coerce(Renderable input)
            {
                return new RenderableAsBlock(input);
            }
        }));

        configuration.add(CoercionTuple.create(String.class, DateFormat.class, new Coercion<String, DateFormat>()
        {
            public DateFormat coerce(String input)
            {
                return new SimpleDateFormat(input, threadLocale.getLocale());
            }
        }));

        configuration.add(CoercionTuple.create(String.class, Resource.class, new Coercion<String, Resource>()
        {
            public Resource coerce(String input)
            {
                return assetSource.resourceForPath(input);
            }
        }));

        configuration.add(CoercionTuple.create(Renderable.class, RenderCommand.class,
                new Coercion<Renderable, RenderCommand>()
                {
                    public RenderCommand coerce(final Renderable input)
                    {
                        return new RenderCommand()
                        {
                            public void render(MarkupWriter writer, RenderQueue queue)
                            {
                                input.render(writer);
                            }
                        };
                    }
                }));

        configuration.add(CoercionTuple.create(Date.class, Calendar.class, new Coercion<Date, Calendar>()
        {
            public Calendar coerce(Date input)
            {
                Calendar calendar = Calendar.getInstance(threadLocale.getLocale());
                calendar.setTime(input);
                return calendar;
            }
        }));

        configuration.add(CoercionTuple.create(Resource.class, DynamicTemplate.class,
                new Coercion<Resource, DynamicTemplate>()
                {
                    public DynamicTemplate coerce(Resource input)
                    {
                        return dynamicTemplateParser.parseTemplate(input);
                    }
                }));

        configuration.add(CoercionTuple.create(Asset.class, Resource.class, new Coercion<Asset, Resource>()
        {
            public Resource coerce(Asset input)
            {
                return input.getResource();
            }
        }));

        configuration.add(CoercionTuple.create(ValueEncoder.class, ValueEncoderFactory.class, new Coercion<ValueEncoder, ValueEncoderFactory>()
        {
            public ValueEncoderFactory coerce(ValueEncoder input)
            {
                return new GenericValueEncoderFactory(input);
            }
        }));
    }

    /**
     * Adds built-in constraint generators:
     * <ul>
     * <li>PrimtiveField -- primitive fields are always required
     * <li>ValidateAnnotation -- adds constraints from a {@link Validate} annotation
     * </ul>
     */
    public static void contributeValidationConstraintGenerator(
            OrderedConfiguration<ValidationConstraintGenerator> configuration)
    {
        configuration.add("PrimitiveField", new PrimitiveFieldConstraintGenerator());
        configuration.add("ValidateAnnotation", new ValidateAnnotationConstraintGenerator());
        configuration.addInstance("Messages", MessagesConstraintGenerator.class);
    }

    private static void add(OrderedConfiguration<ComponentClassTransformWorker2> configuration,
                            Class<? extends Annotation> annotationClass, MethodDescription description)
    {
        String name = TapestryInternalUtils.lastTerm(annotationClass.getName());

        ComponentClassTransformWorker2 worker = new PageLifecycleAnnotationWorker(annotationClass,
                description, name);

        configuration.add(name, worker);
    }

    // ========================================================================
    //
    // Service Builder Methods (instance)
    //
    // ========================================================================

    public Context buildContext(ApplicationGlobals globals)
    {
        return shadowBuilder.build(globals, "context", Context.class);
    }

    public static ComponentClassResolver buildComponentClassResolver(@Autobuild
                                                                     ComponentClassResolverImpl service, @ComponentClasses
    InvalidationEventHub hub)
    {
        // Allow the resolver to clean its cache when the component classes
        // change

        hub.addInvalidationListener(service);

        return service;
    }

    @Marker(ContextProvider.class)
    public AssetFactory buildContextAssetFactory(ApplicationGlobals globals,

                                                 AssetPathConstructor assetPathConstructor,

                                                 AssetPathConverter converter)
    {
        return new ContextAssetFactory(assetPathConstructor, globals.getContext(), converter);
    }

    /**
     * Builds the PropBindingFactory as a chain of command. The terminator of
     * the chain is responsible for ordinary
     * property names (and property paths).
     * <p/>
     * This mechanism has been replaced in 5.1 with a more sophisticated parser based on ANTLR. See <a
     * href="https://issues.apache.org/jira/browse/TAP5-79">TAP5-79</a> for details. There are no longer any built-in
     * contributions to the configuration.
     *
     * @param configuration
     *         contributions of special factories for some constants, each
     *         contributed factory may return a
     *         binding if applicable, or null otherwise
     */
    public BindingFactory buildPropBindingFactory(List<BindingFactory> configuration, @Autobuild
    PropBindingFactory service)
    {
        configuration.add(service);

        return chainBuilder.build(BindingFactory.class, configuration);
    }

    public PersistentFieldStrategy buildClientPersistentFieldStrategy(LinkCreationHub linkCreationHub, @Autobuild
    ClientPersistentFieldStrategy service)
    {
        linkCreationHub.addListener(service);

        return service;
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry5.services.ClientBehaviorSupport} inside this
     * thread's {@link org.apache.tapestry5.services.Environment}.
     *
     * @since 5.1.0.1
     */

    public ClientBehaviorSupport buildClientBehaviorSupport()
    {
        return environmentalBuilder.build(ClientBehaviorSupport.class);
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry5.services.FormSupport} inside this
     * thread's {@link org.apache.tapestry5.services.Environment}.
     */
    public FormSupport buildFormSupport()
    {
        return environmentalBuilder.build(FormSupport.class);
    }

    /**
     * Allows the exact steps in the component class transformation process to
     * be defined.
     */
    @Marker(Primary.class)
    public ComponentClassTransformWorker2 buildComponentClassTransformWorker(
            List<ComponentClassTransformWorker2> configuration)

    {
        return chainBuilder.build(ComponentClassTransformWorker2.class, configuration);
    }

    /**
     * Analyzes properties to determine the data types, used to
     * {@linkplain #provideDefaultBeanBlocks(org.apache.tapestry5.ioc.Configuration)} locale
     * display and edit blocks for properties. The default behaviors
     * look for a {@link org.apache.tapestry5.beaneditor.DataType} annotation
     * before deriving the data type from the property type.
     */
    @Marker(Primary.class)
    public DataTypeAnalyzer buildDataTypeAnalyzer(List<DataTypeAnalyzer> configuration)
    {
        return chainBuilder.build(DataTypeAnalyzer.class, configuration);
    }

    /**
     * A chain of command for providing values for {@link Inject}-ed fields in
     * component classes. The service's
     * configuration can be extended to allow for different automatic injections
     * (based on some combination of field
     * type and field name).
     * <p/>
     * Note that contributions to this service may be old-style {@link InjectionProvider}, which will
     * be coerced to {@link InjectionProvider2}.
     */
    public InjectionProvider2 buildInjectionProvider(List<InjectionProvider2> configuration)
    {
        return chainBuilder.build(InjectionProvider2.class, configuration);
    }

    /**
     * Initializes the application, using a pipeline of {@link org.apache.tapestry5.services.ApplicationInitializer}s.
     */
    @Marker(Primary.class)
    public ApplicationInitializer buildApplicationInitializer(Logger logger,
                                                              List<ApplicationInitializerFilter> configuration)
    {
        ApplicationInitializer terminator = new ApplicationInitializerTerminator();

        return pipelineBuilder.build(logger, ApplicationInitializer.class, ApplicationInitializerFilter.class,
                configuration, terminator);
    }

    public HttpServletRequestHandler buildHttpServletRequestHandler(Logger logger,

                                                                    List<HttpServletRequestFilter> configuration,

                                                                    @Primary
                                                                    RequestHandler handler,

                                                                    @Symbol(SymbolConstants.CHARSET)
                                                                    String applicationCharset,

                                                                    TapestrySessionFactory sessionFactory)
    {
        HttpServletRequestHandler terminator = new HttpServletRequestHandlerTerminator(handler, applicationCharset,
                sessionFactory);

        return pipelineBuilder.build(logger, HttpServletRequestHandler.class, HttpServletRequestFilter.class,
                configuration, terminator);
    }

    @Marker(Primary.class)
    public RequestHandler buildRequestHandler(Logger logger, List<RequestFilter> configuration,

                                              @Primary
                                              Dispatcher masterDispatcher)
    {
        RequestHandler terminator = new RequestHandlerTerminator(masterDispatcher);

        return pipelineBuilder.build(logger, RequestHandler.class, RequestFilter.class, configuration, terminator);
    }

    public ServletApplicationInitializer buildServletApplicationInitializer(Logger logger,
                                                                            List<ServletApplicationInitializerFilter> configuration,

                                                                            @Primary
                                                                            ApplicationInitializer initializer)
    {
        ServletApplicationInitializer terminator = new ServletApplicationInitializerTerminator(initializer);

        return pipelineBuilder.build(logger, ServletApplicationInitializer.class,
                ServletApplicationInitializerFilter.class, configuration, terminator);
    }

    /**
     * The component event result processor used for normal component requests.
     */
    @Marker(
            {Primary.class, Traditional.class})
    public ComponentEventResultProcessor buildComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration, @ComponentClasses
    InvalidationEventHub hub)
    {
        return constructComponentEventResultProcessor(configuration, hub);
    }

    /**
     * The component event result processor used for Ajax-oriented component
     * requests.
     */
    @Marker(Ajax.class)
    public ComponentEventResultProcessor buildAjaxComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration, @ComponentClasses
    InvalidationEventHub hub)
    {
        return constructComponentEventResultProcessor(configuration, hub);
    }

    private ComponentEventResultProcessor constructComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration, InvalidationEventHub hub)
    {
        Set<Class> handledTypes = CollectionFactory.newSet(configuration.keySet());

        // A slight hack!

        configuration.put(Object.class, new ObjectComponentEventResultProcessor(handledTypes));

        final StrategyRegistry<ComponentEventResultProcessor> registry = StrategyRegistry.newInstance(
                ComponentEventResultProcessor.class, configuration);

        //As the registry will cache component classes, we need to clear the cache when we reload components to avoid memory leaks in permgen
        hub.addInvalidationCallback(new Runnable()
        {
            @Override
            public void run()
            {
                registry.clearCache();
            }
        });

        return strategyBuilder.build(registry);
    }

    /**
     * The default data type analyzer is the final analyzer consulted and
     * identifies the type entirely pased on the
     * property type, working against its own configuration (mapping property
     * type class to data type).
     */
    public static DataTypeAnalyzer buildDefaultDataTypeAnalyzer(@Autobuild
                                                                DefaultDataTypeAnalyzer service, @ComponentClasses
    InvalidationEventHub hub)
    {
        hub.addInvalidationCallback(service);

        return service;
    }

    public static TranslatorSource buildTranslatorSource(Map<Class, Translator> configuration,
                                                         TranslatorAlternatesSource alternatesSource,
                                                         @ComponentClasses
                                                         InvalidationEventHub hub)
    {
        TranslatorSourceImpl service = new TranslatorSourceImpl(configuration,
                alternatesSource.getTranslatorAlternates());

        hub.addInvalidationCallback(service);

        return service;
    }

    @Marker(Primary.class)
    public ObjectRenderer buildObjectRenderer(Map<Class, ObjectRenderer> configuration)
    {
        return strategyBuilder.build(ObjectRenderer.class, configuration);
    }

    /**
     * Returns a {@link PlasticProxyFactory} that can be used to create extra classes around component classes. This
     * factory will be cleared whenever an underlying component class is discovered to have changed. Use of this
     * factory implies that your code will become aware of this (if necessary) to discard any cached object (alas,
     * this currently involves dipping into the internals side to register for the correct notifications). Failure to
     * properly clean up can result in really nasty PermGen space memory leaks.
     */
    @Marker(ComponentLayer.class)
    public PlasticProxyFactory buildComponentProxyFactory(ComponentInstantiatorSource source)
    {
        return shadowBuilder.build(source, "proxyFactory", PlasticProxyFactory.class);
    }

    /**
     * Ordered contributions to the MasterDispatcher service allow different URL
     * matching strategies to occur.
     */
    @Marker(Primary.class)
    public Dispatcher buildMasterDispatcher(List<Dispatcher> configuration)
    {
        return chainBuilder.build(Dispatcher.class, configuration);
    }

    /**
     * Builds a shadow of the RequestGlobals.request property. Note again that
     * the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Request buildRequest()
    {
        return shadowBuilder.build(requestGlobals, "request", Request.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.HTTPServletRequest property.
     * Generally, you should inject the {@link Request} service instead, as
     * future version of Tapestry may operate beyond just the servlet API.
     */
    public HttpServletRequest buildHttpServletRequest()
    {
        return shadowBuilder.build(requestGlobals, "HTTPServletRequest", HttpServletRequest.class);
    }

    /**
     * @since 5.1.0.0
     */
    public HttpServletResponse buildHttpServletResponse()
    {
        return shadowBuilder.build(requestGlobals, "HTTPServletResponse", HttpServletResponse.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.response property. Note again that
     * the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Response buildResponse()
    {
        return shadowBuilder.build(requestGlobals, "response", Response.class);
    }

    /**
     * The MarkupRenderer service is used to render a full page as markup.
     * Supports an ordered configuration of {@link org.apache.tapestry5.services.MarkupRendererFilter}s.
     */
    public MarkupRenderer buildMarkupRenderer(Logger logger, @Autobuild
    MarkupRendererTerminator terminator, List<MarkupRendererFilter> configuration)
    {
        return pipelineBuilder.build(logger, MarkupRenderer.class, MarkupRendererFilter.class, configuration,
                terminator);
    }

    /**
     * A wrapper around {@link org.apache.tapestry5.internal.services.PageRenderQueue} used for
     * partial page renders.
     * Supports an ordered configuration of {@link org.apache.tapestry5.services.PartialMarkupRendererFilter}s.
     */
    public PartialMarkupRenderer buildPartialMarkupRenderer(Logger logger,
                                                            List<PartialMarkupRendererFilter> configuration, @Autobuild
    PartialMarkupRendererTerminator terminator)
    {

        return pipelineBuilder.build(logger, PartialMarkupRenderer.class, PartialMarkupRendererFilter.class,
                configuration, terminator);
    }

    public PageRenderRequestHandler buildPageRenderRequestHandler(List<PageRenderRequestFilter> configuration,
                                                                  Logger logger, @Autobuild
    PageRenderRequestHandlerImpl terminator)
    {
        return pipelineBuilder.build(logger, PageRenderRequestHandler.class, PageRenderRequestFilter.class,
                configuration, terminator);
    }

    /**
     * Builds the component action request handler for traditional (non-Ajax)
     * requests. These typically result in a
     * redirect to a Tapestry render URL.
     */
    @Marker(
            {Traditional.class, Primary.class})
    public ComponentEventRequestHandler buildComponentEventRequestHandler(
            List<ComponentEventRequestFilter> configuration, Logger logger, @Autobuild
    ComponentEventRequestHandlerImpl terminator)
    {
        return pipelineBuilder.build(logger, ComponentEventRequestHandler.class, ComponentEventRequestFilter.class,
                configuration, terminator);
    }

    /**
     * Builds the action request handler for Ajax requests, based on a
     * {@linkplain org.apache.tapestry5.ioc.services.PipelineBuilder
     * pipeline} around {@link org.apache.tapestry5.internal.services.AjaxComponentEventRequestHandler} . Filters on
     * the
     * request handler are supported here as well.
     */
    @Marker(
            {Ajax.class, Primary.class})
    public ComponentEventRequestHandler buildAjaxComponentEventRequestHandler(
            List<ComponentEventRequestFilter> configuration, Logger logger, @Autobuild
    AjaxComponentEventRequestHandler terminator)
    {
        return pipelineBuilder.build(logger, ComponentEventRequestHandler.class, ComponentEventRequestFilter.class,
                configuration, terminator);
    }

    // ========================================================================
    //
    // Service Contribution Methods (instance)
    //
    // ========================================================================

    /**
     * Contributes the default "session" strategy.
     */
    public void contributeApplicationStatePersistenceStrategySource(
            MappedConfiguration<String, ApplicationStatePersistenceStrategy> configuration,

            @Local
            ApplicationStatePersistenceStrategy sessionStategy)
    {
        configuration.add("session", sessionStategy);
    }

    public void contributeAssetSource(MappedConfiguration<String, AssetFactory> configuration, @ContextProvider
    AssetFactory contextAssetFactory,

                                      @ClasspathProvider
                                      AssetFactory classpathAssetFactory)
    {
        configuration.add(AssetConstants.CONTEXT, contextAssetFactory);
        configuration.add(AssetConstants.CLASSPATH, classpathAssetFactory);
    }

    /**
     * Contributes handlers for the following types:
     * <dl>
     * <dt>Object</dt>
     * <dd>Failure case, added to provide a more useful exception message</dd>
     * <dt>{@link Link}</dt>
     * <dd>Sends a redirect to the link (which is typically a page render link)</dd>
     * <dt>String</dt>
     * <dd>Sends a page render redirect</dd>
     * <dt>Class</dt>
     * <dd>Interpreted as the class name of a page, sends a page render render redirect (this is more refactoring safe
     * than the page name)</dd>
     * <dt>{@link Component}</dt>
     * <dd>A page's root component (though a non-root component will work, but will generate a warning). A direct to the
     * containing page is sent.</dd>
     * <dt>{@link org.apache.tapestry5.StreamResponse}</dt>
     * <dd>The stream response is sent as the actual reply.</dd>
     * <dt>URL</dt>
     * <dd>Sends a redirect to a (presumably) external URL</dd>
     * </dl>
     */
    public void contributeComponentEventResultProcessor(@Traditional
                                                        @ComponentInstanceProcessor
                                                        ComponentEventResultProcessor componentInstanceProcessor,

                                                        MappedConfiguration<Class, ComponentEventResultProcessor> configuration)
    {
        configuration.add(Link.class, new ComponentEventResultProcessor<Link>()
        {
            public void processResultValue(Link value) throws IOException
            {
                response.sendRedirect(value);
            }
        });

        configuration.add(URL.class, new ComponentEventResultProcessor<URL>()
        {
            public void processResultValue(URL value) throws IOException
            {
                response.sendRedirect(value.toExternalForm());
            }
        });

        configuration.addInstance(HttpError.class, HttpErrorComponentEventResultProcessor.class);

        configuration.addInstance(String.class, PageNameComponentEventResultProcessor.class);

        configuration.addInstance(Class.class, ClassResultProcessor.class);

        configuration.add(Component.class, componentInstanceProcessor);

        configuration.addInstance(StreamResponse.class, StreamResponseResultProcessor.class);

        configuration.addInstance(StreamPageContent.class, StreamPageContentResultProcessor.class);
    }

    /**
     * Contributes handlers for the following types:
     * <dl>
     * <dt>Object</dt>
     * <dd>Failure case, added to provide more useful exception message</dd>
     * <dt>{@link RenderCommand}</dt>
     * <dd>Typically, a {@link org.apache.tapestry5.Block}</dd>
     * <dt>{@link org.apache.tapestry5.annotations.Component}</dt>
     * <dd>Renders the component and its body (unless its a page, in which case a redirect JSON response is sent)</dd>
     * <dt>{@link org.apache.tapestry5.json.JSONObject} or {@link org.apache.tapestry5.json.JSONArray}</dt>
     * <dd>The JSONObject is returned as a text/javascript response</dd>
     * <dt>{@link org.apache.tapestry5.StreamResponse}</dt>
     * <dd>The stream response is sent as the actual response</dd>
     * <dt>String</dt>
     * <dd>Interprets the value as a logical page name and sends a client response to redirect to that page</dd>
     * <dt>{@link org.apache.tapestry5.Link}</dt>
     * <dd>Sends a JSON response to redirect to the link</dd>
     * <dt>{@link Class}</dt>
     * <dd>Treats the class as a page class and sends a redirect for a page render for that page</dd>
     * <dt>{@link org.apache.tapestry5.ajax.MultiZoneUpdate}</dt>
     * <dd>Sends a single JSON response to update the content of multiple zones
     * </dl>
     * <p/>
     * In most cases, when you want to support a new type, you should convert it to one of the built-in supported types
     * (such as {@link RenderCommand}. You can then inject the master AjaxComponentEventResultProcessor (use the
     * {@link Ajax} marker annotation) and delegate to it.
     */
    @Contribute(ComponentEventResultProcessor.class)
    @Ajax
    public static void provideBaseAjaxComponentEventResultProcessors(
            MappedConfiguration<Class, ComponentEventResultProcessor> configuration)
    {
        configuration.addInstance(RenderCommand.class, RenderCommandComponentEventResultProcessor.class);
        configuration.addInstance(Component.class, AjaxComponentInstanceEventResultProcessor.class);
        configuration.addInstance(JSONObject.class, JSONObjectEventResultProcessor.class);
        configuration.addInstance(JSONArray.class, JSONArrayEventResultProcessor.class);
        configuration.addInstance(StreamResponse.class, StreamResponseResultProcessor.class);
        configuration.addInstance(String.class, AjaxPageNameComponentEventResultProcessor.class);
        configuration.addInstance(Link.class, AjaxLinkComponentEventResultProcessor.class);
        configuration.addInstance(Class.class, AjaxPageClassComponentEventResultProcessor.class);
        configuration.addInstance(MultiZoneUpdate.class, MultiZoneUpdateEventResultProcessor.class);
        configuration.addInstance(HttpError.class, HttpErrorComponentEventResultProcessor.class);
    }

    /**
     * The MasterDispatcher is a chain-of-command of individual Dispatchers,
     * each handling (like a servlet) a particular
     * kind of incoming request.
     * <dl>
     * <dt>RootPath</dt>
     * <dd>Renders the start page for the "/" request (outdated)</dd>
     * <dt>Asset</dt>
     * <dd>Provides access to assets (context, classpath and virtual) via {@link AssetDispatcher}</dd>
     * <dt>PageRender</dt>
     * <dd>Identifies the {@link org.apache.tapestry5.services.PageRenderRequestParameters} and forwards onto
     * {@link PageRenderRequestHandler}</dd>
     * <dt>ComponentEvent</dt>
     * <dd>Identifies the {@link ComponentEventRequestParameters} and forwards onto the
     * {@link ComponentEventRequestHandler}</dd>
     * </dl>
     */
    public static void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration,

                                                  @AssetRequestDispatcher
                                                  Dispatcher assetDispatcher)
    {
        // Looks for the root path and renders the start page. This is
        // maintained for compatibility
        // with earlier versions of Tapestry 5, it is recommended that an Index
        // page be used instead.

        configuration.addInstance("RootPath", RootPathDispatcher.class, "before:Asset");

        // This goes first because an asset to be streamed may have an file
        // extension, such as
        // ".html", that will confuse the later dispatchers.

        configuration.add("Asset", assetDispatcher, "before:ComponentEvent");

        configuration.addInstance("ComponentEvent", ComponentEventDispatcher.class, "before:PageRender");

        configuration.addInstance("PageRender", PageRenderDispatcher.class);
    }

    /**
     * Contributes a default object renderer for type Object, plus specialized
     * renderers for {@link org.apache.tapestry5.services.Request}, {@link org.apache.tapestry5.ioc.Location},
     * {@link org.apache.tapestry5.ComponentResources}, {@link org.apache.tapestry5.EventContext},
     * {@link AvailableValues},
     * List, and Object[].
     */
    @SuppressWarnings("unchecked")
    public void contributeObjectRenderer(MappedConfiguration<Class, ObjectRenderer> configuration,

                                         @InjectService("LocationRenderer")
                                         ObjectRenderer locationRenderer,

                                         final TypeCoercer typeCoercer)
    {
        configuration.add(Object.class, new DefaultObjectRenderer());

        configuration.addInstance(Request.class, RequestRenderer.class);

        configuration.add(Location.class, locationRenderer);

        ObjectRenderer preformatted = new ObjectRenderer<Object>()
        {
            public void render(Object object, MarkupWriter writer)
            {
                writer.element("pre");
                writer.write(typeCoercer.coerce(object, String.class));
                writer.end();
            }
        };

        configuration.addInstance(List.class, ListRenderer.class);
        configuration.addInstance(Object[].class, ObjectArrayRenderer.class);
        configuration.addInstance(ComponentResources.class, ComponentResourcesRenderer.class);
        configuration.addInstance(EventContext.class, EventContextRenderer.class);
        configuration.add(AvailableValues.class, new AvailableValuesRenderer());
    }

    /**
     * Adds page render filters, each of which provides an {@link org.apache.tapestry5.annotations.Environmental}
     * service. Filters
     * often provide {@link org.apache.tapestry5.annotations.Environmental} services needed by
     * components as they render.
     * <dl>
     * <dt>DocumentLinker</dt>
     * <dd>Provides {@link org.apache.tapestry5.internal.services.DocumentLinker}</dd>
     * <dt>InjectDefaultStylesheet</dt>
     * <dd>Injects the default stylesheet into all pages</dd></dt>
     * <dt>ClientBehaviorSupport</dt>
     * <dd>Provides {@link ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt>
     * <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>ValidationDecorator</dt>
     * <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (via {@link ValidationDecoratorFactory#newInstance(org.apache.tapestry5.MarkupWriter)})</dd>
     * </dl>
     */
    public void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration,

                                         final ModuleManager moduleManager,

                                         @Symbol(SymbolConstants.OMIT_GENERATOR_META)
                                         final boolean omitGeneratorMeta,

                                         @Symbol(SymbolConstants.TAPESTRY_VERSION)
                                         final String tapestryVersion,

                                         final ValidationDecoratorFactory validationDecoratorFactory,

                                         @Path("${tapestry.default-stylesheet}")
                                         final Asset defaultStylesheet)
    {
        MarkupRendererFilter documentLinker = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinkerImpl linker = new DocumentLinkerImpl(moduleManager, omitGeneratorMeta, tapestryVersion);

                environment.push(DocumentLinker.class, linker);

                renderer.renderMarkup(writer);

                environment.pop(DocumentLinker.class);

                linker.updateDocument(writer.getDocument());
            }
        };


        MarkupRendererFilter injectDefaultStylesheet = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                linker.addStylesheetLink(new StylesheetLink(defaultStylesheet.toClientURL()));

                renderer.renderMarkup(writer);
            }
        };

        MarkupRendererFilter clientBehaviorSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                JavaScriptSupport javascriptSupport = environment.peekRequired(JavaScriptSupport.class);

                ClientBehaviorSupportImpl clientBehaviorSupport = new ClientBehaviorSupportImpl(javascriptSupport,
                        environment);

                environment.push(ClientBehaviorSupport.class, clientBehaviorSupport);

                renderer.renderMarkup(writer);

                environment.pop(ClientBehaviorSupport.class);

                clientBehaviorSupport.commit();
            }
        };

        MarkupRendererFilter heartbeat = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                Heartbeat heartbeat = new HeartbeatImpl();

                heartbeat.begin();

                environment.push(Heartbeat.class, heartbeat);

                renderer.renderMarkup(writer);

                environment.pop(Heartbeat.class);

                heartbeat.end();
            }
        };

        MarkupRendererFilter defaultValidationDecorator = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                ValidationDecorator decorator = validationDecoratorFactory.newInstance(writer);

                environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer);

                environment.pop(ValidationDecorator.class);
            }
        };

        configuration.add("DocumentLinker", documentLinker);
        configuration.add("InjectDefaultStylesheet", injectDefaultStylesheet, "after:JavaScriptSupport");
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport);
        configuration.add("Heartbeat", heartbeat);
        configuration.add("ValidationDecorator", defaultValidationDecorator);
    }

    /**
     * Contributes {@link PartialMarkupRendererFilter}s used when rendering a
     * partial Ajax response.
     * <dl>
     * <dt>DocumentLinker
     * <dd>Provides {@link org.apache.tapestry5.internal.services.DocumentLinker}
     * <dt>ClientBehaviorSupport</dt>
     * <dd>Provides {@link ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt>
     * <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>DefaultValidationDecorator</dt>
     * <dt>ValidationDecorator</dt>
     * <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (via {@link ValidationDecoratorFactory#newInstance(org.apache.tapestry5.MarkupWriter)})</dd>
     * </dl>
     */
    public void contributePartialMarkupRenderer(OrderedConfiguration<PartialMarkupRendererFilter> configuration,

                                                final ValidationDecoratorFactory validationDecoratorFactory)
    {
        PartialMarkupRendererFilter documentLinker = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker();

                environment.push(DocumentLinker.class, linker);

                renderer.renderMarkup(writer, reply);

                environment.pop(DocumentLinker.class);

                linker.commit(reply);
            }
        };


        PartialMarkupRendererFilter clientBehaviorSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                JavaScriptSupport javascriptSupport = environment.peekRequired(JavaScriptSupport.class);

                ClientBehaviorSupportImpl support = new ClientBehaviorSupportImpl(javascriptSupport, environment);

                environment.push(ClientBehaviorSupport.class, support);

                renderer.renderMarkup(writer, reply);

                environment.pop(ClientBehaviorSupport.class);

                support.commit();
            }
        };

        PartialMarkupRendererFilter heartbeat = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                Heartbeat heartbeat = new HeartbeatImpl();

                heartbeat.begin();

                environment.push(Heartbeat.class, heartbeat);

                renderer.renderMarkup(writer, reply);

                environment.pop(Heartbeat.class);

                heartbeat.end();
            }
        };

        PartialMarkupRendererFilter defaultValidationDecorator = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                ValidationDecorator decorator = validationDecoratorFactory.newInstance(writer);

                environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer, reply);

                environment.pop(ValidationDecorator.class);
            }
        };

        configuration.add("DocumentLinker", documentLinker);
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:JavaScriptSupport");
        configuration.add("Heartbeat", heartbeat);
        configuration.add("ValidationDecorator", defaultValidationDecorator);
    }

    /**
     * Contributes several strategies:
     * <dl>
     * <dt>session
     * <dd>Values are stored in the {@link Session}
     * <dt>flash
     * <dd>Values are stored in the {@link Session}, until the next request (for the page)
     * <dt>client
     * <dd>Values are encoded into URLs (or hidden form fields)
     * </dl>
     */
    public void contributePersistentFieldManager(MappedConfiguration<String, PersistentFieldStrategy> configuration,

                                                 Request request,

                                                 @InjectService("ClientPersistentFieldStrategy")
                                                 PersistentFieldStrategy clientStrategy)
    {
        configuration.add(PersistenceConstants.SESSION, new SessionPersistentFieldStrategy(request));
        configuration.add(PersistenceConstants.FLASH, new FlashPersistentFieldStrategy(request));
        configuration.add(PersistenceConstants.CLIENT, clientStrategy);
    }

    /**
     * Contributes {@link ValueEncoder}s or {@link ValueEncoderFactory}s for types:
     * <ul>
     * <li>Object
     * <li>String
     * <li>Enum
     * </ul>
     */
    @SuppressWarnings("all")
    public static void contributeValueEncoderSource(MappedConfiguration<Class, Object> configuration)
    {
        configuration.addInstance(Object.class, TypeCoercedValueEncoderFactory.class);
        configuration.add(String.class, new StringValueEncoder());
        configuration.addInstance(Enum.class, EnumValueEncoderFactory.class);
    }

    /**
     * Contributes a single filter, "Secure", which checks for non-secure
     * requests that access secure pages.
     */
    public void contributePageRenderRequestHandler(OrderedConfiguration<PageRenderRequestFilter> configuration,
                                                   final RequestSecurityManager securityManager)
    {
        PageRenderRequestFilter secureFilter = new PageRenderRequestFilter()
        {
            public void handle(PageRenderRequestParameters parameters, PageRenderRequestHandler handler)
                    throws IOException
            {

                if (securityManager.checkForInsecurePageRenderRequest(parameters))
                    return;

                handler.handle(parameters);
            }
        };

        configuration.add("Secure", secureFilter);
    }

    /**
     * Configures the extensions that will require a digest to be downloaded via
     * the asset dispatcher. Most resources
     * are "safe", they don't require a digest. For unsafe resources, the digest
     * is incorporated into the URL to ensure
     * that the client side isn't just "fishing".
     * <p/>
     * The extensions must be all lower case.
     * <p/>
     * This contributes "class", "properties" and "tml" (the template extension).
     *
     * @param configuration
     *         collection of extensions
     */
    public static void contributeResourceDigestGenerator(Configuration<String> configuration)
    {
        // Java class files always require a digest.
        configuration.add("class");

        // Even though properties don't contain sensible data we should protect
        // them.
        configuration.add("properties");

        // Likewise, we don't want people fishing for templates.
        configuration.add(TapestryConstants.TEMPLATE_EXTENSION);
    }

    public static void contributeTemplateParser(MappedConfiguration<String, URL> config)
    {
        // Any class inside the internal module would do. Or we could move all
        // these
        // files to o.a.t.services.

        Class c = TemplateParserImpl.class;

        config.add("-//W3C//DTD XHTML 1.0 Strict//EN", c.getResource("xhtml1-strict.dtd"));
        config.add("-//W3C//DTD XHTML 1.0 Transitional//EN", c.getResource("xhtml1-transitional.dtd"));
        config.add("-//W3C//DTD XHTML 1.0 Frameset//EN", c.getResource("xhtml1-frameset.dtd"));
        config.add("-//W3C//DTD HTML 4.01//EN", c.getResource("xhtml1-strict.dtd"));
        config.add("-//W3C//DTD HTML 4.01 Transitional//EN", c.getResource("xhtml1-transitional.dtd"));
        config.add("-//W3C//DTD HTML 4.01 Frameset//EN", c.getResource("xhtml1-frameset.dtd"));
        config.add("-//W3C//ENTITIES Latin 1 for XHTML//EN", c.getResource("xhtml-lat1.ent"));
        config.add("-//W3C//ENTITIES Symbols for XHTML//EN", c.getResource("xhtml-symbol.ent"));
        config.add("-//W3C//ENTITIES Special for XHTML//EN", c.getResource("xhtml-special.ent"));
    }

    /**
     * Contributes factory defaults that may be overridden.
     */
    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration)
    {
        // Remember this is request-to-request time, presumably it'll take the
        // developer more than
        // one second to make a change, save it, and switch back to the browser.

        configuration.add(SymbolConstants.FILE_CHECK_INTERVAL, "1 s");
        configuration.add(SymbolConstants.FILE_CHECK_UPDATE_TIMEOUT, "50 ms");

        // This should be overridden for particular applications. These are the
        // locales for which we have (at least some) localized messages.
        configuration.add(SymbolConstants.SUPPORTED_LOCALES,
                "en,it,es,zh_CN,pt_PT,de,ru,hr,fi_FI,sv_SE,fr_FR,da,pt_BR,ja,el,bg,no_NB,sr_RS,mk_MK");

        configuration.add(SymbolConstants.TAPESTRY_VERSION,
                VersionUtils.readVersionNumber("META-INF/gradle/org.apache.tapestry/tapestry-core/project.properties"));

        configuration.add(SymbolConstants.COOKIE_MAX_AGE, "7 d");

        configuration.add(SymbolConstants.START_PAGE_NAME, "start");

        configuration.add(SymbolConstants.DEFAULT_STYLESHEET, "classpath:/org/apache/tapestry5/default.css");
        configuration.add("tapestry.spacer-image", "classpath:/org/apache/tapestry5/spacer.gif");

        configuration.add(SymbolConstants.PRODUCTION_MODE, true);

        configuration.add(SymbolConstants.CLUSTERED_SESSIONS, true);

        configuration.add(SymbolConstants.ASSET_PATH_PREFIX, "assets");

        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, true);

        configuration.add(MetaDataConstants.SECURE_PAGE, false);

        configuration.add(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED, true);

        // This is designed to make it easy to keep synchronized with
        // script.aculo.ous. As we support a new version, we create a new folder, and update the
        // path entry. We can then delete the old version folder (or keep it around). This should
        // be more manageable than overwriting the local copy with updates (it's too easy for
        // files deleted between scriptaculous releases to be accidentally left lying around).
        // There's also a ClasspathAliasManager contribution based on the path.

        configuration.add(SymbolConstants.SCRIPTACULOUS, "classpath:${tapestry.scriptaculous.path}");
        configuration.add("tapestry.scriptaculous.path", "org/apache/tapestry5/scriptaculous_1_9_0");

        // Likewise for WebFX DatePicker, currently version 1.0.6

        configuration.add("tapestry.datepicker.path", "org/apache/tapestry5/datepicker_106");
        configuration.add(SymbolConstants.DATEPICKER, "classpath:${tapestry.datepicker.path}");

        configuration.add(SymbolConstants.PERSISTENCE_STRATEGY, PersistenceConstants.SESSION);

        configuration.add(MetaDataConstants.RESPONSE_CONTENT_TYPE, "text/html");

        configuration.add(SymbolConstants.CHARSET, "UTF-8");

        configuration.add(SymbolConstants.APPLICATION_CATALOG,
                String.format("context:WEB-INF/${%s}.properties", InternalSymbols.APP_NAME));

        configuration.add(SymbolConstants.EXCEPTION_REPORT_PAGE, "ExceptionReport");

        configuration.add(SymbolConstants.MIN_GZIP_SIZE, 100);

        Random random = new Random(System.currentTimeMillis());

        configuration.add(SymbolConstants.APPLICATION_VERSION, Long.toHexString(random.nextLong()));

        configuration.add(SymbolConstants.OMIT_GENERATOR_META, false);

        configuration.add(SymbolConstants.SECURE_ENABLED, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.COMPACT_JSON, SymbolConstants.PRODUCTION_MODE_VALUE);

        configuration.add(SymbolConstants.ENCODE_LOCALE_INTO_PATH, true);

        configuration.add(InternalSymbols.PRE_SELECTED_FORM_NAMES, "reset,submit,select,id,method,action,onsubmit," + InternalConstants.CANCEL_NAME);

        configuration.add(SymbolConstants.COMPONENT_RENDER_TRACING_ENABLED, false);

        // The default values denote "use values from request"
        configuration.add(SymbolConstants.HOSTNAME, "");
        configuration.add(SymbolConstants.HOSTPORT, 0);
        configuration.add(SymbolConstants.HOSTPORT_SECURE, 0);

        configuration.add(SymbolConstants.UNKNOWN_COMPONENT_ID_CHECK_ENABLED, true);

        configuration.add(SymbolConstants.APPLICATION_FOLDER, "");

        // Grid component parameters defaults
        configuration.add(ComponentParameterConstants.GRID_ROWS_PER_PAGE, GridConstants.ROWS_PER_PAGE);
        configuration.add(ComponentParameterConstants.GRID_PAGER_POSITION, GridConstants.PAGER_POSITION);
        configuration.add(ComponentParameterConstants.GRID_EMPTY_BLOCK, GridConstants.EMPTY_BLOCK);
        configuration.add(ComponentParameterConstants.GRID_TABLE_CSS_CLASS, GridConstants.TABLE_CLASS);
        configuration.add(ComponentParameterConstants.GRIDPAGER_PAGE_RANGE, GridConstants.PAGER_PAGE_RANGE);
        configuration.add(ComponentParameterConstants.GRIDCOLUMNS_SORTABLE_ASSET, GridConstants.COLUMNS_SORTABLE);
        configuration.add(ComponentParameterConstants.GRIDCOLUMNS_ASCENDING_ASSET, GridConstants.COLUMNS_ASCENDING);
        configuration.add(ComponentParameterConstants.GRIDCOLUMNS_DESCENDING_ASSET, GridConstants.COLUMNS_DESCENDING);

        // FormInjector component parameters defaults
        configuration.add(ComponentParameterConstants.FORMINJECTOR_INSERT_POSITION, "above");
        configuration.add(ComponentParameterConstants.FORMINJECTOR_SHOW_FUNCTION, "highlight");

        // Palette component parameters defaults
        configuration.add(ComponentParameterConstants.PALETTE_ROWS_SIZE, 10);

        // Zone component parameters defaults
        configuration.add(ComponentParameterConstants.ZONE_SHOW_METHOD, "show");
        configuration.add(ComponentParameterConstants.ZONE_UPDATE_METHOD, "highlight");

        // By default, no page is on the whitelist unless it has the @WhitelistAccessOnly annotation
        configuration.add(MetaDataConstants.WHITELIST_ONLY_PAGE, false);

        configuration.add(SymbolConstants.REQUIRE_JS, "classpath:org/apache/tapestry5/require_2.0.2.js");
        configuration.add(SymbolConstants.CONTEXT_PATH, "");

        // Leaving this as the default results in a runtime error logged to the console (and a default password is used);
        // you are expected to override this symbol.
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "");
    }

    /**
     * Adds a listener to the {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSource} that clears the
     * {@link PropertyAccess} and {@link TypeCoercer} caches on
     * a class loader invalidation. In addition, forces the
     * realization of {@link ComponentClassResolver} at startup.
     */
    public void contributeApplicationInitializer(OrderedConfiguration<ApplicationInitializerFilter> configuration,
                                                 final TypeCoercer typeCoercer, final ComponentClassResolver componentClassResolver, @ComponentClasses
    final InvalidationEventHub invalidationEventHub, final @Autobuild
    RestoreDirtySessionObjects restoreDirtySessionObjects)
    {
        final Runnable callback = new Runnable()
        {
            public void run()
            {
                propertyAccess.clearCache();

                typeCoercer.clearCache();
            }
        };

        ApplicationInitializerFilter clearCaches = new ApplicationInitializerFilter()
        {
            public void initializeApplication(Context context, ApplicationInitializer initializer)
            {
                // Snuck in here is the logic to clear the PropertyAccess
                // service's cache whenever
                // the component class loader is invalidated.

                invalidationEventHub.addInvalidationCallback(callback);

                endOfRequestEventHub.addEndOfRequestListener(restoreDirtySessionObjects);

                // Perform other pending initialization

                initializer.initializeApplication(context);

                // We don't care about the result, but this forces a load of the
                // service
                // at application startup, rather than on first request.

                componentClassResolver.isPageName("ForceLoadAtStartup");
            }
        };

        configuration.add("ClearCachesOnInvalidation", clearCaches);
    }

    /**
     * Contributes filters:
     * <dl>
     * <dt>Ajax</dt>
     * <dd>Determines if the request is Ajax oriented, and redirects to an alternative handler if so</dd>
     * <dt>Secure</dt>
     * <dd>Sends a redirect if an non-secure request accesses a secure page</dd>
     * </dl>
     */
    public void contributeComponentEventRequestHandler(OrderedConfiguration<ComponentEventRequestFilter> configuration,
                                                       final RequestSecurityManager requestSecurityManager, @Ajax
    ComponentEventRequestHandler ajaxHandler)
    {
        ComponentEventRequestFilter secureFilter = new ComponentEventRequestFilter()
        {
            public void handle(ComponentEventRequestParameters parameters, ComponentEventRequestHandler handler)
                    throws IOException
            {
                if (requestSecurityManager.checkForInsecureComponentEventRequest(parameters))
                    return;

                handler.handle(parameters);
            }
        };
        configuration.add("Secure", secureFilter);

        configuration.add("Ajax", new AjaxFilter(request, ajaxHandler));
    }

    /**
     * Contributes:
     * <dl>
     * <dt>AjaxFormUpdate</dt>
     * <dd>{@link AjaxFormUpdateFilter}</dd>
     * </dl>
     *
     * @since 5.2.0
     */
    public static void contributeAjaxComponentEventRequestHandler(
            OrderedConfiguration<ComponentEventRequestFilter> configuration)
    {
        configuration.addInstance("AjaxFormUpdate", AjaxFormUpdateFilter.class);
    }

    /**
     * Contributes strategies accessible via the {@link NullFieldStrategySource} service.
     * <p/>
     * <dl>
     * <dt>default</dt>
     * <dd>Does nothing, nulls stay null.</dd>
     * <dt>zero</dt>
     * <dd>Null values are converted to zero.</dd>
     * </dl>
     */
    public static void contributeNullFieldStrategySource(MappedConfiguration<String, NullFieldStrategy> configuration)
    {
        configuration.add("default", new DefaultNullFieldStrategy());
        configuration.add("zero", new ZeroNullFieldStrategy());
    }

    /**
     * Determines positioning of hidden fields relative to other elements (this
     * is needed by {@link org.apache.tapestry5.corelib.components.FormFragment} and others.
     * <p/>
     * For elements input, select, textarea and label the hidden field is positioned after.
     * <p/>
     * For elements p, div, li and td, the hidden field is positioned inside.
     */
    public static void contributeHiddenFieldLocationRules(
            MappedConfiguration<String, RelativeElementPosition> configuration)
    {
        configuration.add("input", RelativeElementPosition.AFTER);
        configuration.add("select", RelativeElementPosition.AFTER);
        configuration.add("textarea", RelativeElementPosition.AFTER);
        configuration.add("label", RelativeElementPosition.AFTER);

        configuration.add("p", RelativeElementPosition.INSIDE);
        configuration.add("div", RelativeElementPosition.INSIDE);
        configuration.add("td", RelativeElementPosition.INSIDE);
        configuration.add("li", RelativeElementPosition.INSIDE);
    }

    /**
     * @since 5.1.0.0
     */
    public static LinkCreationHub buildLinkCreationHub(LinkSource source)
    {
        return source.getLinkCreationHub();
    }

    /**
     * Exposes the public portion of the internal {@link InternalComponentInvalidationEventHub} service.
     *
     * @since 5.1.0.0
     */
    @Marker(ComponentClasses.class)
    public static InvalidationEventHub buildComponentClassesInvalidationEventHub(
            InternalComponentInvalidationEventHub trueHub)
    {
        return trueHub;
    }

    /**
     * @since 5.1.0.0
     */
    @Marker(ComponentTemplates.class)
    public static InvalidationEventHub buildComponentTemplatesInvalidationEventHub(
            ComponentTemplateSource templateSource)
    {
        return templateSource.getInvalidationEventHub();
    }

    /**
     * @since 5.1.0.0
     */
    @Marker(ComponentMessages.class)
    public static InvalidationEventHub buildComponentMessagesInvalidationEventHub(ComponentMessagesSource messagesSource)
    {
        return messagesSource.getInvalidationEventHub();
    }

    @Scope(ScopeConstants.PERTHREAD)
    public Environment buildEnvironment(PerthreadManager perthreadManager)
    {
        EnvironmentImpl service = new EnvironmentImpl();

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    /**
     * The master SessionPersistedObjectAnalyzer.
     *
     * @since 5.1.0.0
     */
    @Marker(Primary.class)
    public SessionPersistedObjectAnalyzer buildSessionPersistedObjectAnalyzer(
            Map<Class, SessionPersistedObjectAnalyzer> configuration)
    {
        return strategyBuilder.build(SessionPersistedObjectAnalyzer.class, configuration);
    }

    /**
     * Identifies String, Number and Boolean as immutable objects, a catch-all
     * handler for Object (that understands
     * the {@link org.apache.tapestry5.annotations.ImmutableSessionPersistedObject} annotation),
     * and a handler for {@link org.apache.tapestry5.OptimizedSessionPersistedObject}.
     *
     * @since 5.1.0.0
     */
    public static void contributeSessionPersistedObjectAnalyzer(
            MappedConfiguration<Class, SessionPersistedObjectAnalyzer> configuration)
    {
        configuration.add(Object.class, new DefaultSessionPersistedObjectAnalyzer());

        SessionPersistedObjectAnalyzer<Object> immutable = new SessionPersistedObjectAnalyzer<Object>()
        {
            public boolean checkAndResetDirtyState(Object sessionPersistedObject)
            {
                return false;
            }
        };

        configuration.add(String.class, immutable);
        configuration.add(Number.class, immutable);
        configuration.add(Boolean.class, immutable);

        configuration.add(OptimizedSessionPersistedObject.class, new OptimizedSessionPersistedObjectAnalyzer());
    }

    /**
     * @since 5.1.1.0
     */
    @Marker(Primary.class)
    public StackTraceElementAnalyzer buildMasterStackTraceElementAnalyzer(List<StackTraceElementAnalyzer> configuration)
    {
        return chainBuilder.build(StackTraceElementAnalyzer.class, configuration);
    }

    /**
     * Contributes:
     * <dl>
     * <dt>Application</dt>
     * <dd>Checks for classes in the application package</dd>
     * <dt>Proxies</dt>
     * <dd>Checks for classes that appear to be generated proxies.</dd>
     * <dt>SunReflect</dt>
     * <dd>Checks for <code>sun.reflect</code> (which are omitted)
     * <dt>TapestryAOP</dt>
     * <dd>Omits stack frames for classes related to Tapestry AOP (such as advice, etc.)</dd>
     * <dt>OperationTracker</dt>
     * <dd>Omits stack frames related to {@link OperationTracker}</dd>
     * <dt>Access</dt>
     * <dd>Omits tack frames used to provide with access to container class private members</dd>
     * </dl>
     *
     * @since 5.1.0.0
     */
    public static void contributeMasterStackTraceElementAnalyzer(
            OrderedConfiguration<StackTraceElementAnalyzer> configuration)
    {
        configuration.addInstance("TapestryAOP", TapestryAOPStackFrameAnalyzer.class);
        configuration.add("Proxies", new ProxiesStackTraceElementAnalyzer());
        configuration.add("Synthetic", new SyntheticStackTraceElementAnalyzer());
        configuration.add("SunReflect", new PrefixCheckStackTraceElementAnalyzer(
                StackTraceElementClassConstants.OMITTED, "sun.reflect."));
        configuration.add("OperationTracker", new RegexpStackTraceElementAnalyzer(Pattern.compile("internal\\.(RegistryImpl|PerThreadOperationTracker|OperationTrackerImpl).*(run|invoke)\\("), StackTraceElementClassConstants.OMITTED));
        configuration.add("Access", new RegexpStackTraceElementAnalyzer(Pattern.compile("\\.access\\$\\d+\\("), StackTraceElementClassConstants.OMITTED));

        configuration.addInstance("Application", ApplicationStackTraceElementAnalyzer.class);

    }


    /**
     * Advises the {@link org.apache.tapestry5.services.messages.ComponentMessagesSource} service so
     * that the creation
     * of {@link org.apache.tapestry5.ioc.Messages} instances can be deferred.
     *
     * @since 5.1.0.0
     */
    @Match("ComponentMessagesSource")
    public static void adviseLazy(LazyAdvisor advisor, MethodAdviceReceiver receiver)
    {
        advisor.addLazyMethodInvocationAdvice(receiver);
    }

    /**
     * @since 5.1.0.0
     */
    public ComponentRequestHandler buildComponentRequestHandler(List<ComponentRequestFilter> configuration,

                                                                @Autobuild
                                                                ComponentRequestHandlerTerminator terminator,

                                                                Logger logger)
    {
        return pipelineBuilder.build(logger, ComponentRequestHandler.class, ComponentRequestFilter.class,
                configuration, terminator);
    }

    /**
     * Contributes:
     * <dl>
     * <dt>InitializeActivePageName
     * <dd>{@link InitializeActivePageName}
     * </dl>
     *
     * @since 5.2.0
     */
    public void contributeComponentRequestHandler(OrderedConfiguration<ComponentRequestFilter> configuration)
    {
        configuration.addInstance("InitializeActivePageName", InitializeActivePageName.class);
    }

    /**
     * Decorate FieldValidatorDefaultSource to setup the EnvironmentMessages
     * object and place it in the environment.
     * Although this could have been implemented directly in the default
     * implementation of the service, doing it
     * as service decoration ensures that the environment will be properly setup
     * even if a user overrides the default
     * service implementation.
     *
     * @param defaultSource
     *         The service to decorate
     * @param environment
     */
    public static FieldValidatorDefaultSource decorateFieldValidatorDefaultSource(
            final FieldValidatorDefaultSource defaultSource, final Environment environment)
    {
        return new FieldValidatorDefaultSource()
        {

            public FieldValidator createDefaultValidator(Field field, String overrideId, Messages overrideMessages,
                                                         Locale locale, Class propertyType, AnnotationProvider propertyAnnotations)
            {
                environment.push(EnvironmentMessages.class, new EnvironmentMessages(overrideMessages, overrideId));
                FieldValidator fieldValidator = defaultSource.createDefaultValidator(field, overrideId,
                        overrideMessages, locale, propertyType, propertyAnnotations);
                environment.pop(EnvironmentMessages.class);
                return fieldValidator;
            }

            public FieldValidator createDefaultValidator(ComponentResources resources, String parameterName)
            {

                EnvironmentMessages em = new EnvironmentMessages(resources.getContainerMessages(), resources.getId());
                environment.push(EnvironmentMessages.class, em);
                FieldValidator fieldValidator = defaultSource.createDefaultValidator(resources, parameterName);
                environment.pop(EnvironmentMessages.class);
                return fieldValidator;
            }
        };
    }

    /**
     * Exposes the Environmental {@link Heartbeat} as an injectable service.
     *
     * @since 5.2.0
     */
    public Heartbeat buildHeartbeat()
    {
        return environmentalBuilder.build(Heartbeat.class);
    }

    public static ComponentMessagesSource buildComponentMessagesSource(UpdateListenerHub updateListenerHub, @Autobuild
    ComponentMessagesSourceImpl service)
    {
        updateListenerHub.addUpdateListener(service);

        return service;
    }

    /**
     * Contributes:
     * <dl>
     * <dt>AppCatalog</dt>
     * <dd>The Resource defined by {@link SymbolConstants#APPLICATION_CATALOG}</dd>
     * <dt>Core</dt>
     * <dd>Built in messages used by Tapestry's default validators and components (before:AppCatalog)</dd>
     * <dt>
     *
     * @since 5.2.0
     */
    public static void contributeComponentMessagesSource(AssetSource assetSource,
                                                         @Symbol(SymbolConstants.APPLICATION_CATALOG)
                                                         Resource applicationCatalog, OrderedConfiguration<Resource> configuration)
    {
        configuration.add("Core",
                assetSource.resourceForPath("org/apache/tapestry5/core.properties"),
                "before:AppCatalog");
        configuration.add("AppCatalog", applicationCatalog);
    }

    /**
     * Contributes extractors for {@link Meta}, {@link Secure}, {@link ContentType} and {@link WhitelistAccessOnly} annotations.
     *
     * @since 5.2.0
     */
    @SuppressWarnings("unchecked")
    public static void contributeMetaWorker(MappedConfiguration<Class, MetaDataExtractor> configuration)
    {
        configuration.addInstance(Meta.class, MetaAnnotationExtractor.class);
        configuration.add(Secure.class, new FixedExtractor(MetaDataConstants.SECURE_PAGE));
        configuration.addInstance(ContentType.class, ContentTypeExtractor.class);
        configuration.add(WhitelistAccessOnly.class, new FixedExtractor(MetaDataConstants.WHITELIST_ONLY_PAGE));
    }

    /**
     * Builds the {@link ComponentTemplateLocator} as a chain of command.
     *
     * @since 5.2.0
     */
    @Marker(Primary.class)
    public ComponentTemplateLocator buildComponentTemplateLocator(List<ComponentTemplateLocator> configuration)
    {
        return chainBuilder.build(ComponentTemplateLocator.class, configuration);
    }

    /**
     * Contributes two template locators:
     * <dl>
     * <dt>Default</dt>
     * <dd>Searches for the template on the classpath ({@link DefaultTemplateLocator}</dd>
     * <dt>Page</dt>
     * <dd>Searches for <em>page</em> templates in the context ({@link PageTemplateLocator})</dd>
     * </dl>
     *
     * @since 5.2.0
     */
    public static void contributeComponentTemplateLocator(OrderedConfiguration<ComponentTemplateLocator> configuration,
                                                          @ContextProvider
                                                          AssetFactory contextAssetFactory,
                                                          @Symbol(SymbolConstants.APPLICATION_FOLDER) String applicationFolder,
                                                          ComponentClassResolver componentClassResolver)
    {
        configuration.add("Default", new DefaultTemplateLocator());
        configuration
                .add("Page", new PageTemplateLocator(contextAssetFactory.getRootResource(), componentClassResolver, applicationFolder));

    }

    /**
     * Builds {@link ComponentEventLinkTransformer} service as a chain of command.
     *
     * @since 5.2.0
     */
    @Marker(Primary.class)
    public ComponentEventLinkTransformer buildComponentEventLinkTransformer(
            List<ComponentEventLinkTransformer> configuration)
    {
        return chainBuilder.build(ComponentEventLinkTransformer.class, configuration);
    }

    /**
     * Builds {@link PageRenderLinkTransformer} service as a chain of command.
     *
     * @since 5.2.0
     */
    @Marker(Primary.class)
    public PageRenderLinkTransformer buildPageRenderLinkTransformer(List<PageRenderLinkTransformer> configuration)
    {
        return chainBuilder.build(PageRenderLinkTransformer.class, configuration);
    }

    /**
     * Provides the "LinkTransformer" interceptor for the {@link ComponentEventLinkEncoder} service.
     * Other decorations
     * should come after LinkTransformer.
     *
     * @since 5.2.0
     */
    @Match("ComponentEventLinkEncoder")
    public ComponentEventLinkEncoder decorateLinkTransformer(LinkTransformer linkTransformer,
                                                             ComponentEventLinkEncoder delegate)
    {
        return new LinkTransformerInterceptor(linkTransformer, delegate);
    }

    /**
     * In production mode, override {@link UpdateListenerHub} to be an empty placeholder.
     */
    @Contribute(ServiceOverride.class)
    public static void productionModeOverrides(MappedConfiguration<Class, Object> configuration,
                                               @Symbol(SymbolConstants.PRODUCTION_MODE)
                                               boolean productionMode)
    {
        if (productionMode)
        {
            configuration.add(UpdateListenerHub.class, new UpdateListenerHub()
            {
                public void fireCheckForUpdates()
                {
                }

                public void addUpdateListener(UpdateListener listener)
                {

                }
            });
        }
    }

    /**
     * Contributes a single default analyzer:
     * <dl>
     * <dt>LocalhostOnly</dt>
     * <dd>Identifies requests from localhost as on client whitelist</dd>
     * </dl>
     *
     * @since 5.3
     */
    @Contribute(ClientWhitelist.class)
    public static void defaultWhitelist(OrderedConfiguration<WhitelistAnalyzer> configuration)
    {
        configuration.add("LocalhostOnly", new LocalhostOnly());
    }

    @Startup
    public static void registerToClearPlasticProxyFactoryOnInvalidation(@ComponentClasses InvalidationEventHub hub, @Builtin final PlasticProxyFactory proxyFactory)
    {
        hub.addInvalidationCallback(new Runnable()
        {
            public void run()
            {
                proxyFactory.clearCache();
            }
        });
    }
}
