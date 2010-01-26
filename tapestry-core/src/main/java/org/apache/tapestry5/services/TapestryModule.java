// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.data.BlankOption;
import org.apache.tapestry5.corelib.data.GridPagerPosition;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.internal.*;
import org.apache.tapestry5.internal.beaneditor.PrimitiveFieldConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.ValidateAnnotationConstraintGenerator;
import org.apache.tapestry5.internal.bindings.*;
import org.apache.tapestry5.internal.events.InvalidationListener;
import org.apache.tapestry5.internal.grid.CollectionGridDataSource;
import org.apache.tapestry5.internal.grid.NullDataSource;
import org.apache.tapestry5.internal.renderers.*;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.transform.*;
import org.apache.tapestry5.internal.translator.*;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.internal.util.RenderableAsBlock;
import org.apache.tapestry5.internal.util.StringRenderable;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.util.StringToEnumCoercion;
import org.apache.tapestry5.validator.*;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The root module for Tapestry.
 */
@SuppressWarnings({"JavaDoc"})
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

    private final ComponentInstantiatorSource componentInstantiatorSource;

    private final ChainBuilder chainBuilder;

    private final Request request;

    private final Response response;

    private final ThreadLocale threadLocale;

    private final RequestGlobals requestGlobals;

    private final EnvironmentalShadowBuilder environmentalBuilder;


    /**
     * We inject all sorts of common dependencies (including builders) into the module itself (note: even though some of
     * these service are defined by the module itself, that's ok because services are always lazy proxies).  This isn't
     * about efficiency (it may be slightly more efficient, but not in any noticable way), it's about eliminating the
     * need to keep injecting these dependencies into invividual service builder and contribution methods.
     */
    public TapestryModule(PipelineBuilder pipelineBuilder,

                          PropertyShadowBuilder shadowBuilder,

                          RequestGlobals requestGlobals,

                          ApplicationGlobals applicationGlobals,

                          ChainBuilder chainBuilder,

                          Environment environment,

                          StrategyBuilder strategyBuilder,

                          ComponentInstantiatorSource componentInstantiatorSource,

                          PropertyAccess propertyAccess,

                          Request request,

                          Response response,

                          ThreadLocale threadLocale,

                          EnvironmentalShadowBuilder environmentalBuilder)
    {
        this.pipelineBuilder = pipelineBuilder;
        this.shadowBuilder = shadowBuilder;
        this.requestGlobals = requestGlobals;
        this.applicationGlobals = applicationGlobals;
        this.chainBuilder = chainBuilder;
        this.environment = environment;
        this.strategyBuilder = strategyBuilder;
        this.componentInstantiatorSource = componentInstantiatorSource;
        this.propertyAccess = propertyAccess;
        this.request = request;
        this.response = response;
        this.threadLocale = threadLocale;
        this.environmentalBuilder = environmentalBuilder;
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
        binder.bind(BindingFactory.class, NullFieldStrategyBindingFactory.class).withId(
                "NullFieldStrategyBindingFactory");
        binder.bind(URLEncoder.class, URLEncoderImpl.class);
        binder.bind(ContextPathEncoder.class, ContextPathEncoderImpl.class);
        binder.bind(Dispatcher.class, AssetProtectionDispatcher.class).withId("AssetProtectionDispatcher");
        binder.bind(AssetPathAuthorizer.class, WhitelistAuthorizer.class).withId("WhitelistAuthorizer");
        binder.bind(AssetPathAuthorizer.class, RegexAuthorizer.class).withId("RegexAuthorizer");
    }

    // ========================================================================
    //
    // Service Builder Methods (static)
    //
    // ========================================================================


    public static Alias buildAlias(Logger logger,

                                   @Inject @Symbol(InternalConstants.TAPESTRY_ALIAS_MODE_SYMBOL)
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
     * Contributes the factory for serveral built-in binding prefixes ("asset", "block", "component", "literal", prop",
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
                                               BindingFactory nullFieldStrategyBindingFactory)
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
    }

    public static void contributeClasspathAssetAliasManager(MappedConfiguration<String, String> configuration,

                                                            @Symbol(SymbolConstants.TAPESTRY_VERSION)
                                                            String version,

                                                            // @Inject not needed, because this isn't a service builder method
                                                            @Symbol("tapestry.scriptaculous.path")
                                                            String scriptaculousPath,

                                                            @Symbol("tapestry.datepicker.path")
                                                            String datepickerPath)
    {
        // TAPESTRY-2159:  All the classpath assets are inside a version numbered folder (i.e., 5.0.12).
        // For scriptaculous, etc., this version is not the version of the library, but the version
        // bundled with Tapestry.

        configuration.add(version + "/tapestry", "org/apache/tapestry5");

        configuration.add(version + "/scriptaculous", scriptaculousPath);

        configuration.add(version + "/datepicker", datepickerPath);
    }

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("core", "org.apache.tapestry5.corelib"));
    }

    /**
     * Adds a number of standard component class transform workers: <dl> <dt>Retain </dt> <dd>Allows fields to retain
     * their values between requests</dd> <dt>Persist </dt> <dd>Allows fields to store their their value persistently
     * between requests</dd> <dt>Parameter </dt> <dd>Identifies parameters based on the {@link
     * org.apache.tapestry5.annotations.Parameter} annotation</dd> <dt>Component </dt> <dd>Defines embedded components
     * based on the {@link org.apache.tapestry5.annotations.Component} annotation</dd> <dt>Mixin </dt> <dd>Adds a mixin
     * as part of a component's implementation</dd> <dt>Environment </dt> <dd>Allows fields to contain values extracted
     * from the {@link org.apache.tapestry5.services.Environment} service</dd> <dt>Inject </dt> <dd>Used with the {@link
     * org.apache.tapestry5.ioc.annotations.Inject} annotation, when a value is supplied</dd> <dt>InjectPage</dt>
     * <dd>Adds code to allow access to other pages via the {@link org.apache.tapestry5.annotations.InjectPage} field
     * annotation</dd> <dt>InjectBlock </dt> <dd>Allows a block from the template to be injected into a field</dd>
     * <dt>IncludeStylesheet </dt> <dd>Supports the {@link org.apache.tapestry5.annotations.IncludeStylesheet}
     * annotation</dd> <dt>IncludeJavaScriptLibrary </dt> <dd>Supports the {@link org.apache.tapestry5.annotations.IncludeJavaScriptLibrary}
     * annotation</dd> <dt>SupportsInformalParameters </dt> <dd>Checks for the annotation</dd> <dt>Meta </dt> <dd>Checks
     * for meta data and adds it to the component model</dd> <dt>ApplicationState </dt> <dd>Converts fields that
     * reference application state objects <dt>UnclaimedField </dt> <dd>Identifies unclaimed fields and resets them to
     * null/0/false at the end of the request</dd> <dt>RenderCommand </dt> <dd>Ensures all components also implement
     * {@link org.apache.tapestry5.runtime.RenderCommand}</dd> <dt>SetupRender, BeginRender, etc. </dt> <dd>Correspond
     * to component render phases and annotations</dd> <dt>InvokePostRenderCleanupOnResources </dt> <dd>Makes sure
     * {@link org.apache.tapestry5.internal.InternalComponentResources#postRenderCleanup()} is invoked after a component
     * finishes rendering</dd> <dt>Secure</dt> <dd>Checks for the {@link org.apache.tapestry5.annotations.Secure}
     * annotation</dd> <dt>ContentType</dt> <dd>Checks for {@link org.apache.tapestry5.annotations.ContentType}
     * annotation</dd>  <dt>GenerateAccessors</dt> <dd>Generates accessor methods if {@link
     * org.apache.tapestry5.annotations.Property} annotation is present </dd> <dt>Cached</dt> <dd>Checks for the {@link
     * org.apache.tapestry5.annotations.Cached} annotation</dd><dt>Log</dt> <dd>Checks for the {@link
     * org.apache.tapestry5.annotations.Log} annotation</dd></dl>
     */
    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration,

            ObjectLocator locator,

            InjectionProvider injectionProvider,

            ComponentClassResolver resolver)
    {
        // TODO: Proper scheduling of all of this. Since a given field or method should
        // only have a single annotation, the order doesn't matter so much, as long as
        // UnclaimedField is last.

        configuration.add("Cached", locator.autobuild(CachedWorker.class));

        configuration.add("Meta", new MetaWorker());

        configuration.add("Inject", new InjectWorker(locator, injectionProvider));

        configuration.add("Secure", new SecureWorker());

        configuration.add("MixinAfter", new MixinAfterWorker());
        configuration.add("Component", new ComponentWorker(resolver));
        configuration.add("Mixin", new MixinWorker(resolver));
        configuration.add("OnEvent", new OnEventWorker());
        configuration.add("SupportsInformalParameters", new SupportsInformalParametersWorker());
        configuration.add("InjectPage", locator.autobuild(InjectPageWorker.class));
        configuration.add("InjectContainer", new InjectContainerWorker());
        configuration.add("InjectComponent", new InjectComponentWorker());
        configuration.add("RenderCommand", new RenderCommandWorker());

        // Default values for parameters are often some form of injection, so make sure
        // that Parameter fields are processed after injections.

        configuration.add("Parameter", locator.autobuild(ParameterWorker.class), "after:Inject*");

        // Workers for the component rendering state machine methods; this is in typical
        // execution order.

        add(configuration, TransformConstants.SETUP_RENDER_SIGNATURE, SetupRender.class, false);
        add(configuration, TransformConstants.BEGIN_RENDER_SIGNATURE, BeginRender.class, false);
        add(configuration, TransformConstants.BEFORE_RENDER_TEMPLATE_SIGNATURE, BeforeRenderTemplate.class, false);
        add(configuration, TransformConstants.BEFORE_RENDER_BODY_SIGNATURE, BeforeRenderBody.class, false);

        // These phases operate in reverse order.

        add(configuration, TransformConstants.AFTER_RENDER_BODY_SIGNATURE, AfterRenderBody.class, true);
        add(configuration, TransformConstants.AFTER_RENDER_TEMPLATE_SIGNATURE, AfterRenderTemplate.class, true);
        add(configuration, TransformConstants.AFTER_RENDER_SIGNATURE, AfterRender.class, true);
        add(configuration, TransformConstants.CLEANUP_RENDER_SIGNATURE, CleanupRender.class, true);

        // Ideally, these should be ordered pretty late in the process to make sure there are no
        // side effects with other workers that do work inside the page lifecycle methods.

        add(configuration, PageLoaded.class, TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, "pageLoaded");
        add(configuration, PageAttached.class, TransformConstants.CONTAINING_PAGE_DID_ATTACH_SIGNATURE, "pageAttached");
        add(configuration, PageDetached.class, TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE, "pageDetached");

        configuration.add("Retain", new RetainWorker());
        configuration.add("Persist", new PersistWorker());

        configuration.add("IncludeStylesheet", locator.autobuild(IncludeStylesheetWorker.class), "after:SetupRender");
        configuration.add("IncludeJavaScriptLibrary", locator.autobuild(IncludeJavaScriptLibraryWorker.class),
                          "after:SetupRender");

        configuration.add("InvokePostRenderCleanupOnResources", new InvokePostRenderCleanupOnResourcesWorker());

        configuration.add("ContentType", new ContentTypeWorker());

        configuration.add("Property", new PropertyWorker());

        // These must come after Property, since they actually delete fields that may still have the annotation
        configuration.add("ApplicationState", locator.autobuild(ApplicationStateWorker.class),
                          "after:Property");
        configuration.add("Environment", locator.autobuild(EnvironmentalWorker.class), "after:Property");

        configuration.add("Log", locator.autobuild(LogWorker.class));

        // This one is always last. Any additional private fields that aren't annotated will
        // be converted to clear out at the end of the request.

        configuration.add("UnclaimedField", new UnclaimedFieldWorker(), "after:*");

        configuration.add("PageActivationContext", new PageActivationContextWorker(), "before:OnEvent");
    }

    /**
     * <dl> <dt>Annotation</dt> <dd>Checks for {@link org.apache.tapestry5.beaneditor.DataType} annotation</dd>
     * <dt>Default  (ordered last)</dt> <dd>{@link org.apache.tapestry5.internal.services.DefaultDataTypeAnalyzer}
     * service ({@link #contributeDefaultDataTypeAnalyzer(org.apache.tapestry5.ioc.MappedConfiguration)} })</dd> </dl>
     */
    public static void contributeDataTypeAnalyzer(OrderedConfiguration<DataTypeAnalyzer> configuration,
                                                  @InjectService("DefaultDataTypeAnalyzer")
                                                  DataTypeAnalyzer defaultDataTypeAnalyzer)
    {
        configuration.add("Annotation", new AnnotationDataTypeAnalyzer());
        configuration.add("Default", defaultDataTypeAnalyzer, "after:*");
    }

    /**
     * Maps property types to data type names: <ul> <li>String --&gt; text <li>Number --&gt; number <li>Enum --&gt; enum
     * <li>Boolean --&gt; boolean <li>Date --&gt; date </ul>
     */
    public static void contributeDefaultDataTypeAnalyzer(MappedConfiguration<Class, String> configuration)
    {
        // This is a special case contributed to avoid exceptions when a property type can't be
        // matched. DefaultDataTypeAnalyzer converts the empty string to null.

        configuration.add(Object.class, "");

        configuration.add(String.class, "text");
        configuration.add(Number.class, "number");
        configuration.add(Enum.class, "enum");
        configuration.add(Boolean.class, "boolean");
        configuration.add(Date.class, "date");
    }

    public static void contributeBeanBlockSource(Configuration<BeanBlockContribution> configuration)
    {
        addEditBlock(configuration, "text");
        addEditBlock(configuration, "number");
        addEditBlock(configuration, "enum");
        addEditBlock(configuration, "boolean");
        addEditBlock(configuration, "date");
        addEditBlock(configuration, "password");

        // longtext uses a text area, not a text field

        addEditBlock(configuration, "longtext");

        addDisplayBlock(configuration, "enum");
        addDisplayBlock(configuration, "date");

        // Password and long text have special output needs.
        addDisplayBlock(configuration, "password");
        addDisplayBlock(configuration, "longtext");
    }

    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType)
    {
        addEditBlock(configuration, dataType, dataType);
    }

    private static void addEditBlock(Configuration<BeanBlockContribution> configuration, String dataType,
                                     String blockId)
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
     * Contributes the basic set of validators: <ul> <li>required</li> <li>minlength</li> <li>maxlength</li>
     * <li>min</li> <li>max</li> <li>regexp</li> </ul>
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
    }

    /**
     * Contributes the base set of injection providers: <dl> <dt>Default</dt> <dd>based on {@link
     * MasterObjectProvider}</dd> <dt>Block</dt> <dd>injects fields of type Block</dd> <dt>ComponentResources</dt>
     * <dd>give component access to its resources</dd> <dt>CommonResources</dt> <dd>access to properties of resources
     * (log, messages, etc.)</dd> <dt>Asset</dt> <dd>injection of assets (triggered via {@link Path} annotation), with
     * the path relative to the component class</dd> <dt>Service</dt> <dd>ordered last, for use when Inject is present
     * and nothing else works, matches field type against Tapestry IoC services</dd> </dl>
     */
    public static void contributeInjectionProvider(OrderedConfiguration<InjectionProvider> configuration,

                                                   MasterObjectProvider masterObjectProvider,

                                                   ObjectLocator locator,

                                                   SymbolSource symbolSource,

                                                   AssetSource assetSource)
    {
        configuration.add("Default", new DefaultInjectionProvider(masterObjectProvider, locator));

        configuration.add("ComponentResources", new ComponentResourcesInjectionProvider());

        // This comes after default, to deal with conflicts between injecting a String as the
        // component id, and injecting a string with @Symbol or @Value.

        configuration.add("CommonResources", new CommonResourcesInjectionProvider(), "after:Default");

        configuration.add("Asset", new AssetInjectionProvider(symbolSource, assetSource), "before:Default");

        configuration.add("Block", new BlockInjectionProvider(), "before:Default");

        // This needs to be the last one, since it matches against services
        // and might blow up if there is no match.
        configuration.add("Service", new ServiceInjectionProvider(locator), "after:*");
    }

    /**
     * Contributes two object providers: <dl> <dt>Alias</dt> <dd> Searches by type among {@linkplain AliasContribution
     * contributions} to the {@link Alias} service</dd> <dt>Asset<dt> <dd> Checks for the {@link Path} annotation, and
     * injects an {@link Asset}</dd> <dt>Service</dt> <dd>Injects based on the {@link Service} annotation, if
     * present</dd> </dl>
     */
    public static void contributeMasterObjectProvider(OrderedConfiguration<ObjectProvider> configuration,

                                                      @Local
                                                      final Alias alias,

                                                      @InjectService("AssetObjectProvider")
                                                      ObjectProvider assetObjectProvider)
    {
        // There's a nasty web of dependencies related to Alias; this wrapper class lets us
        // defer instantiating the Alias service implementation just long enough to defuse those
        // dependencies. The @Local annotation prevents a recursive call through the
        // MasterObjectProvider to resolve the Alias service itself; that is MasterObjectProvider
        // gets built using this proxy, then the proxy will trigger the construction of AliasImpl
        // (which itself needs MasterObjectProvider to resolve some dependencies).

        ObjectProvider wrapper = new ObjectProvider()
        {
            public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
            {
                return alias.getObjectProvider().provide(objectType, annotationProvider, locator);
            }
        };

        configuration.add("Alias", wrapper, "after:Value,Symbol");

        configuration.add("Asset", assetObjectProvider, "before:Alias");

        configuration.add("Service", new ServiceAnnotationObjectProvider(), "before:Alias");
    }


    public static void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration,

                                                           @InjectService("IgnoredPathsFilter")
                                                           HttpServletRequestFilter ignoredPathsFilter)
    {
        configuration.add("IgnoredPaths", ignoredPathsFilter);
    }

    /**
     * Continues a number of filters into the RequestHandler service: <dl> <dt>StaticFiles</dt> <dd>Checks to see if the
     * request is for an actual file, if so, returns true to let the servlet container process the request</dd>
     * <dt>CheckForUpdates</dt> <dd>Periodically fires events that checks to see if the file system sources for any
     * cached data has changed (see {@link org.apache.tapestry5.internal.services.CheckForUpdatesFilter}).
     * <dt>ErrorFilter</dt> <dd>Catches request errors and lets the {@link org.apache.tapestry5.services.RequestExceptionHandler}
     * handle them</dd> <dt>Localization</dt> <dd>Determines the locale for the current request from header data or
     * cookies in the request</dd> <dt>StoreIntoGlobals</dt> <dd>Stores the request and response into the {@link
     * org.apache.tapestry5.services.RequestGlobals} service (this is repeated at the end of the pipeline, in case any
     * filter substitutes the request or response). </dl>
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration, Context context,

                                         // @Inject not needed because its a long, not a String
                                         @Symbol(SymbolConstants.FILE_CHECK_INTERVAL)
                                         @IntermediateType(TimeInterval.class)
                                         long checkInterval,

                                         @Symbol(SymbolConstants.FILE_CHECK_UPDATE_TIMEOUT)
                                         @IntermediateType(TimeInterval.class)
                                         long updateTimeout,

                                         UpdateListenerHub updateListenerHub,

                                         LocalizationSetter localizationSetter,

                                         final EndOfRequestListenerHub endOfRequestListenerHub,

                                         ObjectLocator locator)
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
                    endOfRequestListenerHub.fire();
                }
            }
        };

        configuration.add("CheckForUpdates",
                          new CheckForUpdatesFilter(updateListenerHub, checkInterval, updateTimeout), "before:*");

        configuration.add("StaticFiles", staticFilesFilter);

        configuration.add("ErrorFilter", locator.autobuild(RequestErrorFilter.class));

        configuration.add("StoreIntoGlobals", storeIntoGlobals, "after:StaticFiles", "before:ErrorFilter");

        configuration.add("EndOfRequest", fireEndOfRequestEvent, "after:StoreIntoGlobals", "before:ErrorFilter");

        configuration.add("Localization", new LocalizationFilter(localizationSetter), "after:ErrorFilter");
    }

    /**
     * Contributes the basic set of named translators: <ul>  <li>string</li>  <li>byte</li> <li>integer</li>
     * <li>long</li> <li>float</li> <li>double</li> <li>short</li> </ul>
     */
    public static void contributeTranslatorSource(Configuration<Translator> configuration)
    {

        configuration.add(new StringTranslator());
        configuration.add(new ByteTranslator());
        configuration.add(new IntegerTranslator());
        configuration.add(new LongTranslator());
        configuration.add(new FloatTranslator());
        configuration.add(new DoubleTranslator());
        configuration.add(new ShortTranslator());
    }

    /**
     * Adds coercions: <ul> <li>String to {@link org.apache.tapestry5.SelectModel} <li>String to {@link
     * org.apache.tapestry5.corelib.data.InsertPosition} <li>Map to {@link org.apache.tapestry5.SelectModel}
     * <li>Collection to {@link GridDataSource} <li>null to {@link org.apache.tapestry5.grid.GridDataSource} <li>String
     * to {@link org.apache.tapestry5.corelib.data.GridPagerPosition} <li>List to {@link
     * org.apache.tapestry5.SelectModel} <li>{@link org.apache.tapestry5.runtime.ComponentResourcesAware} (typically, a
     * component) to {@link org.apache.tapestry5.ComponentResources} <li>String to {@link
     * org.apache.tapestry5.corelib.data.BlankOption} <li> {@link org.apache.tapestry5.ComponentResources} to {@link
     * org.apache.tapestry5.PropertyOverrides} <li>String to {@link org.apache.tapestry5.Renderable} <li>{@link
     * org.apache.tapestry5.Renderable} to {@link org.apache.tapestry5.Block} <li>String to {@link
     * java.text.DateFormat}</ul>
     */
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
    {
        add(configuration, ComponentResources.class, PropertyOverrides.class,
            new Coercion<ComponentResources, PropertyOverrides>()
            {
                public PropertyOverrides coerce(ComponentResources input)
                {
                    return new PropertyOverridesImpl(input);
                }
            });

        add(configuration, String.class, SelectModel.class, new Coercion<String, SelectModel>()
        {
            public SelectModel coerce(String input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        });

        add(configuration, Map.class, SelectModel.class, new Coercion<Map, SelectModel>()
        {
            @SuppressWarnings("unchecked")
            public SelectModel coerce(Map input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        });

        add(configuration, Collection.class, GridDataSource.class, new Coercion<Collection, GridDataSource>()
        {
            public GridDataSource coerce(Collection input)
            {
                return new CollectionGridDataSource(input);
            }
        });

        add(configuration, void.class, GridDataSource.class, new Coercion<Void, GridDataSource>()
        {
            private final GridDataSource source = new NullDataSource();

            public GridDataSource coerce(Void input)
            {
                return source;
            }
        });

        add(configuration, String.class, GridPagerPosition.class,
            StringToEnumCoercion.create(GridPagerPosition.class));

        add(configuration, String.class, InsertPosition.class, StringToEnumCoercion.create(InsertPosition.class));

        add(configuration, String.class, BlankOption.class, StringToEnumCoercion.create(BlankOption.class));

        add(configuration, List.class, SelectModel.class, new Coercion<List, SelectModel>()
        {
            @SuppressWarnings("unchecked")
            public SelectModel coerce(List input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        });

        add(configuration, String.class, Pattern.class, new Coercion<String, Pattern>()
        {
            public Pattern coerce(String input)
            {
                return Pattern.compile(input);
            }
        });

        add(configuration, ComponentResourcesAware.class, ComponentResources.class,
            new Coercion<ComponentResourcesAware, ComponentResources>()
            {

                public ComponentResources coerce(ComponentResourcesAware input)
                {
                    return input.getComponentResources();
                }
            });

        add(configuration, String.class, Renderable.class, new Coercion<String, Renderable>()
        {
            public Renderable coerce(String input)
            {
                return new StringRenderable(input);
            }
        });

        add(configuration, Renderable.class, Block.class, new Coercion<Renderable, Block>()
        {
            public Block coerce(Renderable input)
            {
                return new RenderableAsBlock(input);
            }
        });

        add(configuration, String.class, DateFormat.class, new Coercion<String, DateFormat>()
        {
            public DateFormat coerce(String input)
            {
                return new SimpleDateFormat(input);
            }
        });
    }

    /**
     * Adds built-in constraint generators: <ul> <li>PrimtiveField -- primitive fields are always required
     * <li>ValidateAnnotation -- adds constraints from a {@link Validate} annotation </ul>
     */
    public static void contributeValidationConstraintGenerator(
            OrderedConfiguration<ValidationConstraintGenerator> configuration)
    {
        configuration.add("PrimitiveField", new PrimitiveFieldConstraintGenerator());
        configuration.add("ValidateAnnotation", new ValidateAnnotationConstraintGenerator());
    }

    private static <S, T> void add(Configuration<CoercionTuple> configuration, Class<S> sourceType, Class<T> targetType,
                                   Coercion<S, T> coercion)
    {
        CoercionTuple<S, T> tuple = new CoercionTuple<S, T>(sourceType, targetType, coercion);

        configuration.add(tuple);
    }

    private static void add(OrderedConfiguration<ComponentClassTransformWorker> configuration,
                            Class<? extends Annotation> annotationClass,
                            TransformMethodSignature lifecycleMethodSignature, String methodAlias)
    {
        ComponentClassTransformWorker worker = new PageLifecycleAnnotationWorker(annotationClass,
                                                                                 lifecycleMethodSignature, methodAlias);

        String name = TapestryInternalUtils.lastTerm(annotationClass.getName());

        configuration.add(name, worker);
    }

    private static void add(OrderedConfiguration<ComponentClassTransformWorker> configuration,
                            TransformMethodSignature signature, Class<? extends Annotation> annotationClass,
                            boolean reverse)
    {
        // make the name match the annotation class name.

        String name = annotationClass.getSimpleName();

        configuration.add(name, new RenderPhaseMethodWorker(signature, annotationClass, reverse));
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

    public ComponentClassResolver buildComponentClassResolver(ServiceResources resources)
    {
        ComponentClassResolverImpl service = resources.autobuild(ComponentClassResolverImpl.class);

        // Allow the resolver to clean its cache when the source is invalidated

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    @Marker(ClasspathProvider.class)
    public AssetFactory buildClasspathAssetFactory(ResourceCache resourceCache, ClasspathAssetAliasManager aliasManager)
    {
        ClasspathAssetFactory factory = new ClasspathAssetFactory(resourceCache, aliasManager);

        resourceCache.addInvalidationListener(factory);

        return factory;
    }

    @Marker(ContextProvider.class)
    public AssetFactory buildContextAssetFactory(ApplicationGlobals globals, RequestPathOptimizer optimizer)
    {
        return new ContextAssetFactory(request, globals.getContext(), optimizer);
    }

    /**
     * Builds the PropBindingFactory as a chain of command. The terminator of the chain is responsible for ordinary
     * property names (and property paths). Contributions to the service cover additional special cases, such as simple
     * literal values.
     *
     * @param configuration contributions of special factories for some constants, each contributed factory may return a
     *                      binding if applicable, or null otherwise
     */
    public BindingFactory buildPropBindingFactory(List<BindingFactory> configuration,
                                                  PropertyConduitSource propertyConduitSource)
    {
        PropBindingFactory service = new PropBindingFactory(propertyConduitSource);

        configuration.add(service);

        return chainBuilder.build(BindingFactory.class, configuration);
    }

    /**
     * Builds the source of {@link Messages} containing validation messages. The contributions are paths to message
     * bundles (resource paths within the classpath); the default contribution is "org/apache/tapestry5/internal/ValidationMessages".
     */
    public ValidationMessagesSource buildValidationMessagesSource(List<String> configuration,

                                                                  UpdateListenerHub updateListenerHub,

                                                                  @ClasspathProvider AssetFactory classpathAssetFactory)
    {
        ValidationMessagesSourceImpl service = new ValidationMessagesSourceImpl(configuration,
                                                                                classpathAssetFactory.getRootResource());
        updateListenerHub.addUpdateListener(service);

        return service;
    }

    public MetaDataLocator buildMetaDataLocator(ServiceResources resources)
    {
        MetaDataLocatorImpl service = resources.autobuild(MetaDataLocatorImpl.class);

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public PersistentFieldStrategy buildClientPersistentFieldStrategy(LinkFactory linkFactory,
                                                                      ServiceResources resources)
    {
        ClientPersistentFieldStrategy service = resources.autobuild(ClientPersistentFieldStrategy.class);

        linkFactory.addListener(service);

        return service;
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry5.RenderSupport} inside this thread's {@link
     * Environment}.
     */
    public RenderSupport buildRenderSupport()
    {
        return environmentalBuilder.build(RenderSupport.class);
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry5.services.FormSupport} inside this thread's {@link
     * org.apache.tapestry5.services.Environment}.
     */
    public FormSupport buildFormSupport()
    {
        return environmentalBuilder.build(FormSupport.class);
    }

    /**
     * Allows the exact steps in the component class transformation process to be defined.
     */
    public ComponentClassTransformWorker buildComponentClassTransformWorker(
            List<ComponentClassTransformWorker> configuration)
    {
        return chainBuilder.build(ComponentClassTransformWorker.class, configuration);
    }

    /**
     * Analyzes properties to determine the data types, used to {@linkplain #contributeBeanBlockSource(org.apache.tapestry5.ioc.Configuration)}
     * locale display and edit blocks} for properties.  The default behaviors look for a {@link
     * org.apache.tapestry5.beaneditor.DataType} annotation before deriving the data type from the property type.
     */
    @Marker(Primary.class)
    public DataTypeAnalyzer buildDataTypeAnalyzer(List<DataTypeAnalyzer> configuration)
    {
        return chainBuilder.build(DataTypeAnalyzer.class, configuration);
    }

    /**
     * A chain of command for providing values for {@link Inject}-ed fields in component classes. The service's
     * configuration can be extended to allow for different automatic injections (based on some combination of field
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
        ApplicationInitializer terminator = new ApplicationInitializer()
        {
            public void initializeApplication(Context context)
            {
                applicationGlobals.storeContext(context);
            }
        };

        return pipelineBuilder.build(logger, ApplicationInitializer.class, ApplicationInitializerFilter.class,
                                     configuration, terminator);
    }

    public HttpServletRequestHandler buildHttpServletRequestHandler(Logger logger,

                                                                    List<HttpServletRequestFilter> configuration,

                                                                    @Primary
                                                                    final RequestHandler handler,

                                                                    @Inject @Symbol(SymbolConstants.CHARSET)
                                                                    final String applicationCharset)
    {
        HttpServletRequestHandler terminator = new HttpServletRequestHandler()
        {
            public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
                    throws IOException
            {
                requestGlobals.storeServletRequestResponse(servletRequest, servletResponse);

                Request request = new RequestImpl(servletRequest, applicationCharset);
                Response response = new ResponseImpl(servletResponse);

                // TAP5-257: Make sure that the "initial guess" for request/response is available, even if
                // some filter in the RequestHandler pipeline replaces them.

                requestGlobals.storeRequestResponse(request, response);

                // Transition from the Servlet API-based pipeline, to the Tapestry-based pipeline.

                return handler.service(request, response);
            }
        };

        return pipelineBuilder.build(logger, HttpServletRequestHandler.class, HttpServletRequestFilter.class,
                                     configuration, terminator);
    }

    @Marker(Primary.class)
    public RequestHandler buildRequestHandler(Logger logger, List<RequestFilter> configuration,

                                              @Primary
                                              final Dispatcher masterDispatcher)
    {
        RequestHandler terminator = new RequestHandler()
        {
            public boolean service(Request request, Response response) throws IOException
            {
                // Update RequestGlobals with the current request/response (in case some filter replaced the
                // normal set).
                requestGlobals.storeRequestResponse(request, response);

                return masterDispatcher.dispatch(request, response);
            }
        };

        return pipelineBuilder.build(logger, RequestHandler.class, RequestFilter.class, configuration, terminator);
    }

    public ServletApplicationInitializer buildServletApplicationInitializer(Logger logger,
                                                                            List<ServletApplicationInitializerFilter> configuration,

                                                                            @Primary
                                                                            final ApplicationInitializer initializer)
    {
        ServletApplicationInitializer terminator = new ServletApplicationInitializer()
        {
            public void initializeApplication(ServletContext context)
            {
                applicationGlobals.storeServletContext(context);

                // And now, down the (Web) ApplicationInitializer pipeline ...

                initializer.initializeApplication(new ContextImpl(context));
            }
        };

        return pipelineBuilder.build(logger, ServletApplicationInitializer.class,
                                     ServletApplicationInitializerFilter.class, configuration, terminator);
    }

    /**
     * The component event result processor used for normal component requests.
     */
    @Marker({Primary.class, Traditional.class})
    public ComponentEventResultProcessor buildComponentEventResultProcessor(
            Map<Class, ComponentEventResultProcessor> configuration)
    {
        return constructComponentEventResultProcessor(configuration);
    }

    /**
     * The component event result processor used for Ajax-oriented component requests.
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
     * The default data type analyzer is the final analyzer consulted and identifies the type entirely pased on the
     * property type, working against its own configuration (mapping property type class to data type).
     */
    public DataTypeAnalyzer buildDefaultDataTypeAnalyzer(ServiceResources resources)
    {
        DefaultDataTypeAnalyzer service = resources.autobuild(DefaultDataTypeAnalyzer.class);

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public TranslatorSource buildTranslatorSource(ServiceResources resources)
    {
        TranslatorSourceImpl service = resources.autobuild(TranslatorSourceImpl.class);

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    @Marker(Primary.class)
    public ObjectRenderer buildObjectRenderer(Map<Class, ObjectRenderer> configuration)
    {
        StrategyRegistry<ObjectRenderer> registry = StrategyRegistry.newInstance(ObjectRenderer.class, configuration);

        return strategyBuilder.build(registry);
    }


    /**
     * Returns a {@link org.apache.tapestry5.ioc.services.ClassFactory} that can be used to create extra classes around
     * component classes. This ClassFactory will be cleared whenever an underlying component class is discovered to have
     * changed. Use of this class factory implies that your code will become aware of this (if necessary) to discard any
     * cached object (alas, this currently involves dipping into the internals side to register for the correct
     * notifications). Failure to properly clean up can result in really nasty PermGen space memory leaks.
     */
    @Marker(ComponentLayer.class)
    public ClassFactory buildComponentClassFactory()
    {
        return shadowBuilder.build(componentInstantiatorSource, "classFactory", ClassFactory.class);
    }


    /**
     * Ordered contributions to the MasterDispatcher service allow different URL matching strategies to occur.
     */
    @Marker(Primary.class)
    public Dispatcher buildMasterDispatcher(List<Dispatcher> configuration)
    {
        return chainBuilder.build(Dispatcher.class, configuration);
    }

    public PropertyConduitSource buildPropertyConduitSource(@ComponentLayer ClassFactory componentClassFactory)
    {
        PropertyConduitSourceImpl service = new PropertyConduitSourceImpl(propertyAccess, componentClassFactory);

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Builds a shadow of the RequestGlobals.request property. Note again that the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Request buildRequest()
    {
        return shadowBuilder.build(requestGlobals, "request", Request.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.HTTPServletRequest property.  Generally, you should inject the {@link
     * Request} service instead, as future version of Tapestry may operate beyond just the servlet API.
     */
    public HttpServletRequest buildHttpServletRequest()
    {
        return shadowBuilder.build(requestGlobals, "HTTPServletRequest", HttpServletRequest.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.response property. Note again that the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Response buildResponse()
    {
        return shadowBuilder.build(requestGlobals, "response", Response.class);
    }


    /**
     * The MarkupRenderer service is used to render a full page as markup.  Supports an ordered configuration of {@link
     * org.apache.tapestry5.services.MarkupRendererFilter}s.
     *
     * @param pageRenderQueue handles the bulk of the work
     * @param logger          used to log errors building the pipeline
     * @param configuration   filters on this service
     * @return the service
     */
    public MarkupRenderer buildMarkupRenderer(final PageRenderQueue pageRenderQueue, Logger logger,
                                              List<MarkupRendererFilter> configuration)
    {
        MarkupRenderer terminator = new MarkupRenderer()
        {
            public void renderMarkup(MarkupWriter writer)
            {
                pageRenderQueue.render(writer);
            }
        };

        return pipelineBuilder.build(logger, MarkupRenderer.class, MarkupRendererFilter.class, configuration,
                                     terminator);
    }

    /**
     * A wrapper around {@link org.apache.tapestry5.internal.services.PageRenderQueue} used for partial page renders.
     * Supports an ordered configuration of {@link org.apache.tapestry5.services.PartialMarkupRendererFilter}s.
     *
     * @param logger        used to log warnings creating the pipeline
     * @param configuration filters for the service
     * @param renderQueue   does most of the work
     * @return the service
     * @see #contributePartialMarkupRenderer(org.apache.tapestry5.ioc.OrderedConfiguration, org.apache.tapestry5.Asset,
     *      org.apache.tapestry5.ioc.services.SymbolSource, AssetSource, ValidationMessagesSource)
     */
    public PartialMarkupRenderer buildPartialMarkupRenderer(Logger logger,
                                                            List<PartialMarkupRendererFilter> configuration,
                                                            final PageRenderQueue renderQueue)
    {

        PartialMarkupRenderer terminator = new PartialMarkupRenderer()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply)
            {
                renderQueue.renderPartial(writer, reply);
            }
        };

        return pipelineBuilder.build(logger, PartialMarkupRenderer.class, PartialMarkupRendererFilter.class,
                                     configuration, terminator);
    }

    public PageRenderRequestHandler buildPageRenderRequestHandler(List<PageRenderRequestFilter> configuration,
                                                                  Logger logger, ServiceResources resources)
    {
        return pipelineBuilder.build(logger, PageRenderRequestHandler.class, PageRenderRequestFilter.class,
                                     configuration, resources.autobuild(PageRenderRequestHandlerImpl.class));
    }


    /**
     * Builds the component action request handler for traditional (non-Ajax) requests. These typically result in a
     * redirect to a Tapestry render URL.
     *
     * @see org.apache.tapestry5.internal.services.ComponentEventRequestHandlerImpl
     */
    @Marker(Traditional.class)
    public ComponentEventRequestHandler buildComponentEventRequestHandler(
            List<ComponentEventRequestFilter> configuration, Logger logger, ServiceResources resources)
    {
        return pipelineBuilder.build(logger, ComponentEventRequestHandler.class, ComponentEventRequestFilter.class,
                                     configuration, resources.autobuild(ComponentEventRequestHandlerImpl.class));
    }

    /**
     * Builds the action request handler for Ajax requests, based on a {@linkplain org.apache.tapestry5.ioc.services.PipelineBuilder
     * pipeline} around {@link org.apache.tapestry5.internal.services.AjaxComponentEventRequestHandler}. Filters on the
     * request handler are supported here as well.
     */
    @Marker(Ajax.class)
    public ComponentEventRequestHandler buildAjaxComponentEventRequestHandler(
            List<ComponentEventRequestFilter> configuration, Logger logger, ServiceResources resources)
    {
        return pipelineBuilder.build(logger, ComponentEventRequestHandler.class, ComponentEventRequestFilter.class,
                                     configuration, resources.autobuild(AjaxComponentEventRequestHandler.class));
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

    public ApplicationStatePersistenceStrategy buildSessionApplicationStatePersistenceStrategy(ObjectLocator locator,
                                                                                               EndOfRequestListenerHub hub)
    {
        SessionApplicationStatePersistenceStrategy service = locator.autobuild(
                SessionApplicationStatePersistenceStrategy.class);

        hub.addEndOfRequestListener(service);

        return service;
    }

    public void contributeAssetSource(MappedConfiguration<String, AssetFactory> configuration,
                                      @ContextProvider AssetFactory contextAssetFactory,

                                      @ClasspathProvider AssetFactory classpathAssetFactory)
    {
        configuration.add("context", contextAssetFactory);
        configuration.add("classpath", classpathAssetFactory);
    }

    /**
     * Contributes handlers for the following types: <dl> <dt>Object</dt> <dd>Failure case, added to provide a more
     * useful exception message</dd> <dt>{@link Link}</dt> <dd>Sends a redirect to the link (which is typically a page
     * render link)</dd> <dt>String</dt> <dd>Sends a page render redirect</dd> <dt>Class</dt> <dd>Interpreted as the
     * class name of a page, sends a page render render redirect (this is more refactoring safe than the page name)</dd>
     * <dt>{@link Component}</dt> <dd>A page's root component (though a non-root component will work, but will generate
     * a warning). A direct to the containing page is sent.</dd> <dt>{@link org.apache.tapestry5.StreamResponse}</dt>
     * <dd>The stream response is sent as the actual reply.</dd> <dt>URL</dt> <dd>Sends a redirect to a (presumably)
     * external URL</dd> </dl>
     */
    public void contributeComponentEventResultProcessor(

            @InjectService("ComponentInstanceResultProcessor")
            ComponentEventResultProcessor componentInstanceProcessor,

            ObjectLocator locator,

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

        configuration.add(String.class, locator.autobuild(PageNameComponentEventResultProcessor.class));

        configuration.add(Class.class, locator.autobuild(ClassResultProcessor.class));

        configuration.add(Component.class, componentInstanceProcessor);

        configuration.add(StreamResponse.class, locator.autobuild(StreamResponseResultProcessor.class));
    }


    /**
     * Contributes handlers for the following types: <dl> <dt>Object</dt> <dd>Failure case, added to provide more useful
     * exception message</dd> <dt>{@link RenderCommand}</dt> <dd>Typically, a {@link org.apache.tapestry5.Block}</dd>
     * <dt>{@link org.apache.tapestry5.annotations.Component}</dt> <dd>Renders the component and its body (unless its a
     * page, in which case a redirect JSON response is sent)</dd> <dt>{@link org.apache.tapestry5.json.JSONObject} or
     * {@link org.apache.tapestry5.json.JSONArray}</dt> <dd>The JSONObject is returned as a text/javascript
     * response</dd> <dt>{@link org.apache.tapestry5.StreamResponse}</dt> <dd>The stream response is sent as the actual
     * response</dd> <dt>String</dt> <dd>Interprets the value as a logical page name and sends a client response to
     * redirect to that page</dd> <dt>{@link org.apache.tapestry5.Link}</dt> <dd>Sends a JSON response to redirect to
     * the link</dd> <dt>{@link Class}</dt> <dd>Treats the class as a page class and sends a redirect for a page render
     * for that page</dd> </dl>
     */

    public void contributeAjaxComponentEventResultProcessor(
            MappedConfiguration<Class, ComponentEventResultProcessor> configuration, ObjectLocator locator)
    {
        configuration.add(RenderCommand.class, locator.autobuild(RenderCommandComponentEventResultProcessor.class));
        configuration.add(Component.class, locator.autobuild(AjaxComponentInstanceEventResultProcessor.class));
        configuration.add(JSONObject.class, locator.autobuild(JSONObjectEventResultProcessor.class));
        configuration.add(JSONArray.class, locator.autobuild(JSONArrayEventResultProcessor.class));
        configuration.add(StreamResponse.class, new StreamResponseResultProcessor(response));
        configuration.add(String.class, locator.autobuild(AjaxPageNameComponentEventResultProcessor.class));
        configuration.add(Link.class, locator.autobuild(AjaxLinkComponentEventResultProcessor.class));
        configuration.add(Class.class, locator.autobuild(AjaxPageClassComponentEventResultProcessor.class));
    }

    /**
     * The MasterDispatcher is a chain-of-command of individual Dispatchers, each handling (like a servlet) a particular
     * kind of incoming request. <dl> <dt>RootPath</dt> <dd>Renders the start page for the "/" request</dd>
     * <dt>Asset</dt> <dd>Provides access to classpath assets</dd> <dt>PageRender</dt> <dd>Identifies the {@link
     * org.apache.tapestry5.services.PageRenderRequestParameters} and forwards onto {@link
     * PageRenderRequestHandler}</dd> <dt>ComponentEvent</dt> <dd>Identifies the {@link ComponentEventRequestParameters}
     * and forwards onto the {@link ComponentEventRequestHandler}</dd> </dl>
     */
    public void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration,
                                           @InjectService("AssetProtectionDispatcher") Dispatcher assetProt,
                                           ObjectLocator locator)
    {
        // Looks for the root path and renders the start page. This is maintained for compatibility
        // with earlier versions of Tapestry 5, it is recommended that an Index page be used instead.

        configuration.add("RootPath",
                          locator.autobuild(RootPathDispatcher.class),
                          "before:Asset");

        //this goes before asset to make sure that only allowed assets are streamed to the client.
        configuration.add("AssetProtection", assetProt, "before:Asset");

        // This goes first because an asset to be streamed may have an file extension, such as
        // ".html", that will confuse the later dispatchers.

        configuration.add("Asset",
                          locator.autobuild(AssetDispatcher.class), "before:ComponentEvent");


        configuration.add("ComponentEvent", locator.autobuild(ComponentEventDispatcher.class),
                          "before:PageRender");

        configuration.add("PageRender",
                          locator.autobuild(PageRenderDispatcher.class));
    }

    /**
     * Contributes a default object renderer for type Object, plus specialized renderers for {@link
     * org.apache.tapestry5.services.Request}, {@link org.apache.tapestry5.ioc.Location}, {@link
     * org.apache.tapestry5.ComponentResources}, {@link org.apache.tapestry5.EventContext}, List, and Object[].
     */
    public void contributeObjectRenderer(MappedConfiguration<Class, ObjectRenderer> configuration,

                                         @InjectService("LocationRenderer")
                                         ObjectRenderer locationRenderer,

                                         final TypeCoercer typeCoercer,

                                         ObjectLocator locator)
    {
        configuration.add(Object.class, new ObjectRenderer()
        {
            public void render(Object object, MarkupWriter writer)
            {
                writer.write(String.valueOf(object));
            }
        });

        configuration.add(Request.class, locator.autobuild(RequestRenderer.class));

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

        configuration.add(List.class, locator.autobuild(ListRenderer.class));
        configuration.add(Object[].class, locator.autobuild(ObjectArrayRenderer.class));
        configuration.add(ComponentResources.class, locator.autobuild(ComponentResourcesRenderer.class));
        configuration.add(EventContext.class, locator.autobuild(EventContextRenderer.class));
    }


    /**
     * Adds page render filters, each of which provides an {@link org.apache.tapestry5.annotations.Environmental}
     * service. Filters often provide {@link org.apache.tapestry5.annotations.Environmental} services needed by
     * components as they render. <dl> <dt>DocumentLinker</dt> <dd>Provides {@link org.apache.tapestry5.internal.services.DocumentLinker}
     * <dt>RenderSupport</dt>  <dd>Provides {@link org.apache.tapestry5.RenderSupport}</dd>
     * <dt>ClientBehaviorSupport</dt> <dd>Provides {@link org.apache.tapestry5.internal.services.ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt> <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>DefaultValidationDecorator</dt> <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (as an instance
     * of {@link org.apache.tapestry5.internal.DefaultValidationDecorator})</dd> </dl>
     */
    public void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration,

                                         @Symbol(SymbolConstants.PRODUCTION_MODE)
                                         final boolean productionMode,

                                         @Symbol(SymbolConstants.SCRIPTS_AT_TOP)
                                         final boolean scriptsAtTop,

                                         @Path("${tapestry.default-stylesheet}")
                                         final Asset stylesheetAsset,

                                         @Path("${tapestry.spacer-image}")
                                         final Asset spacerImage,

                                         final ValidationMessagesSource validationMessagesSource,

                                         final SymbolSource symbolSource,

                                         final AssetSource assetSource)
    {
        MarkupRendererFilter documentLinker = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinkerImpl linker = new DocumentLinkerImpl(productionMode, scriptsAtTop);

                environment.push(DocumentLinker.class, linker);

                renderer.renderMarkup(writer);

                environment.pop(DocumentLinker.class);

                linker.updateDocument(writer.getDocument());
            }
        };

        MarkupRendererFilter renderSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                RenderSupportImpl support = new RenderSupportImpl(linker, symbolSource, assetSource,

                                                                  // Core scripts added to any page that uses scripting

                                                                  "${tapestry.scriptaculous}/prototype.js",
                                                                  "${tapestry.scriptaculous}/scriptaculous.js",
                                                                  "${tapestry.scriptaculous}/effects.js",

                                                                  // Uses functions defined by the prior three

                                                                  "org/apache/tapestry5/tapestry.js");

                support.addStylesheetLink(stylesheetAsset, null);

                environment.push(RenderSupport.class, support);

                renderer.renderMarkup(writer);

                environment.pop(RenderSupport.class);

                support.commit();
            }
        };

        MarkupRendererFilter clientBehaviorSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                RenderSupport renderSupport = environment.peekRequired(RenderSupport.class);

                ClientBehaviorSupportImpl clientBehaviorSupport = new ClientBehaviorSupportImpl(renderSupport);

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
                Messages messages = validationMessagesSource.getValidationMessages(threadLocale.getLocale());

                ValidationDecorator decorator = new DefaultValidationDecorator(environment, messages, spacerImage,
                                                                               writer);

                environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer);

                environment.pop(ValidationDecorator.class);
            }
        };


        configuration.add("DocumentLinker", documentLinker, "before:RenderSupport");
        configuration.add("RenderSupport", renderSupport);
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:RenderSupport");
        configuration.add("Heartbeat", heartbeat, "after:RenderSupport");
        configuration.add("DefaultValidationDecorator", defaultValidationDecorator, "after:Heartbeat");
    }


    /**
     * Contributes {@link PartialMarkupRendererFilter}s used when rendering a partial Ajax response. <dl>
     * <dt>DocumentLinker <dd>Provides {@link org.apache.tapestry5.internal.services.DocumentLinker} <dt>
     * PageRenderSupport     </dt> <dd>Provides {@link org.apache.tapestry5.RenderSupport}</dd>
     * <dt>ClientBehaviorSupport</dt> <dd>Provides {@link org.apache.tapestry5.internal.services.ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt> <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>DefaultValidationDecorator</dt> <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (as an instance
     * of {@link org.apache.tapestry5.internal.DefaultValidationDecorator})</dd> </dl>
     */
    public void contributePartialMarkupRenderer(OrderedConfiguration<PartialMarkupRendererFilter> configuration,

                                                @Path("${tapestry.spacer-image}")
                                                final Asset spacerImage,

                                                final SymbolSource symbolSource,

                                                final AssetSource assetSource,

                                                final ValidationMessagesSource validationMessagesSource)
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


        PartialMarkupRendererFilter renderSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                String uid = Long.toHexString(System.currentTimeMillis());

                String namespace = ":" + uid;

                IdAllocator idAllocator = new IdAllocator(namespace);

                DocumentLinker linker = environment.peekRequired(DocumentLinker.class);

                RenderSupportImpl support = new RenderSupportImpl(linker, symbolSource, assetSource,
                                                                  idAllocator);

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
                RenderSupport renderSupport = environment.peekRequired(RenderSupport.class);

                ClientBehaviorSupportImpl support = new ClientBehaviorSupportImpl(renderSupport);

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
                Messages messages = validationMessagesSource.getValidationMessages(threadLocale.getLocale());

                ValidationDecorator decorator = new DefaultValidationDecorator(environment, messages, spacerImage,
                                                                               writer);

                environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer, reply);

                environment.pop(ValidationDecorator.class);
            }
        };


        configuration.add("DocumentLinker", documentLinker, "before:RenderSupport");
        configuration.add("RenderSupport", renderSupport);
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:RenderSupport");
        configuration.add("Heartbeat", heartbeat, "after:RenderSupport");
        configuration.add("DefaultValidationDecorator", defaultValidationDecorator, "after:Heartbeat");
    }

    /**
     * Contributes several strategies: <dl> <dt>session <dd>Values are stored in the {@link Session} <dt>flash
     * <dd>Values are stored in the {@link Session}, until the next request (for the page) <dt>client <dd>Values are
     * encoded into URLs (or hidden form fields) </dl>
     */
    public void contributePersistentFieldManager(MappedConfiguration<String, PersistentFieldStrategy> configuration,

                                                 Request request,

                                                 @InjectService("ClientPersistentFieldStrategy")
                                                 PersistentFieldStrategy clientStrategy)
    {
        configuration.add("session", new SessionPersistentFieldStrategy(request));
        configuration.add(PersistenceConstants.FLASH, new FlashPersistentFieldStrategy(request));
        configuration.add("client", clientStrategy);
    }

    /**
     * Contributes org/apache/tapestry5/internal/ValidationMessages as "Default", ordered first.
     */
    public void contributeValidationMessagesSource(OrderedConfiguration<String> configuration)
    {
        configuration.add("Default", "org/apache/tapestry5/internal/ValidationMessages", "before:*");
    }

    public ValueEncoderSource buildValueEncoderSource(Map<Class, ValueEncoderFactory> configuration)
    {
        ValueEncoderSourceImpl service = new ValueEncoderSourceImpl(configuration);

        componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Contributes {@link ValueEncoderFactory}s for types: <ul> <li>Object <li>String <li>Enum </ul>
     */
    @SuppressWarnings("unchecked")
    public static void contributeValueEncoderSource(MappedConfiguration<Class, ValueEncoderFactory> configuration,
                                                    ObjectLocator locator)
    {
        configuration.add(Object.class, locator.autobuild(TypeCoercedValueEncoderFactory.class));
        configuration.add(String.class, GenericValueEncoderFactory.create(new StringValueEncoder()));
        configuration.add(Enum.class, new EnumValueEncoderFactory());
    }


    /**
     * Contributes a single filter, "Secure", which checks for non-secure requests that access secure pages.
     */
    public void contributePageRenderRequestHandler(OrderedConfiguration<PageRenderRequestFilter> configuration,
                                                   final RequestSecurityManager securityManager)
    {
        PageRenderRequestFilter secureFilter = new PageRenderRequestFilter()
        {
            public void handle(PageRenderRequestParameters parameters, PageRenderRequestHandler handler) throws
                    IOException
            {

                if (securityManager.checkForInsecureRequest(parameters.getLogicalPageName())) return;

                handler.handle(parameters);
            }
        };

        configuration.add("Secure", secureFilter);
    }


    /**
     * Configures the extensions that will require a digest to be downloaded via the asset dispatcher. Most resources
     * are "safe", they don't require a digest. For unsafe resources, the digest is incorporated into the URL to ensure
     * that the client side isn't just "fishing".
     * <p/>
     * The extensions must be all lower case.
     * <p/>
     * This contributes "class" and "tml" (the template extension).
     *
     * @param configuration collection of extensions
     */
    public static void contributeResourceDigestGenerator(Configuration<String> configuration)
    {
        // Java class files always require a digest.
        configuration.add("class");

        // Likewise, we don't want people fishing for templates.
        configuration.add(InternalConstants.TEMPLATE_EXTENSION);
    }

    public static void contributeTemplateParser(MappedConfiguration<String, URL> config)
    {
        // Any class inside the internal module would do. Or we could move all these
        // files to o.a.t.services.

        Class c = UpdateListenerHub.class;
        config.add("-//W3C//DTD XHTML 1.0 Strict//EN", c.getResource("xhtml1-strict.dtd"));
        config.add("-//W3C//DTD XHTML 1.0 Transitional//EN", c
                .getResource("xhtml1-transitional.dtd"));
        config.add("-//W3C//DTD XHTML 1.0 Frameset//EN", c.getResource("xhtml1-frameset.dtd"));
        config.add("-//W3C//DTD HTML 4.01//EN", c.getResource("xhtml1-strict.dtd"));
        config.add("-//W3C//DTD HTML 4.01 Transitional//EN", c
                .getResource("xhtml1-transitional.dtd"));
        config.add("-//W3C//DTD HTML 4.01 Frameset//EN", c.getResource("xhtml1-frameset.dtd"));
        config.add("-//W3C//ENTITIES Latin 1 for XHTML//EN", c.getResource("xhtml-lat1.ent"));
        config.add("-//W3C//ENTITIES Symbols for XHTML//EN", c.getResource("xhtml-symbol.ent"));
        config.add("-//W3C//ENTITIES Special for XHTML//EN", c.getResource("xhtml-special.ent"));
    }

    /**
     * Contributes factory defaults that map be overridden.
     *
     * @see TapestryModule#contributeClasspathAssetAliasManager(org.apache.tapestry5.ioc.MappedConfiguration, String,
     *      String, String)
     */
    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        // Remember this is request-to-request time, presumably it'll take the developer more than
        // one second to make a change, save it, and switch back to the browser.

        configuration.add(SymbolConstants.FILE_CHECK_INTERVAL, "1 s");
        configuration.add(SymbolConstants.FILE_CHECK_UPDATE_TIMEOUT, "50 ms");

        // This should be overridden for particular applications.
        configuration.add(SymbolConstants.SUPPORTED_LOCALES,
                          "en,it,es,zh_CN,pt_PT,de,ru,hr,fi_FI,sv_SE,fr_FR,da,pt_BR,ja,el,bg,no_NB");

        configuration.add(SymbolConstants.TAPESTRY_VERSION,
                          VersionUtils.readVersionNumber(
                                  "META-INF/maven/org.apache.tapestry/tapestry-core/pom.properties"));

        configuration.add("tapestry.default-cookie-max-age", "7 d");

        configuration.add("tapestry.start-page-name", "start");

        configuration.add("tapestry.default-stylesheet", "classpath:/org/apache/tapestry5/default.css");
        configuration.add("tapestry.spacer-image", "classpath:/org/apache/tapestry5/spacer.gif");

        configuration.add("tapestry.page-pool.soft-limit", "5");
        configuration.add("tapestry.page-pool.soft-wait", "10 ms");
        configuration.add("tapestry.page-pool.hard-limit", "20");
        configuration.add("tapestry.page-pool.active-window", "10 m");

        configuration.add(SymbolConstants.SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS, "false");

        configuration.add(SymbolConstants.FORCE_ABSOLUTE_URIS, "false");

        configuration.add(SymbolConstants.PRODUCTION_MODE, "true");

        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, "true");

        configuration.add(MetaDataConstants.SECURE_PAGE, "false");

        configuration.add(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED, "true");

        // This is designed to make it easy to keep synchronized with script.aculo.ous. As we
        // support a new version, we create a new folder, and update the path entry. We can then
        // delete the old version folder (or keep it around). This should be more manageable than
        // overwriting the local copy with updates (it's too easy for files deleted between scriptaculous
        // releases to be accidentally left lying around). There's also a ClasspathAliasManager
        // contribution based on the path.

        configuration.add("tapestry.scriptaculous", "classpath:${tapestry.scriptaculous.path}");
        configuration.add("tapestry.scriptaculous.path", "org/apache/tapestry5/scriptaculous_1_8_1");

        // Likewise for WebFX DatePicker, currently version 1.0.6

        configuration.add("tapestry.datepicker.path", "org/apache/tapestry5/datepicker_106");
        configuration.add("tapestry.datepicker", "classpath:${tapestry.datepicker.path}");

        configuration.add(PersistentFieldManagerImpl.META_KEY, PersistentFieldManagerImpl.DEFAULT_STRATEGY);

        configuration.add(MetaDataConstants.RESPONSE_CONTENT_TYPE, "text/html");

        configuration.add(SymbolConstants.CHARSET, "UTF-8");

        configuration.add(SymbolConstants.APPLICATION_CATALOG,
                          "WEB-INF/${" + InternalConstants.TAPESTRY_APP_NAME_SYMBOL + "}.properties");

        configuration.add(SymbolConstants.EXCEPTION_REPORT_PAGE, "ExceptionReport");

        configuration.add(SymbolConstants.SCRIPTS_AT_TOP, "false");
    }


    /**
     * Adds content types for "css" and "js" file extensions. <dl> <dt>css</dt> <dd>test/css</dd> <dt>js</dt>
     * <dd>text/javascript</dd> </dl>
     */
    public void contributeResourceStreamer(MappedConfiguration<String, String> configuration)
    {
        configuration.add("css", "text/css");
        configuration.add("js", "text/javascript");
    }

    /**
     * Adds a listener to the {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSource} that clears the
     * {@link PropertyAccess} and {@link TypeCoercer} caches on a class loader invalidation.  In addition, forces the
     * realization of {@link ComponentClassResolver} at startup.
     */
    public void contributeApplicationInitializer(OrderedConfiguration<ApplicationInitializerFilter> configuration,
                                                 final TypeCoercer typeCoercer,
                                                 final ComponentClassResolver componentClassResolver)
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
                // Snuck in here is the logic to clear the PropertyAccess service's cache whenever
                // the component class loader is invalidated.

                componentInstantiatorSource.addInvalidationListener(listener);

                initializer.initializeApplication(context);

                // We don't care about the result, but this forces a load of the service
                // at application startup, rather than on first request.

                componentClassResolver.isPageName("ForceLoadAtStartup");
            }
        };

        configuration.add("ClearCachesOnInvalidation", clearCaches);
    }

    public void contributePropBindingFactory(OrderedConfiguration<BindingFactory> configuration)
    {
        BindingFactory keywordFactory = new BindingFactory()
        {
            private final Map<String, Object> keywords = CollectionFactory.newCaseInsensitiveMap();

            {
                keywords.put("true", Boolean.TRUE);
                keywords.put("false", Boolean.FALSE);
                keywords.put("null", null);
            }

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                String key = expression.trim();

                if (keywords.containsKey(key)) return new LiteralBinding(description, keywords.get(key), location);

                return null;
            }
        };

        BindingFactory thisFactory = new BindingFactory()
        {

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                if ("this".equalsIgnoreCase(expression.trim()))
                    return new LiteralBinding(description, container.getComponent(), location);

                return null;
            }
        };

        BindingFactory longFactory = new BindingFactory()
        {
            private final Pattern pattern = Pattern.compile("^\\s*(-?\\d+)\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = pattern.matcher(expression);

                if (matcher.matches())
                {
                    String value = matcher.group(1);

                    return new LiteralBinding(description, new Long(value), location);
                }

                return null;
            }
        };

        BindingFactory intRangeFactory = new BindingFactory()
        {
            private final Pattern pattern = Pattern
                    .compile("^\\s*(-?\\d+)\\s*\\.\\.\\s*(-?\\d+)\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = pattern.matcher(expression);

                if (matcher.matches())
                {
                    int start = Integer.parseInt(matcher.group(1));
                    int finish = Integer.parseInt(matcher.group(2));

                    IntegerRange range = new IntegerRange(start, finish);

                    return new LiteralBinding(description, range, location);
                }

                return null;
            }
        };

        BindingFactory doubleFactory = new BindingFactory()
        {
            // So, either 1234. or 1234.56 or .78
            private final Pattern pattern = Pattern
                    .compile("^\\s*(\\-?((\\d+\\.)|(\\d*\\.\\d+)))\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = pattern.matcher(expression);

                if (matcher.matches())
                {
                    String value = matcher.group(1);

                    return new LiteralBinding(description, new Double(value), location);
                }

                return null;
            }
        };

        BindingFactory stringFactory = new BindingFactory()
        {
            // This will match embedded single quotes as-is, no escaping necessary.

            private final Pattern pattern = Pattern.compile("^\\s*'(.*)'\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = pattern.matcher(expression);

                if (matcher.matches())
                {
                    String value = matcher.group(1);

                    return new LiteralBinding(description, value, location);
                }

                return null;
            }
        };

        // To be honest, order probably doesn't matter.

        configuration.add("Keyword", keywordFactory);
        configuration.add("This", thisFactory);
        configuration.add("Long", longFactory);
        configuration.add("IntRange", intRangeFactory);
        configuration.add("Double", doubleFactory);
        configuration.add("StringLiteral", stringFactory);
    }

    /**
     * Contributes filters: <dl> <dt>Ajax</dt> <dd>Determines if the request is Ajax oriented, and redirects to an
     * alternative handler if so</dd> <dt>ImmediateRender</dt> <dd>When {@linkplain
     * SymbolConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS immediate action response rendering} is enabled, generates
     * the markup response (instead of a page redirect response, which is the normal behavior) </dd> <dt>Secure</dt>
     * <dd>Sends a redirect if an non-secure request accesses a secure page</dd></dl>
     */
    public void contributeComponentEventRequestHandler(OrderedConfiguration<ComponentEventRequestFilter> configuration,
                                                       final RequestSecurityManager requestSecurityManager,
                                                       @Ajax ComponentEventRequestHandler ajaxHandler,
                                                       ObjectLocator locator)
    {
        ComponentEventRequestFilter secureFilter = new ComponentEventRequestFilter()
        {
            public void handle(ComponentEventRequestParameters parameters, ComponentEventRequestHandler handler)
                    throws IOException
            {
                if (requestSecurityManager.checkForInsecureRequest(parameters.getActivePageName())) return;

                handler.handle(parameters);
            }
        };

        configuration.add("Ajax", new AjaxFilter(request, ajaxHandler));

        configuration.add("ImmediateRender", locator.autobuild(ImmediateActionRenderResponseFilter.class));

        configuration.add("Secure", secureFilter, "before:Ajax");
    }


    /**
     * Contributes strategies accessible via the {@link NullFieldStrategySource} service.
     * <p/>
     * <dl> <dt>default</dt> <dd>Does nothing, nulls stay null.</dd> <dt>zero</dt> <dd>Null values are converted to
     * zero.</dd> </dl>
     */
    public static void contributeNullFieldStrategySource(MappedConfiguration<String, NullFieldStrategy> configuration)
    {
        configuration.add("default", new DefaultNullFieldStrategy());
        configuration.add("zero", new ZeroNullFieldStrategy());
    }


    /**
     * Determines positioning of hidden fields relative to other elements (this is needed by {@link
     * org.apache.tapestry5.corelib.components.FormFragment} and others.
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

    @Scope(ScopeConstants.PERTHREAD)
    public Environment buildEnvironment(PerthreadManager perthreadManager)
    {
        EnvironmentImpl service = new EnvironmentImpl();

        perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    /**
     * Contributes the default set of AssetPathAuthorizers into the AssetProtectionDispatcher.
     * @param whitelist authorization based on explicit whitelisting.
     * @param regex authorization based on pattern matching.
     * @param conf
     */
    public static void contributeAssetProtectionDispatcher(
            @InjectService("WhitelistAuthorizer") AssetPathAuthorizer whitelist,
            @InjectService("RegexAuthorizer") AssetPathAuthorizer regex,
            OrderedConfiguration<AssetPathAuthorizer> conf)
    {
        //putting whitelist after everything ensures that, in fact, nothing falls through.
        //also ensures that whitelist gives other authorizers the chance to act...
        conf.add("regex",regex,"before:whitelist");
        conf.add("whitelist", whitelist,"after:*");
    }

    public void contributeRegexAuthorizer(Configuration<String> regex,
                @Symbol("tapestry.scriptaculous.path") String scriptPath,
                @Symbol("tapestry.datepicker.path") String datepickerPath)
    {
        //allow any js, jpg, jpeg, png, or css under org/apache/tapstry5. The funky bit of ([^/.]+/)* is what allows
        //multiple paths, while not allowing any of those paths to contains ./ or ../ thereby preventing paths like:
        //org/apache/tapestry5/../../../foo.js
        String pathPattern = "([^/.]+/)*[^/.]+\\.((css)|(js)|(jpg)|(jpeg)|(png)|(gif))$";

        regex.add("^org/apache/tapestry5/" + pathPattern);

        regex.add(datepickerPath + "/" + pathPattern);
        regex.add(scriptPath + "/" + pathPattern);
    }

}
