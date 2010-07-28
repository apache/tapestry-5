// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.*;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.LoopFormState;
import org.apache.tapestry5.corelib.SubmitMode;
import org.apache.tapestry5.corelib.data.BlankOption;
import org.apache.tapestry5.corelib.data.GridPagerPosition;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.internal.AssetConstants;
import org.apache.tapestry5.internal.DefaultNullFieldStrategy;
import org.apache.tapestry5.internal.DefaultValidationDecorator;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.internal.PropertyOverridesImpl;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.ZeroNullFieldStrategy;
import org.apache.tapestry5.internal.beaneditor.EnvironmentMessages;
import org.apache.tapestry5.internal.beaneditor.MessagesConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.PrimitiveFieldConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.ValidateAnnotationConstraintGenerator;
import org.apache.tapestry5.internal.bindings.*;
import org.apache.tapestry5.internal.grid.CollectionGridDataSource;
import org.apache.tapestry5.internal.grid.NullDataSource;
import org.apache.tapestry5.internal.gzip.GZipFilter;
import org.apache.tapestry5.internal.renderers.AvailableValuesRenderer;
import org.apache.tapestry5.internal.renderers.ComponentResourcesRenderer;
import org.apache.tapestry5.internal.renderers.EventContextRenderer;
import org.apache.tapestry5.internal.renderers.ListRenderer;
import org.apache.tapestry5.internal.renderers.LocationRenderer;
import org.apache.tapestry5.internal.renderers.ObjectArrayRenderer;
import org.apache.tapestry5.internal.renderers.RequestRenderer;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.services.ajax.AjaxFormUpdateFilter;
import org.apache.tapestry5.internal.services.ajax.JavaScriptSupportImpl;
import org.apache.tapestry5.internal.services.assets.AssetPathConstructorImpl;
import org.apache.tapestry5.internal.services.assets.ClasspathAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.ContextAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.StackAssetRequestHandler;
import org.apache.tapestry5.internal.services.javascript.CoreJSStack;
import org.apache.tapestry5.internal.services.javascript.DateFieldStack;
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor;
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackSourceImpl;
import org.apache.tapestry5.internal.services.linktransform.LinkTransformerImpl;
import org.apache.tapestry5.internal.services.linktransform.LinkTransformerInterceptor;
import org.apache.tapestry5.internal.services.messages.PropertiesFileParserImpl;
import org.apache.tapestry5.internal.services.meta.ContentTypeExtractor;
import org.apache.tapestry5.internal.services.meta.MetaAnnotationExtractor;
import org.apache.tapestry5.internal.services.meta.MetaWorkerImpl;
import org.apache.tapestry5.internal.services.templates.DefaultTemplateLocator;
import org.apache.tapestry5.internal.services.templates.PageTemplateLocator;
import org.apache.tapestry5.internal.transform.*;
import org.apache.tapestry5.internal.translator.NumericTranslator;
import org.apache.tapestry5.internal.translator.NumericTranslatorSupport;
import org.apache.tapestry5.internal.translator.StringTranslator;
import org.apache.tapestry5.internal.util.PrimaryKeyEncoder2ValueEncoder;
import org.apache.tapestry5.internal.util.RenderableAsBlock;
import org.apache.tapestry5.internal.util.StringRenderable;
import org.apache.tapestry5.internal.validator.ValidatorMacroImpl;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.ajax.MultiZoneUpdateEventResultProcessor;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.linktransform.ComponentEventLinkTransformer;
import org.apache.tapestry5.services.linktransform.LinkTransformer;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.apache.tapestry5.services.meta.FixedExtractor;
import org.apache.tapestry5.services.meta.MetaDataExtractor;
import org.apache.tapestry5.services.meta.MetaWorker;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.apache.tapestry5.util.StringToEnumCoercion;
import org.apache.tapestry5.validator.Email;
import org.apache.tapestry5.validator.Max;
import org.apache.tapestry5.validator.MaxLength;
import org.apache.tapestry5.validator.Min;
import org.apache.tapestry5.validator.MinLength;
import org.apache.tapestry5.validator.None;
import org.apache.tapestry5.validator.Regexp;
import org.apache.tapestry5.validator.Required;
import org.apache.tapestry5.validator.ValidatorMacro;
import org.slf4j.Logger;

/**
 * The root module for Tapestry.
 */
@SuppressWarnings(
{ "JavaDoc" })
@Marker(Core.class)
@SubModule(InternalModule.class)
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
     * noticable way), it's about eliminating the
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
    // are teminators for pipeline services.

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
        private final SessionPersistedObjectAnalyzer analyzer;

        public HttpServletRequestHandlerTerminator(RequestHandler handler, String applicationCharset,
                SessionPersistedObjectAnalyzer analyzer)
        {
            this.handler = handler;
            this.applicationCharset = applicationCharset;
            this.analyzer = analyzer;
        }

        public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
                throws IOException
        {
            requestGlobals.storeServletRequestResponse(servletRequest, servletResponse);

            Request request = new RequestImpl(servletRequest, applicationCharset, analyzer);
            Response response = new ResponseImpl(servletResponse);

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
        binder.bind(ObjectRenderer.class, LocationRenderer.class).withId("LocationRenderer");
        binder.bind(ObjectProvider.class, AssetObjectProvider.class).withId("AssetObjectProvider");
        binder.bind(RequestExceptionHandler.class, DefaultRequestExceptionHandler.class);
        binder.bind(ComponentEventResultProcessor.class, ComponentInstanceResultProcessor.class).withId(
                "ComponentInstanceResultProcessor");
        binder.bind(NullFieldStrategySource.class, NullFieldStrategySourceImpl.class);
        binder.bind(HttpServletRequestFilter.class, IgnoredPathsFilter.class).withId("IgnoredPathsFilter");
        binder.bind(ContextValueEncoder.class, ContextValueEncoderImpl.class);
        binder.bind(BaseURLSource.class, BaseURLSourceImpl.class);
        binder.bind(BeanBlockOverrideSource.class, BeanBlockOverrideSourceImpl.class);
        binder.bind(AliasManager.class, AliasManagerImpl.class).withId("AliasOverrides");
        binder.bind(HiddenFieldLocationRules.class, HiddenFieldLocationRulesImpl.class);
        binder.bind(PageDocumentGenerator.class, PageDocumentGeneratorImpl.class);
        binder.bind(ResponseRenderer.class, ResponseRendererImpl.class);
        binder.bind(FieldTranslatorSource.class, FieldTranslatorSourceImpl.class);
        binder.bind(BindingFactory.class, MessageBindingFactory.class).withId("MessageBindingFactory");
        binder.bind(BindingFactory.class, ValidateBindingFactory.class).withId("ValidateBindingFactory");
        binder.bind(BindingFactory.class, TranslateBindingFactory.class).withId("TranslateBindingFactory");
        binder.bind(BindingFactory.class, AssetBindingFactory.class).withId("AssetBindingFactory");
        binder.bind(BindingFactory.class, ContextBindingFactory.class).withId("ContextBindingFactory");
        binder.bind(BindingFactory.class, NullFieldStrategyBindingFactory.class).withId(
                "NullFieldStrategyBindingFactory");
        binder.bind(BindingFactory.class, SymbolBindingFactory.class).withId("SymbolBindingFactory");
        binder.bind(URLEncoder.class, URLEncoderImpl.class);
        binder.bind(ContextPathEncoder.class, ContextPathEncoderImpl.class);
        binder.bind(ApplicationStatePersistenceStrategy.class, SessionApplicationStatePersistenceStrategy.class)
                .withId("SessionApplicationStatePersistenceStrategy");
        binder.bind(AssetPathConverter.class, IdentityAssetPathConverter.class);
        binder.bind(NumericTranslatorSupport.class);
        binder.bind(ClientDataEncoder.class, ClientDataEncoderImpl.class);
        binder.bind(ComponentEventLinkEncoder.class, ComponentEventLinkEncoderImpl.class);
        binder.bind(PageRenderLinkSource.class, PageRenderLinkSourceImpl.class);
        binder.bind(ClientInfrastructure.class, ClientInfrastructureImpl.class);
        binder.bind(URLRewriter.class, URLRewriterImpl.class);
        binder.bind(ValidatorMacro.class, ValidatorMacroImpl.class);
        binder.bind(PropertiesFileParser.class, PropertiesFileParserImpl.class);
        binder.bind(PageActivator.class, PageActivatorImpl.class);
        binder.bind(Dispatcher.class, AssetDispatcher.class).withId("AssetDispatcher");
        binder.bind(AssetPathConstructor.class, AssetPathConstructorImpl.class);
        binder.bind(JavaScriptStackSource.class, JavaScriptStackSourceImpl.class);
        binder.bind(TranslatorAlternatesSource.class, TranslatorAlternatesSourceImpl.class);
        binder.bind(MetaWorker.class, MetaWorkerImpl.class);
        binder.bind(LinkTransformer.class, LinkTransformerImpl.class);
    }

    // ========================================================================
    //
    // Service Builder Methods (static)
    //
    // ========================================================================

    @PreventServiceDecoration
    public static Alias buildAlias(Logger logger,

    @Symbol(InternalSymbols.ALIAS_MODE)
    String mode,

    @InjectService("AliasOverrides")
    AliasManager overridesManager,

    Collection<AliasContribution> configuration)
    {
        AliasManager manager = new AliasManagerImpl(logger, configuration);

        return new AliasImpl(manager, mode, overridesManager);
    }

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

    public static void contributeClasspathAssetAliasManager(MappedConfiguration<String, String> configuration,

    @Symbol(SymbolConstants.APPLICATION_VERSION)
    String applicationVersion,

    @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
    String appPackage,

    ComponentClassResolver resolver)
    {
        configuration.add("tapestry", "org/apache/tapestry5");

        configuration.add("app", toPackagePath(appPackage));

        // Each library gets a mapping or its folder automatically

        Map<String, String> folderToPackageMapping = resolver.getFolderToPackageMapping();

        for (String folder : folderToPackageMapping.keySet())
        {
            configuration.add(folder, toPackagePath(folderToPackageMapping.get(folder)));
        }

    }

    /**
     * Contributes an handler for each mapped classpath alias, as well handlers for context assets
     * and stack assets (combined {@link JavaScriptStack} files).
     */
    public static void contributeAssetDispatcher(MappedConfiguration<String, AssetRequestHandler> configuration,

    @ContextProvider
    AssetFactory contextAssetFactory,

    @Autobuild
    StackAssetRequestHandler stackAssetRequestHandler,

    ResourceCache resourceCache,

    ClasspathAssetAliasManager classpathAssetAliasManager, ResourceStreamer streamer,
            AssetResourceLocator assetResourceLocator)
    {
        resourceCache.addInvalidationListener(stackAssetRequestHandler);

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

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("core", "org.apache.tapestry5.corelib"));
    }

    /**
     * Adds a number of standard component class transform workers:
     * <dl>
     * <dt>Retain</dt>
     * <dd>Allows fields to retain their values between requests</dd>
     * <dt>Persist</dt>
     * <dd>Allows fields to store their their value persistently between requests</dd>
     * <dt>Parameter</dt>
     * <dd>Identifies parameters based on the {@link org.apache.tapestry5.annotations.Parameter} annotation</dd>
     * <dt>Component</dt>
     * <dd>Defines embedded components based on the {@link org.apache.tapestry5.annotations.Component} annotation</dd>
     * <dt>Mixin</dt>
     * <dd>Adds a mixin as part of a component's implementation</dd>
     * <dt>Environment</dt>
     * <dd>Allows fields to contain values extracted from the {@link org.apache.tapestry5.services.Environment} service</dd>
     * <dt>Inject</dt>
     * <dd>Used with the {@link org.apache.tapestry5.ioc.annotations.Inject} annotation, when a value is supplied</dd>
     * <dt>InjectService</dt>
     * <dd>Handles the {@link org.apache.tapestry5.ioc.annotations.InjectService} annotation</dd>
     * <dt>InjectPage</dt>
     * <dd>Adds code to allow access to other pages via the {@link org.apache.tapestry5.annotations.InjectPage} field
     * annotation</dd>
     * <dt>InjectBlock</dt>
     * <dd>Allows a block from the template to be injected into a field</dd>
     * <dt>IncludeStylesheet</dt>
     * <dd>Supports the {@link org.apache.tapestry5.annotations.IncludeStylesheet} annotation</dd>
     * <dt>IncludeJavaScriptLibrary</dt>
     * <dd>Supports the {@link org.apache.tapestry5.annotations.IncludeJavaScriptLibrary} annotation</dd>
     * <dt>Import</dt>
     * <dd>Supports the {@link Import} annotation</dd>
     * <dt>SupportsInformalParameters</dt>
     * <dd>Checks for the annotation</dd>
     * <dt>Meta</dt>
     * <dd>Checks for meta data annotations and adds it to the component model</dd>
     * <dt>ApplicationState</dt>
     * <dd>Converts fields that reference application state objects
     * <dt>UnclaimedField</dt>
     * <dd>Identifies unclaimed fields and resets them to null/0/false at the end of the request</dd>
     * <dt>RenderCommand</dt>
     * <dd>Ensures all components also implement {@link org.apache.tapestry5.runtime.RenderCommand}</dd>
     * <dt>RenderPhase</dt>
     * <dd>Link in render phaes methods</dd>
     * <dt>InvokePostRenderCleanupOnResources</dt>
     * <dd>Makes sure {@link org.apache.tapestry5.internal.InternalComponentResources#postRenderCleanup()} is invoked
     * after a component finishes rendering</dd>
     * <dt>GenerateAccessors</dt>
     * <dd>Generates accessor methods if {@link org.apache.tapestry5.annotations.Property} annotation is present</dd>
     * <dt>Cached</dt>
     * <dd>Checks for the {@link org.apache.tapestry5.annotations.Cached} annotation</dd>
     * <dt>Log</dt>
     * <dd>Checks for the {@link org.apache.tapestry5.annotations.Log} annotation</dd>
     * <dt>PageReset
     * <dd>Checks for the {@link PageReset} annotation
     * <dt>HeartbeatDeferred
     * <dd>Support for the {@link HeartbeatDeferred} annotation
     * <dt>ActivationRequestParameter
     * <dd>Support for the {@link ActivationRequestParameter} annotation
     * </dl>
     */
    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration,

            MetaWorker metaWorker,

            ComponentClassResolver resolver)
    {
        configuration.addInstance("Cached", CachedWorker.class);

        configuration.add("Meta", metaWorker);

        configuration.addInstance("Inject", InjectWorker.class);
        configuration.addInstance("InjectService", InjectServiceWorker.class);

        configuration.add("MixinAfter", new MixinAfterWorker());
        configuration.add("Component", new ComponentWorker(resolver));
        configuration.add("Mixin", new MixinWorker(resolver));
        configuration.addInstance("ActivationRequestParameter", ActivationRequestParameterWorker.class,
                "before:OnEvent");
        configuration.addInstance("OnEvent", OnEventWorker.class);
        configuration.add("SupportsInformalParameters", new SupportsInformalParametersWorker());
        configuration.addInstance("InjectPage", InjectPageWorker.class);
        configuration.addInstance("InjectContainer", InjectContainerWorker.class);
        configuration.addInstance("InjectComponent", InjectComponentWorker.class);
        configuration.add("RenderCommand", new RenderCommandWorker());

        // Default values for parameters are often some form of injection, so
        // make sure
        // that Parameter fields are processed after injections.

        configuration.addInstance("Parameter", ParameterWorker.class, "after:Inject*");

        // bind parameter should always go after parameter to make sure all
        // parameters
        // have been properly setup.

        configuration.addInstance("BindParameter", BindParameterWorker.class, "after:Parameter");

        configuration.addInstance("RenderPhase", RenderPhaseMethodWorker.class);

        // Ideally, these should be ordered pretty late in the process to make
        // sure there are no
        // side effects with other workers that do work inside the page
        // lifecycle methods.

        add(configuration, PageLoaded.class, TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, "pageLoaded");
        add(configuration, PageAttached.class, TransformConstants.CONTAINING_PAGE_DID_ATTACH_SIGNATURE, "pageAttached");
        add(configuration, PageDetached.class, TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE, "pageDetached");

        configuration.add("Retain", new RetainWorker());
        configuration.addInstance("Persist", PersistWorker.class);

        configuration.addInstance("DiscardAfter", DiscardAfterWorker.class);

        configuration.addInstance("IncludeStylesheet", IncludeStylesheetWorker.class, "after:SetupRender");
        configuration
                .addInstance("IncludeJavaScriptLibrary", IncludeJavaScriptLibraryWorker.class, "after:SetupRender");
        configuration.addInstance("Import", ImportWorker.class, "after:SetupRender");

        configuration.add("InvokePostRenderCleanupOnResources", new InvokePostRenderCleanupOnResourcesWorker());

        configuration.add("Property", new PropertyWorker());

        // These must come after Property, since they actually delete fields
        // that may still have the annotation
        configuration.addInstance("ApplicationState", ApplicationStateWorker.class, "after:Property");
        configuration.addInstance("Environment", EnvironmentalWorker.class, "after:Property");

        configuration.addInstance("Log", LogWorker.class);

        configuration.addInstance("PageReset", PageResetAnnotationWorker.class);

        // This one is always last. Any additional private fields that aren't
        // annotated will
        // be converted to clear out at the end of the request.

        configuration.addInstance("UnclaimedField", UnclaimedFieldWorker.class, "after:*");

        configuration.add("PageActivationContext", new PageActivationContextWorker(), "before:OnEvent");

        configuration.addInstance("SessionAttribute", SessionAttributeWorker.class);

        configuration.addInstance("HeartbeatDeferred", HeartbeatDeferredWorker.class, "after:RenderPhase");
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

        configuration.add(String.class, "text");
        configuration.add(Number.class, "number");
        configuration.add(Enum.class, "enum");
        configuration.add(Boolean.class, "boolean");
        configuration.add(Date.class, "date");
        configuration.add(Calendar.class, "calendar");
    }

    public static void contributeBeanBlockSource(Configuration<BeanBlockContribution> configuration)
    {
        addEditBlock(configuration, "text");
        addEditBlock(configuration, "number");
        addEditBlock(configuration, "enum");
        addEditBlock(configuration, "boolean");
        addEditBlock(configuration, "date");
        addEditBlock(configuration, "password");
        addEditBlock(configuration, "calendar");

        // longtext uses a text area, not a text field

        addEditBlock(configuration, "longtext");

        addDisplayBlock(configuration, "enum");
        addDisplayBlock(configuration, "date");
        addDisplayBlock(configuration, "calendar");

        // Password and long text have special output needs.
        addDisplayBlock(configuration, "password");
        addDisplayBlock(configuration, "longtext");
    }

    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType)
    {
        addEditBlock(configuration, dataType, dataType);
    }

    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType, String blockId)
    {
        configuration.add(new BeanBlockContribution(dataType, "PropertyEditBlocks", blockId, true));
    }

    private static void addDisplayBlock(Configuration<BeanBlockContribution> configuration, String dataType)
    {
        addDisplayBlock(configuration, dataType, dataType);
    }

    private static void addDisplayBlock(Configuration<BeanBlockContribution> configuration, String dataType,
            String blockId)
    {
        configuration.add(new BeanBlockContribution(dataType, "PropertyDisplayBlocks", blockId, false));
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
     * Contributes the base set of injection providers:
     * <dl>
     * <dt>Default</dt>
     * <dd>based on {@link MasterObjectProvider}</dd>
     * <dt>Block</dt>
     * <dd>injects fields of type Block</dd>
     * <dt>ComponentResources</dt>
     * <dd>give component access to its resources</dd>
     * <dt>CommonResources</dt>
     * <dd>access to properties of resources (log, messages, etc.)</dd>
     * <dt>Asset</dt>
     * <dd>injection of assets (triggered via {@link Path} annotation), with the path relative to the component class</dd>
     * <dt>Service</dt>
     * <dd>ordered last, for use when Inject is present and nothing else works, matches field type against Tapestry IoC
     * services</dd>
     * </dl>
     */
    public static void contributeInjectionProvider(OrderedConfiguration<InjectionProvider> configuration,

    MasterObjectProvider masterObjectProvider,

    ObjectLocator locator,

    SymbolSource symbolSource,

    AssetSource assetSource)
    {
        configuration.add("Default", new DefaultInjectionProvider(masterObjectProvider, locator));

        configuration.add("ComponentResources", new ComponentResourcesInjectionProvider());

        // This comes after default, to deal with conflicts between injecting a
        // String as the
        // component id, and injecting a string with @Symbol or @Value.

        configuration.add("CommonResources", new CommonResourcesInjectionProvider(), "after:Default");

        configuration.add("Asset", new AssetInjectionProvider(symbolSource, assetSource), "before:Default");

        configuration.add("Block", new BlockInjectionProvider(), "before:Default");

        // This needs to be the last one, since it matches against services
        // and might blow up if there is no match.
        configuration.add("Service", new ServiceInjectionProvider(locator), "after:*");
    }

    /**
     * Contributes two object providers:
     * <dl>
     * <dt>Alias</dt>
     * <dd>Searches by type among {@linkplain AliasContribution
     * contributions} to the {@link Alias} service</dd>
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

    @Local
    final Alias alias,

    @InjectService("AssetObjectProvider")
    ObjectProvider assetObjectProvider,

    ObjectLocator locator)
    {
        // There's a nasty web of dependencies related to Alias; this wrapper
        // class lets us defer instantiating the Alias service implementation just long enough
        // to defuse those dependencies. The @Local annotation prevents a recursive call through
        // the MasterObjectProvider to resolve the Alias service itself; that is
        // MasterObjectProvider gets built using this proxy, then the proxy will trigger the
        // construction of AliasImpl (which itself needs MasterObjectProvider to resolve some
        // dependencies).

        ObjectProvider wrapper = new ObjectProvider()
        {
            public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
            {
                return alias.getObjectProvider().provide(objectType, annotationProvider, locator);
            }
        };

        configuration.add("Alias", wrapper, "after:ServiceOverride");

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

        configuration.add("GZIP", gzipCompressionEnabled ? gzipFilter : null, "after:IgnoredPaths");

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
     * {@link org.apache.tapestry5.internal.services.CheckForUpdatesFilter}).
     * <dt>ErrorFilter</dt>
     * <dd>Catches request errors and lets the {@link org.apache.tapestry5.services.RequestExceptionHandler} handle them
     * </dd>
     * <dt>StoreIntoGlobals</dt>
     * <dd>Stores the request and response into the {@link org.apache.tapestry5.services.RequestGlobals} service (this
     * is repeated at the end of the pipeline, in case any filter substitutes the request or response).
     * </dl>
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration, Context context,

    @Symbol(SymbolConstants.FILE_CHECK_INTERVAL)
    @IntermediateType(TimeInterval.class)
    long checkInterval,

    @Symbol(SymbolConstants.FILE_CHECK_UPDATE_TIMEOUT)
    @IntermediateType(TimeInterval.class)
    long updateTimeout,

    UpdateListenerHub updateListenerHub,

    URLRewriter urlRewriter)
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
                }
                finally
                {
                    endOfRequestEventHub.fire();
                }
            }
        };

        configuration.add("CheckForUpdates",
                new CheckForUpdatesFilter(updateListenerHub, checkInterval, updateTimeout), "before:*");

        // we just need the URLRewriterRequestFilter if we have URL rewriter
        // rules, of course.
        if (urlRewriter.hasRequestRules())
        {

            URLRewriterRequestFilter urlRewriterRequestFilter = new URLRewriterRequestFilter(urlRewriter);
            configuration.add("URLRewriter", urlRewriterRequestFilter, "before:StaticFiles");

        }

        configuration.add("StaticFiles", staticFilesFilter);

        configuration.addInstance("ErrorFilter", RequestErrorFilter.class);

        configuration.add("StoreIntoGlobals", storeIntoGlobals, "after:StaticFiles", "before:ErrorFilter");

        configuration.add("EndOfRequest", fireEndOfRequestEvent, "after:StoreIntoGlobals", "before:ErrorFilter");

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
        { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class,
                BigDecimal.class };

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
     * <li>String to {@link InsertPosition}
     * <li>Map to {@link oSelectModel}
     * <li>Collection to {@link GridDataSource}
     * <li>null to {@link GridDataSource}
     * <li>String to {@link GridPagerPosition}
     * <li>List to {@link SelectModel}
     * <li>{@link .ComponentResourcesAware} (typically, a component) to {@link ComponentResources}
     * <li>String to {@link BlankOption}
     * <li> {@link ComponentResources} to {@link PropertyOverrides}
     * <li>String to {@link Renderable}
     * <li>{@link Renderable} to {@link Block}
     * <li>String to {@link DateFormat}
     * <li>{@link PrimaryKeyEncoder} to {@link ValueEncoder}
     * <li>String to {@link Resource} (via {@link AssetSource#resourceForPath(String)})
     * <li>{@link Renderable} to {@link RenderCommand}</li>
     * <li>String to {@link LoopFormState}</li>
     * <li>String to {@link SubmitMode}</li>
     * <li>String to {@link Pattern}</li>
     * <li>String to {@link DateFormat}</li>
     * </ul>
     */
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration,

    @Builtin
    TypeCoercer coercer,

    @Builtin
    final ThreadLocale threadLocale,

    @Core
    final AssetSource assetSource)
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

        add(configuration, GridPagerPosition.class);
        add(configuration, InsertPosition.class);
        add(configuration, BlankOption.class);
        add(configuration, LoopFormState.class);
        add(configuration, SubmitMode.class);

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

        configuration.add(CoercionTuple.create(PrimaryKeyEncoder.class, ValueEncoder.class,
                new PrimaryKeyEncoder2ValueEncoder(coercer)));

        configuration.add(CoercionTuple.create(Date.class, Calendar.class, new Coercion<Date, Calendar>()
        {
            public Calendar coerce(Date input)
            {
                Calendar calendar = Calendar.getInstance(threadLocale.getLocale());
                calendar.setTime(input);
                return calendar;
            }
        }));
    }

    private static <T extends Enum> void add(Configuration<CoercionTuple> configuration, Class<T> enumType)
    {
        configuration.add(CoercionTuple.create(String.class, enumType, StringToEnumCoercion.create(enumType)));
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

    private static void add(OrderedConfiguration<ComponentClassTransformWorker> configuration,
            Class<? extends Annotation> annotationClass, TransformMethodSignature lifecycleMethodSignature,
            String methodAlias)
    {
        ComponentClassTransformWorker worker = new PageLifecycleAnnotationWorker(annotationClass,
                lifecycleMethodSignature, methodAlias);

        String name = TapestryInternalUtils.lastTerm(annotationClass.getName());

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

    @Marker(ClasspathProvider.class)
    public AssetFactory buildClasspathAssetFactory(ResourceCache resourceCache,
            ClasspathAssetAliasManager aliasManager, AssetPathConverter converter)
    {
        ClasspathAssetFactory factory = new ClasspathAssetFactory(resourceCache, aliasManager, converter);

        resourceCache.addInvalidationListener(factory);

        return factory;
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
     *            contributions of special factories for some constants, each
     *            contributed factory may return a
     *            binding if applicable, or null otherwise
     */
    public BindingFactory buildPropBindingFactory(List<BindingFactory> configuration, @Autobuild
    PropBindingFactory service)
    {
        configuration.add(service);

        return chainBuilder.build(BindingFactory.class, configuration);
    }

    /**
     * Builds the source of {@link Messages} containing validation messages. The
     * contributions are paths to message
     * bundles (resource paths within the classpath); the default contribution
     * is "org/apache/tapestry5/internal/ValidationMessages".
     */
    public ValidationMessagesSource buildValidationMessagesSource(List<String> configuration,

    UpdateListenerHub updateListenerHub,

    @ClasspathProvider
    AssetFactory classpathAssetFactory,

    PropertiesFileParser parser,

    ComponentMessagesSource componentMessagesSource,

    ClasspathURLConverter classpathURLConverter)
    {
        ValidationMessagesSourceImpl service = new ValidationMessagesSourceImpl(configuration,
                classpathAssetFactory.getRootResource(), parser, componentMessagesSource, classpathURLConverter);
        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public static MetaDataLocator buildMetaDataLocator(@Autobuild
    MetaDataLocatorImpl service, @ComponentClasses
    InvalidationEventHub hub)
    {
        hub.addInvalidationListener(service);

        return service;
    }

    public PersistentFieldStrategy buildClientPersistentFieldStrategy(LinkCreationHub linkCreationHub, @Autobuild
    ClientPersistentFieldStrategy service)
    {
        linkCreationHub.addListener(service);

        return service;
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry5.RenderSupport} inside this thread's
     * {@link org.apache.tapestry5.services.Environment}.
     */
    public RenderSupport buildRenderSupport()
    {
        return environmentalBuilder.build(RenderSupport.class);
    }

    /**
     * Builds a proxy to the current {@link JavaScriptSupport} inside this thread's {@link Environment}.
     * 
     * @since 5.2.0
     */
    public JavaScriptSupport buildJavaScriptSupport()
    {
        return environmentalBuilder.build(JavaScriptSupport.class);
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
    public ComponentClassTransformWorker buildComponentClassTransformWorker(
            List<ComponentClassTransformWorker> configuration)
    {
        return chainBuilder.build(ComponentClassTransformWorker.class, configuration);
    }

    /**
     * Analyzes properties to determine the data types, used to
     * {@linkplain #contributeBeanBlockSource(org.apache.tapestry5.ioc.Configuration)} locale
     * display and edit blocks} for properties. The default behaviors
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
     */

    public InjectionProvider buildInjectionProvider(List<InjectionProvider> configuration)
    {
        return chainBuilder.build(InjectionProvider.class, configuration);
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

    @Primary
    SessionPersistedObjectAnalyzer analyzer)
    {
        HttpServletRequestHandler terminator = new HttpServletRequestHandlerTerminator(handler, applicationCharset,
                analyzer);

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
    { Primary.class, Traditional.class })
    public ComponentEventResultProcessor buildComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration)
    {
        return constructComponentEventResultProcessor(configuration);
    }

    /**
     * The component event result processor used for Ajax-oriented component
     * requests.
     */
    @Marker(Ajax.class)
    public ComponentEventResultProcessor buildAjaxComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration)
    {
        return constructComponentEventResultProcessor(configuration);
    }

    private ComponentEventResultProcessor constructComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration)
    {
        Set<Class> handledTypes = CollectionFactory.newSet(configuration.keySet());

        // A slight hack!

        configuration.put(Object.class, new ObjectComponentEventResultProcessor(handledTypes));

        StrategyRegistry<ComponentEventResultProcessor> registry = StrategyRegistry.newInstance(
                ComponentEventResultProcessor.class, configuration);

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
        hub.addInvalidationListener(service);

        return service;
    }

    public static TranslatorSource buildTranslatorSource(Map<Class, Translator> configuration,
            TranslatorAlternatesSource alternatesSource, @ComponentClasses
            InvalidationEventHub hub)
    {
        TranslatorSourceImpl service = new TranslatorSourceImpl(configuration,
                alternatesSource.getTranslatorAlternates());

        hub.addInvalidationListener(service);

        return service;
    }

    @Marker(Primary.class)
    public ObjectRenderer buildObjectRenderer(Map<Class, ObjectRenderer> configuration)
    {
        return strategyBuilder.build(ObjectRenderer.class, configuration);
    }

    /**
     * Returns a {@link org.apache.tapestry5.ioc.services.ClassFactory} that can
     * be used to create extra classes around
     * component classes. This ClassFactory will be cleared whenever an
     * underlying component class is discovered to have
     * changed. Use of this class factory implies that your code will become
     * aware of this (if necessary) to discard any
     * cached object (alas, this currently involves dipping into the internals
     * side to register for the correct
     * notifications). Failure to properly clean up can result in really nasty
     * PermGen space memory leaks.
     */
    @Marker(ComponentLayer.class)
    public ClassFactory buildComponentClassFactory(ComponentInstantiatorSource source)
    {
        return shadowBuilder.build(source, "classFactory", ClassFactory.class);
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

    public PropertyConduitSource buildPropertyConduitSource(@Autobuild
    PropertyConduitSourceImpl service, @ComponentClasses
    InvalidationEventHub hub)
    {
        hub.addInvalidationListener(service);

        return service;
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
     * 
     * @see #contributePartialMarkupRenderer(org.apache.tapestry5.ioc.OrderedConfiguration, org.apache.tapestry5.Asset,
     *      org.apache.tapestry5.ioc.services.SymbolSource, AssetSource)
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
    { Traditional.class, Primary.class })
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
    { Ajax.class, Primary.class })
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

        configuration.add(HttpError.class, new ComponentEventResultProcessor<HttpError>()
        {
            public void processResultValue(HttpError value) throws IOException
            {
                response.sendError(value.getStatusCode(), value.getMessage());
            }
        });

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
     */
    public static void contributeAjaxComponentEventResultProcessor(
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

    @InjectService("AssetDispatcher")
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

        configuration.add(ClassTransformation.class, preformatted);

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
     * <dt>JavascriptSupport</dt>
     * <dd>Provides {@link JavaScriptSupport}</dd>
     * <dt>RenderSupport</dt>
     * <dd>Provides {@link org.apache.tapestry5.RenderSupport}</dd>
     * <dt>InjectDefaultStyleheet</dt>
     * <dd>Injects the default stylesheet</dd></dt>
     * <dt>ClientBehaviorSupport</dt>
     * <dd>Provides {@link ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt>
     * <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>DefaultValidationDecorator</dt>
     * <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (as an instance of
     * {@link org.apache.tapestry5.internal.DefaultValidationDecorator})</dd>
     * </dl>
     */
    public void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration,

    @Path("${tapestry.spacer-image}")
    final Asset spacerImage,

    @Symbol(SymbolConstants.OMIT_GENERATOR_META)
    final boolean omitGeneratorMeta,

    @Symbol(SymbolConstants.TAPESTRY_VERSION)
    final String tapestryVersion,

    @Symbol(SymbolConstants.COMPACT_JSON)
    final boolean compactJSON,

    final SymbolSource symbolSource,

    final AssetSource assetSource,

    final JavaScriptStackSource javascriptStackSource,

    final JavaScriptStackPathConstructor javascriptStackPathConstructor,

    @Path("${tapestry.default-stylesheet}")
    final Asset defaultStylesheet)
    {
        MarkupRendererFilter documentLinker = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinkerImpl linker = new DocumentLinkerImpl(omitGeneratorMeta, tapestryVersion, compactJSON);

                environment.push(DocumentLinker.class, linker);

                renderer.renderMarkup(writer);

                environment.pop(DocumentLinker.class);

                linker.updateDocument(writer.getDocument());
            }
        };

        MarkupRendererFilter javaScriptSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                JavaScriptSupportImpl support = new JavaScriptSupportImpl(linker, javascriptStackSource,
                        javascriptStackPathConstructor);

                environment.push(JavaScriptSupport.class, support);

                renderer.renderMarkup(writer);

                environment.pop(JavaScriptSupport.class);

                support.commit();
            }
        };

        MarkupRendererFilter renderSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                JavaScriptSupport javascriptSupport = environment.peekRequired(JavaScriptSupport.class);

                RenderSupportImpl support = new RenderSupportImpl(symbolSource, assetSource, javascriptSupport);

                environment.push(RenderSupport.class, support);

                renderer.renderMarkup(writer);

                environment.pop(RenderSupport.class);

                support.commit();
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
                ValidationDecorator decorator = new DefaultValidationDecorator(environment, spacerImage, writer);

                environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer);

                environment.pop(ValidationDecorator.class);
            }
        };

        configuration.add("DocumentLinker", documentLinker);
        configuration.add("JavaScriptSupport", javaScriptSupport, "after:DocumentLinker");
        configuration.add("RenderSupport", renderSupport, "after:JavaScriptSupport");
        configuration.add("InjectDefaultStyleheet", injectDefaultStylesheet, "after:RenderSupport");
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:JavaScriptSupport");
        configuration.add("Heartbeat", heartbeat, "after:RenderSupport");
        configuration.add("DefaultValidationDecorator", defaultValidationDecorator, "after:Heartbeat");
    }

    /**
     * Contributes {@link PartialMarkupRendererFilter}s used when rendering a
     * partial Ajax response.
     * <dl>
     * <dt>DocumentLinker
     * <dd>Provides {@link org.apache.tapestry5.internal.services.DocumentLinker}
     * <dt>JavaScriptSupport
     * <dd>Provides {@link JavaScriptSupport}</dd>
     * <dt>PageRenderSupport</dt>
     * <dd>Provides {@link org.apache.tapestry5.RenderSupport}</dd>
     * <dt>ClientBehaviorSupport</dt>
     * <dd>Provides {@link ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt>
     * <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>DefaultValidationDecorator</dt>
     * <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (as an instance of
     * {@link org.apache.tapestry5.internal.DefaultValidationDecorator})</dd>
     * </dl>
     */
    public void contributePartialMarkupRenderer(OrderedConfiguration<PartialMarkupRendererFilter> configuration,

    @Path("${tapestry.spacer-image}")
    final Asset spacerImage,

    final JavaScriptStackSource javascriptStackSource,

    final JavaScriptStackPathConstructor javascriptStackPathConstructor,

    final SymbolSource symbolSource,

    final AssetSource assetSource)
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

        PartialMarkupRendererFilter javascriptSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                String uid = Long.toHexString(System.currentTimeMillis());

                String namespace = "_" + uid;

                IdAllocator idAllocator = new IdAllocator(namespace);

                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                JavaScriptSupportImpl support = new JavaScriptSupportImpl(linker, javascriptStackSource,
                        javascriptStackPathConstructor, idAllocator, true);

                environment.push(JavaScriptSupport.class, support);

                renderer.renderMarkup(writer, reply);

                environment.pop(JavaScriptSupport.class);

                support.commit();
            }
        };

        PartialMarkupRendererFilter renderSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                JavaScriptSupport javascriptSupport = environment.peekRequired(JavaScriptSupport.class);

                RenderSupportImpl support = new RenderSupportImpl(symbolSource, assetSource, javascriptSupport);

                environment.push(RenderSupport.class, support);

                renderer.renderMarkup(writer, reply);

                support.commit();

                environment.pop(RenderSupport.class);
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
                ValidationDecorator decorator = new DefaultValidationDecorator(environment, spacerImage, writer);

                environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer, reply);

                environment.pop(ValidationDecorator.class);
            }
        };

        configuration.add("DocumentLinker", documentLinker);
        configuration.add("JavaScriptSupport", javascriptSupport, "after:DocumentLinker");
        configuration.add("RenderSupport", renderSupport, "after:JavaScriptSupport");
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:JavaScriptSupport");
        configuration.add("Heartbeat", heartbeat, "after:RenderSupport");
        configuration.add("DefaultValidationDecorator", defaultValidationDecorator, "after:Heartbeat");
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
     * Contributes org/apache/tapestry5/internal/ValidationMessages as
     * "Default", ordered first.
     */
    public void contributeValidationMessagesSource(OrderedConfiguration<String> configuration)
    {
        configuration.add("Default", "org/apache/tapestry5/internal/ValidationMessages", "before:*");
    }

    public static ValueEncoderSource buildValueEncoderSource(Map<Class, ValueEncoderFactory> configuration,
            @ComponentClasses
            InvalidationEventHub hub)
    {
        ValueEncoderSourceImpl service = new ValueEncoderSourceImpl(configuration);

        hub.addInvalidationListener(service);

        return service;
    }

    /**
     * Contributes {@link ValueEncoderFactory}s for types:
     * <ul>
     * <li>Object
     * <li>String
     * <li>Enum
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public static void contributeValueEncoderSource(MappedConfiguration<Class, ValueEncoderFactory> configuration)
    {
        configuration.addInstance(Object.class, TypeCoercedValueEncoderFactory.class);
        configuration.add(String.class, GenericValueEncoderFactory.create(new StringValueEncoder()));
        configuration.add(Enum.class, new EnumValueEncoderFactory());
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
     *            collection of extensions
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
    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        // Remember this is request-to-request time, presumably it'll take the
        // developer more than
        // one second to make a change, save it, and switch back to the browser.

        configuration.add(SymbolConstants.FILE_CHECK_INTERVAL, "1 s");
        configuration.add(SymbolConstants.FILE_CHECK_UPDATE_TIMEOUT, "50 ms");

        // This should be overridden for particular applications. These are the
        // locales for which we have (at least some) localized messages.
        configuration.add(SymbolConstants.SUPPORTED_LOCALES,
                "en,it,es,zh_CN,pt_PT,de,ru,hr,fi_FI,sv_SE,fr_FR,da,pt_BR,ja,el,bg,no_NB");

        configuration.add(SymbolConstants.TAPESTRY_VERSION,
                VersionUtils.readVersionNumber("META-INF/maven/org.apache.tapestry/tapestry-core/pom.properties"));

        configuration.add(SymbolConstants.COOKIE_MAX_AGE, "7 d");

        configuration.add(SymbolConstants.START_PAGE_NAME, "start");

        configuration.add(SymbolConstants.DEFAULT_JAVASCRIPT, "classpath:/org/apache/tapestry5/tapestry.js");

        configuration.add(SymbolConstants.DEFAULT_STYLESHEET, "classpath:/org/apache/tapestry5/default.css");
        configuration.add("tapestry.spacer-image", "classpath:/org/apache/tapestry5/spacer.gif");

        configuration.add(SymbolConstants.PAGE_POOL_SOFT_LIMIT, "5");
        configuration.add(SymbolConstants.PAGE_POOL_SOFT_WAIT, "10 ms");
        configuration.add(SymbolConstants.PAGE_POOL_HARD_LIMIT, "20");
        configuration.add(SymbolConstants.PAGE_POOL_ACTIVE_WINDOW, "10 m");
        configuration.add(SymbolConstants.PAGE_POOL_ENABLED, "false");

        configuration.add(SymbolConstants.SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS, "false");

        configuration.add(SymbolConstants.FORCE_ABSOLUTE_URIS, "false");

        configuration.add(SymbolConstants.PRODUCTION_MODE, "true");

        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, "true");

        configuration.add(MetaDataConstants.SECURE_PAGE, "false");

        configuration.add(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED, "true");

        // This is designed to make it easy to keep synchronized with
        // script.aculo.ous. As we
        // support a new version, we create a new folder, and update the path
        // entry. We can then
        // delete the old version folder (or keep it around). This should be
        // more manageable than
        // overwriting the local copy with updates (it's too easy for files
        // deleted between scriptaculous
        // releases to be accidentally left lying around). There's also a
        // ClasspathAliasManager
        // contribution based on the path.

        configuration.add(SymbolConstants.SCRIPTACULOUS, "classpath:${tapestry.scriptaculous.path}");
        configuration.add("tapestry.scriptaculous.path", "org/apache/tapestry5/scriptaculous_1_8_2");

        // Likewise for WebFX DatePicker, currently version 1.0.6

        configuration.add("tapestry.datepicker.path", "org/apache/tapestry5/datepicker_106");
        configuration.add(SymbolConstants.DATEPICKER, "classpath:${tapestry.datepicker.path}");

        configuration.add("tapestry.blackbird.path", "org/apache/tapestry5/blackbird_1_0");
        configuration.add(SymbolConstants.BLACKBIRD, "classpath:${tapestry.blackbird.path}");

        configuration.add(SymbolConstants.PERSISTENCE_STRATEGY, PersistenceConstants.SESSION);

        configuration.add(MetaDataConstants.RESPONSE_CONTENT_TYPE, "text/html");

        configuration.add(SymbolConstants.CHARSET, "UTF-8");

        configuration.add(SymbolConstants.APPLICATION_CATALOG,
                String.format("context:WEB-INF/${%s}.properties", InternalSymbols.APP_NAME));

        configuration.add(SymbolConstants.EXCEPTION_REPORT_PAGE, "ExceptionReport");

        configuration.add(SymbolConstants.MIN_GZIP_SIZE, "100");

        Random random = new Random(System.currentTimeMillis());

        configuration.add(SymbolConstants.APPLICATION_VERSION, Long.toHexString(random.nextLong()));

        configuration.add(SymbolConstants.OMIT_GENERATOR_META, "false");
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, "true");

        configuration.add(SymbolConstants.SECURE_ENABLED, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.COMPACT_JSON, SymbolConstants.PRODUCTION_MODE_VALUE);

        configuration.add(SymbolConstants.ENCODE_LOCALE_INTO_PATH, "true");

        configuration.add(SymbolConstants.BLACKBIRD_ENABLED, "false");

        configuration.add(InternalSymbols.PRE_SELECTED_FORM_NAMES, "reset,submit,select,id,method,action,onsubmit");
    }

    /**
     * Adds content types:
     * <dl>
     * <dt>css</dt>
     * <dd>text/css</dd>
     * <dt>js</dt>
     * <dd>text/javascript</dd>
     * <dt>jpg, jpeg</dt>
     * <dd>image/jpeg</dd>
     * <dt>gif</dt>
     * <dd>image/gif</dd>
     * <dt>png</dt>
     * <dd>image/png</dd>
     * <dt>swf</dt>
     * <dd>application/x-shockwave-flash</dd>
     * </dl>
     */
    public void contributeResourceStreamer(MappedConfiguration<String, String> configuration)
    {
        configuration.add("css", "text/css");
        configuration.add("js", "text/javascript");
        configuration.add("gif", "image/gif");
        configuration.add("jpg", "image/jpeg");
        configuration.add("jpeg", "image/jpeg");
        configuration.add("png", "image/png");
        configuration.add("swf", "application/x-shockwave-flash");
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
        final InvalidationListener listener = new InvalidationListener()
        {
            public void objectWasInvalidated()
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

                invalidationEventHub.addInvalidationListener(listener);

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
     * <dt>ImmediateRender</dt>
     * <dd>When {@linkplain SymbolConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS
     * immediate action response rendering} is enabled, generates the markup response (instead of a page redirect
     * response, which is the normal behavior)</dd>
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

        configuration.add("Ajax", new AjaxFilter(request, ajaxHandler));

        configuration.addInstance("ImmediateRender", ImmediateActionRenderResponseFilter.class);

        configuration.add("Secure", secureFilter, "before:Ajax");
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
     * @since 5.1.0.0
     */
    @Marker(ComponentClasses.class)
    public static InvalidationEventHub buildComponentClassesInvalidationEventHub(ComponentInstantiatorSource source)
    {
        return source.getInvalidationEventHub();
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
     * The master SessionPesistedObjectAnalyzer.
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
     * and handlers for {@link org.apache.tapestry5.OptimizedSessionPersistedObject} and
     * {@link org.apache.tapestry5.OptimizedApplicationStateObject}.
     * 
     * @since 5.1.0.0
     */
    public static void contributeSessionPersistedObjectAnalyzer(
            MappedConfiguration<Class, SessionPersistedObjectAnalyzer> configuration)
    {
        configuration.add(Object.class, new DefaultSessionPersistedObjectAnalyzer());

        SessionPersistedObjectAnalyzer<Object> immutable = new SessionPersistedObjectAnalyzer<Object>()
        {
            public boolean isDirty(Object object)
            {
                return false;
            }
        };

        configuration.add(String.class, immutable);
        configuration.add(Number.class, immutable);
        configuration.add(Boolean.class, immutable);

        configuration.add(OptimizedSessionPersistedObject.class, new OptimizedSessionPersistedObjectAnalyzer());
        configuration.add(OptimizedApplicationStateObject.class, new OptimizedApplicationStateObjectAnalyzer());
    }

    /**
     * Contributions are content types that do not benefit from compression. Adds
     * the following content types:
     * <ul>
     * <li>image/jpeg</li>
     * <li>image/gif</li>
     * <li>image/png</li>
     * <li>application/json (see https://issues.apache.org/jira/browse/TAP5-469)</li>
     * <li>application/x-shockwave-flash</li>
     * </ul>
     * 
     * @since 5.1.0.0
     */
    public static void contributeResponseCompressionAnalyzer(Configuration<String> configuration)
    {
        configuration.add("image/jpeg");
        configuration.add("image/gif");
        configuration.add("image/png");
        configuration.add("application/json");
        configuration.add("application/x-shockwave-flash");
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
     * </dl>
     * 
     * @since 5.1.0.0
     */
    public static void contributeMasterStackTraceElementAnalyzer(
            OrderedConfiguration<StackTraceElementAnalyzer> configuration)
    {
        configuration.addInstance("Application", ApplicationStackTraceElementAnalyzer.class);
        configuration.add("Proxies", new ProxiesStackTraceElementAnalyzer(), "before:Application");
        configuration.add("Synthetic", new SyntheticStackTraceElementAnalyzer(), "before:Application");
        configuration.add("SunReflect", new PrefixCheckStackTraceElementAnalyzer(
                StackTraceElementClassConstants.OMITTED, "sun.reflect."));
        configuration.addInstance("TapestryAOP", TapestryAOPStackFrameAnalyzer.class, "before:Application");
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
     * @throws Exception
     * @since 5.1.0.2
     */
    public static ComponentEventLinkEncoder decorateComponentEventLinkEncoder(ComponentEventLinkEncoder encoder,
            URLRewriter urlRewriter, Request request, Response response)
    {
        // no rules, no link rewriting.
        if (!urlRewriter.hasLinkRules())
            return null;

        return new URLRewriterLinkEncoderInterceptor(urlRewriter, request, encoder);
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
     *            The serivce to decorate
     * @param environment
     * @return
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

    /**
     * Contributes the "core" and "core-datefield" {@link JavaScriptStack}s
     * 
     * @since 5.2.0
     */
    public static void contributeJavaScriptStackSource(MappedConfiguration<String, JavaScriptStack> configuration)
    {
        configuration.addInstance(InternalConstants.CORE_STACK_NAME, CoreJSStack.class);
        configuration.addInstance("core-datefield", DateFieldStack.class);
    }

    public static ComponentMessagesSource buildComponentMessagesSource(UpdateListenerHub updateListenerHub, @Autobuild
    ComponentMessagesSourceImpl service)
    {
        updateListenerHub.addUpdateListener(service);

        return service;
    }

    /**
     * Contributes "AppCatalog" as the Resource defined by {@link SymbolConstants#APPLICATION_CATALOG}.
     * 
     * @since 5.2.0
     */
    public static void contributeComponentMessagesSource(@Symbol(SymbolConstants.APPLICATION_CATALOG)
    Resource applicationCatalog, OrderedConfiguration<Resource> configuration)
    {
        configuration.add("AppCatalog", applicationCatalog);
    }

    /**
     * Contributes extractors for {@link Meta}, {@link Secure} and {@link ContentType} annotations.
     * 
     * @since 5.2.0
     */
    @SuppressWarnings("unchecked")
    public static void contributeMetaWorker(MappedConfiguration<Class, MetaDataExtractor> configuration)
    {
        configuration.addInstance(Meta.class, MetaAnnotationExtractor.class);
        configuration.add(Secure.class, new FixedExtractor(MetaDataConstants.SECURE_PAGE));
        configuration.addInstance(ContentType.class, ContentTypeExtractor.class);
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
     * <dt>Page (after:Default)</dt>
     * <dd>Searches for <em>page</em> templates in the context ({@link PageTemplateLocator})</dd>
     * </dl>
     * 
     * @since 5.2.0
     */
    public static void contributeComponentTemplateLocator(OrderedConfiguration<ComponentTemplateLocator> configuration,
            @ContextProvider
            AssetFactory contextAssetFactory, ComponentClassResolver componentClassResolver)
    {
        configuration.add("Default", new DefaultTemplateLocator());
        configuration
                .add("Page", new PageTemplateLocator(contextAssetFactory.getRootResource(), componentClassResolver),
                        "after:Default");
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
     * Provides the "LinkTransformer" interceptor for the {@link ComponentEventLinkEncoder} service. Other decorations
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
}
