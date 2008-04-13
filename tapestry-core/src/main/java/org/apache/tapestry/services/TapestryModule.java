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

package org.apache.tapestry.services;

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.*;
import org.apache.tapestry.beaneditor.Validate;
import org.apache.tapestry.corelib.data.BlankOption;
import org.apache.tapestry.corelib.data.GridPagerPosition;
import org.apache.tapestry.corelib.data.InsertPosition;
import org.apache.tapestry.grid.GridDataSource;
import org.apache.tapestry.internal.*;
import org.apache.tapestry.internal.beaneditor.PrimitiveFieldConstraintGenerator;
import org.apache.tapestry.internal.beaneditor.ValidateAnnotationConstraintGenerator;
import org.apache.tapestry.internal.bindings.*;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.internal.grid.CollectionGridDataSource;
import org.apache.tapestry.internal.grid.NullDataSource;
import org.apache.tapestry.internal.renderers.*;
import org.apache.tapestry.internal.services.*;
import org.apache.tapestry.internal.structure.PageResourcesSource;
import org.apache.tapestry.internal.structure.PageResourcesSourceImpl;
import org.apache.tapestry.internal.transform.*;
import org.apache.tapestry.internal.translator.*;
import org.apache.tapestry.internal.util.IntegerRange;
import org.apache.tapestry.ioc.*;
import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import org.apache.tapestry.ioc.annotations.*;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.apache.tapestry.ioc.services.*;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.ioc.util.TimeInterval;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentResourcesAware;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.util.StringToEnumCoercion;
import org.apache.tapestry.validator.*;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The root module for Tapestry.
 */
@Marker(Core.class)
public final class TapestryModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ClasspathAssetAliasManager.class, ClasspathAssetAliasManagerImpl.class);
        binder.bind(PersistentLocale.class, PersistentLocaleImpl.class);
        binder.bind(ApplicationStateManager.class, ApplicationStateManagerImpl.class);
        binder.bind(ApplicationStatePersistenceStrategySource.class,
                    ApplicationStatePersistenceStrategySourceImpl.class);
        binder.bind(BindingSource.class, BindingSourceImpl.class);
        binder.bind(PersistentFieldManager.class, PersistentFieldManagerImpl.class);
        binder.bind(FieldValidatorSource.class, FieldValidatorSourceImpl.class);
        binder.bind(ApplicationGlobals.class, ApplicationGlobalsImpl.class);
        binder.bind(AssetSource.class, AssetSourceImpl.class);
        binder.bind(Cookies.class, CookiesImpl.class);
        binder.bind(Environment.class, EnvironmentImpl.class);
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
        binder.bind(TemplateParser.class, TemplateParserImpl.class);
        binder.bind(PageResponseRenderer.class, PageResponseRendererImpl.class);
        binder.bind(PageMarkupRenderer.class, PageMarkupRendererImpl.class);
        binder.bind(ComponentInvocationMap.class, NoOpComponentInvocationMap.class);
        binder.bind(ObjectRenderer.class, LocationRenderer.class).withId("LocationRenderer");
        binder.bind(UpdateListenerHub.class, UpdateListenerHubImpl.class);
        binder.bind(ObjectProvider.class, AssetObjectProvider.class).withId("AssetObjectProvider");
        binder.bind(LinkFactory.class, LinkFactoryImpl.class);
        binder.bind(LocalizationSetter.class, LocalizationSetterImpl.class);
        binder.bind(PageElementFactory.class, PageElementFactoryImpl.class);
        binder.bind(RequestExceptionHandler.class, DefaultRequestExceptionHandler.class);
        binder.bind(ResourceStreamer.class, ResourceStreamerImpl.class);
        binder.bind(ClientPersistentFieldStorage.class, ClientPersistentFieldStorageImpl.class);
        binder.bind(RequestEncodingInitializer.class, RequestEncodingInitializerImpl.class);
        binder.bind(ComponentEventResultProcessor.class, ComponentInstanceResultProcessor.class).withId(
                "ComponentInstanceResultProcessor");
        binder.bind(PageRenderQueue.class, PageRenderQueueImpl.class);
        binder.bind(AjaxPartialResponseRenderer.class, AjaxPartialResponseRendererImpl.class);
        binder.bind(PageContentTypeAnalyzer.class, PageContentTypeAnalyzerImpl.class);
        binder.bind(ResponseRenderer.class, ResponseRendererImpl.class);
        binder.bind(RequestPathOptimizer.class, RequestPathOptimizerImpl.class);
        binder.bind(NullFieldStrategySource.class, NullFieldStrategySourceImpl.class);
        binder.bind(HttpServletRequestFilter.class, IgnoredPathsFilter.class).withId("IgnoredPathsFilter");
        binder.bind(PageResourcesSource.class, PageResourcesSourceImpl.class);
        binder.bind(ContextValueEncoder.class, ContextValueEncoderImpl.class);
        binder.bind(BaseURLSource.class, BaseURLSourceImpl.class);
        binder.bind(RequestSecurityManager.class, RequestSecurityManagerImpl.class);
        binder.bind(BeanBlockOverrideSource.class, BeanBlockOverrideSourceImpl.class);
        binder.bind(InternalRequestGlobals.class, InternalRequestGlobalsImpl.class);
    }

    public static Alias build(Logger logger,

                              @Inject @Symbol(InternalConstants.TAPESTRY_ALIAS_MODE_SYMBOL)
                              String mode,

                              @InjectService("AliasOverrides")
                              AliasManager overridesManager,

                              Collection<AliasContribution> configuration)
    {
        AliasManager manager = new AliasManagerImpl(logger, configuration);

        return new AliasImpl(manager, mode, overridesManager);
    }

    /**
     * A companion service to {@linkplain #build(org.slf4j.Logger, String, AliasManager, java.util.Collection)}  the
     * Alias service} whose configuration contribution define spot overrides to specific services.
     */
    public static AliasManager buildAliasOverrides(Logger logger, Collection<AliasContribution> configuration)
    {
        return new AliasManagerImpl(logger, configuration);
    }

    /**
     * Contributes the factory for serveral built-in binding prefixes ("asset", "block", "component", "literal", prop",
     * "nullfieldstrategy", "message", "validate", "translate", "var").
     */
    public static void contributeBindingSource(MappedConfiguration<String, BindingFactory> configuration,

                                               @InjectService("PropBindingFactory")
                                               BindingFactory propBindingFactory,

                                               ObjectLocator locator)
    {
        configuration.add(TapestryConstants.LITERAL_BINDING_PREFIX, new LiteralBindingFactory());
        configuration.add(TapestryConstants.PROP_BINDING_PREFIX, propBindingFactory);

        configuration.add("component", new ComponentBindingFactory());
        configuration.add("message", new MessageBindingFactory());
        configuration.add("validate", locator.autobuild(ValidateBindingFactory.class));
        configuration.add("translate", locator.autobuild(TranslateBindingFactory.class));
        configuration.add("block", new BlockBindingFactory());
        configuration.add("asset", locator.autobuild(AssetBindingFactory.class));
        configuration.add("var", new RenderVariableBindingFactory());
        configuration.add("nullfieldstrategy", locator.autobuild(NullFieldStrategyBindingFactory.class));
    }

    public static void contributeClasspathAssetAliasManager(MappedConfiguration<String, String> configuration,

                                                            // @Inject not needed, because this isn't a service builder method
                                                            @Symbol("tapestry.scriptaculous.path")
                                                            String scriptaculousPath,

                                                            @Symbol("tapestry.datepicker.path")
                                                            String datepickerPath)
    {
        configuration.add("tapestry", "org/apache/tapestry");

        configuration.add("scriptaculous", scriptaculousPath);

        configuration.add("datepicker", datepickerPath);
    }

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("core", "org.apache.tapestry.corelib"));
    }

    /**
     * Adds a number of standard component class transform workers: <dl> <dt>Retain </dt> <dd>Allows fields to retain
     * their values between requests</dd> <dt>Persist </dt> <dd>Allows fields to store their their value persistently
     * between requests</dd> <dt>Parameter </dt> <dd>Identifies parameters based on the {@link
     * org.apache.tapestry.annotations.Parameter} annotation</dd> <dt>Component </dt> <dd>Defines embedded components
     * based on the {@link org.apache.tapestry.annotations.Component} annotation</dd> <dt>Mixin </dt> <dd>Adds a mixin
     * as part of a component's implementation</dd> <dt>Environment </dt> <dd>Allows fields to contain values extracted
     * from the {@link org.apache.tapestry.services.Environment} service</dd> <dt>Inject </dt> <dd>Used with the {@link
     * org.apache.tapestry.ioc.annotations.Inject} annotation, when a value is supplied</dd> <dt>InjectPage</dt>
     * <dd>Adds code to allow access to other pages via the {@link org.apache.tapestry.annotations.InjectPage} field
     * annotation</dd> <dt>InjectBlock </dt> <dd>Allows a block from the template to be injected into a field</dd>
     * <dt>IncludeStylesheet </dt> <dd>Supports the {@link org.apache.tapestry.annotations.IncludeStylesheet}
     * annotation</dd> <dt>IncludeJavaScriptLibrary </dt> <dd>Supports the {@link org.apache.tapestry.annotations.IncludeJavaScriptLibrary}
     * annotation</dd> <dt>SupportsInformalParameters </dt> <dd>Checks for the annotation</dd> <dt>Meta </dt> <dd>Checks
     * for meta data and adds it to the component model</dd> <dt>ApplicationState </dt> <dd>Converts fields that
     * reference application state objects <dt>UnclaimedField </dt> <dd>Identifies unclaimed fields and resets them to
     * null/0/false at the end of the request</dd> <dt>RenderCommand </dt> <dd>Ensures all components also implement
     * {@link org.apache.tapestry.runtime.RenderCommand}</dd> <dt>SetupRender, BeginRender, etc. </dt> <dd>Correspond to
     * component render phases and annotations</dd> <dt>InvokePostRenderCleanupOnResources </dt> <dd>Makes sure {@link
     * org.apache.tapestry.internal.InternalComponentResources#postRenderCleanup()} is invoked after a component
     * finishes rendering</dd> <dt>Secure</dt> <dd>Checks for the {@link org.apache.tapestry.annotations.Secure}
     * annotation</dd> <dt>ContentType</dt> <dd>Checks for {@link org.apache.tapestry.annotations.ContentType}
     * annotation</dd> <dt>ResponseEncoding</dt> <dd>Checks for the {@link org.apache.tapestry.annotations.ResponseEncoding}
     * annotation</dd> <dt>GenerateAccessors</dt> <dd>Generates accessor methods if {@link
     * org.apache.tapestry.annotations.Property} annotation is present </dd> <dt>Cached</dt> <dd>Checks for the {@link
     * Cached} annotation</dd></dl>
     */
    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration,

            ObjectLocator locator,

            InjectionProvider injectionProvider,

            Environment environment,

            ComponentClassResolver resolver,

            ComponentSource componentSource,

            BindingSource bindingsource)
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
        configuration.add("InjectPage", new InjectPageWorker(componentSource, resolver));
        configuration.add("InjectContainer", new InjectContainerWorker());
        configuration.add("InjectComponent", new InjectComponentWorker());
        configuration.add("RenderCommand", new RenderCommandWorker());

        // Default values for parameters are often some form of injection, so make sure
        // that Parameter fields are processed after injections.

        configuration.add("Parameter", new ParameterWorker(bindingsource), "after:Inject*");

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
        configuration.add("ResponseEncoding", new ResponseEncodingWorker());

        configuration.add("Property", new PropertyWorker());

        // These must come after Property, since they actually delete fields that may still have the annotation
        configuration.add("ApplicationState", locator.autobuild(ApplicationStateWorker.class),
                          "after:Property");
        configuration.add("Environment", new EnvironmentalWorker(environment), "after:Property");

        // This one is always last. Any additional private fields that aren't annotated will
        // be converted to clear out at the end of the request.

        configuration.add("UnclaimedField", new UnclaimedFieldWorker(), "after:*");
    }

    /**
     * <dl> <dt>Annotation</dt> <dd>Checks for {@link org.apache.tapestry.beaneditor.DataType} annotation</dd>
     * <dt>Default  (ordered last)</dt> <dd>{@link org.apache.tapestry.internal.services.DefaultDataTypeAnalyzer}
     * service ({@link #contributeDefaultDataTypeAnalyzer(org.apache.tapestry.ioc.MappedConfiguration)} })</dd> </dl>
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

                                                      @InjectService("Alias")
                                                      final Alias alias,

                                                      @InjectService("AssetObjectProvider")
                                                      ObjectProvider assetObjectProvider)
    {
        // There's a nasty web of dependencies related to Alias; this wrapper class lets us
        // defer instantiating the Alias service implementation just long enough to defuse those
        // dependencies.

        ObjectProvider wrapper = new ObjectProvider()
        {
            public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
            {
                return alias.getObjectProvider().provide(objectType, annotationProvider, locator);
            }
        };

        configuration.add("Alias", wrapper, "after:Value");

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
     * cached data has changed (see {@link org.apache.tapestry.internal.services.CheckForUpdatesFilter}).
     * <dt>ErrorFilter</dt> <dd>Catches request errors and lets the {@link org.apache.tapestry.services.RequestExceptionHandler}
     * handle them</dd> <dt>Localization</dt> <dd>Determines the locale for the current request from header data or
     * cookies in the request</dd> <dt>StoreIntoGlobals</dt> <dd>Stores the request and response into the {@link
     * org.apache.tapestry.services.RequestGlobals} service (this is repeated at the end of the pipeline, in case any
     * filter substitutes the request or response). </dl>
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration, Context context,

                                         final RequestExceptionHandler exceptionHandler,

                                         // @Inject not needed because its a long, not a String
                                         @Symbol(TapestryConstants.FILE_CHECK_INTERVAL_SYMBOL)
                                         @IntermediateType(TimeInterval.class)
                                         long checkInterval,

                                         @Symbol(TapestryConstants.FILE_CHECK_UPDATE_TIMEOUT_SYMBOL)
                                         @IntermediateType(TimeInterval.class)
                                         long updateTimeout,

                                         final InternalRequestGlobals requestGlobals,

                                         LocalizationSetter localizationSetter)
    {
        RequestFilter staticFilesFilter = new StaticFilesFilter(context);

        RequestFilter errorFilter = new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler) throws IOException
            {
                try
                {
                    return handler.service(request, response);
                }
                catch (IOException ex)
                {
                    // Pass it through.
                    throw ex;
                }
                catch (Throwable ex)
                {
                    // Most of the time, we've got exception linked up the kazoo ... but when ClassLoaders
                    // get involved, things go screwy.  Exceptions when transforming classes can cause
                    // a NoClassDefFoundError with no cause; here we're trying to link the cause back in.
                    // TAPESTRY-2078

                    if (ex.getCause() == null)
                    {
                        Throwable cause = requestGlobals.getClassLoaderException();

                        if (cause != null)
                        {

                            try
                            {
                                ex.initCause(cause);
                            }
                            catch (IllegalStateException ise)
                            {
                                // TAPESTRY-2284: sometimes you just can't init the cause, and there's no way to
                                // find out without trying.
                            }
                        }
                    }

                    exceptionHandler.handleRequestException(ex);

                    // We assume a reponse has been sent and there's no need to handle the request
                    // further.

                    return true;
                }
            }
        };

        RequestFilter storeIntoGlobals = new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler) throws IOException
            {
                _requestGlobals.storeRequestResponse(request, response);

                return handler.service(request, response);
            }
        };

        configuration.add("CheckForUpdates",
                          new CheckForUpdatesFilter(_updateListenerHub, checkInterval, updateTimeout), "before:*");

        configuration.add("StaticFiles", staticFilesFilter);

        configuration.add("ErrorFilter", errorFilter);

        configuration.add("StoreIntoGlobals", storeIntoGlobals);

        configuration.add("Localization", new LocalizationFilter(localizationSetter), "after:ErrorFilter");
    }

    /**
     * Contributes the basic set of named translators: <ul>  <li>string</li>  <li>byte</li> <li>integer</li>
     * <li>long</li> <li>float</li> <li>double</li> </ul>
     */
    public static void contributeTranslatorSource(MappedConfiguration<String, Translator> configuration)
    {

        configuration.add("string", new StringTranslator());
        configuration.add("byte", new ByteTranslator());
        configuration.add("integer", new IntegerTranslator());
        configuration.add("long", new LongTranslator());
        configuration.add("float", new FloatTranslator());
        configuration.add("double", new DoubleTranslator());
    }

    /**
     * Adds coercions: <ul> <li>String to {@link org.apache.tapestry.SelectModel} <li>String to {@link
     * org.apache.tapestry.corelib.data.InsertPosition} <li>Map to {@link org.apache.tapestry.SelectModel}
     * <li>Collection to {@link GridDataSource} <li>null to {@link org.apache.tapestry.grid.GridDataSource} <li>String
     * to {@link org.apache.tapestry.corelib.data.GridPagerPosition} <li>List to {@link org.apache.tapestry.SelectModel}
     * <li>{@link org.apache.tapestry.runtime.ComponentResourcesAware} (typically, a component) to {@link
     * org.apache.tapestry.ComponentResources} <li>String to {@link org.apache.tapestry.corelib.data.BlankOption} </ul>
     */
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
    {
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
            private final GridDataSource _source = new NullDataSource();

            public GridDataSource coerce(Void input)
            {
                return _source;
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

        configuration.add(name, new ComponentLifecycleMethodWorker(signature, annotationClass, reverse));
    }

    private final PipelineBuilder _pipelineBuilder;

    private final ApplicationGlobals _applicationGlobals;

    private final PropertyShadowBuilder _shadowBuilder;

    private final RequestPageCache _requestPageCache;

    private final Environment _environment;

    private final StrategyBuilder _strategyBuilder;

    private final PropertyAccess _propertyAccess;

    private final ClassFactory _componentClassFactory;

    private final ComponentInstantiatorSource _componentInstantiatorSource;

    private final ComponentTemplateSource _componentTemplateSource;

    private final UpdateListenerHub _updateListenerHub;

    private final PerthreadManager _perthreadManager;

    private final ChainBuilder _chainBuilder;

    private final Request _request;

    private final Response _response;

    private final ThreadLocale _threadLocale;

    private final RequestGlobals _requestGlobals;

    private final ActionRenderResponseGenerator _actionRenderResponseGenerator;

    public TapestryModule(PipelineBuilder pipelineBuilder,

                          PropertyShadowBuilder shadowBuilder,

                          RequestGlobals requestGlobals,

                          ApplicationGlobals applicationGlobals,

                          ChainBuilder chainBuilder,

                          RequestPageCache requestPageCache,

                          Environment environment,

                          StrategyBuilder strategyBuilder,

                          ComponentInstantiatorSource componentInstantiatorSource,

                          PropertyAccess propertyAccess,

                          @ComponentLayer ClassFactory componentClassFactory,

                          UpdateListenerHub updateListenerHub,

                          PerthreadManager perthreadManager,

                          ComponentTemplateSource componentTemplateSource,

                          Request request,

                          Response response,

                          ThreadLocale threadLocale,

                          ActionRenderResponseGenerator actionRenderResponseGenerator)
    {
        _pipelineBuilder = pipelineBuilder;
        _shadowBuilder = shadowBuilder;
        _requestGlobals = requestGlobals;
        _applicationGlobals = applicationGlobals;
        _chainBuilder = chainBuilder;
        _requestPageCache = requestPageCache;
        _environment = environment;
        _strategyBuilder = strategyBuilder;
        _componentInstantiatorSource = componentInstantiatorSource;
        _propertyAccess = propertyAccess;
        _componentClassFactory = componentClassFactory;

        _updateListenerHub = updateListenerHub;
        _perthreadManager = perthreadManager;
        _componentTemplateSource = componentTemplateSource;
        _request = request;
        _response = response;
        _threadLocale = threadLocale;
        _actionRenderResponseGenerator = actionRenderResponseGenerator;
    }

    public Context build(ApplicationGlobals globals)
    {
        return _shadowBuilder.build(globals, "context", Context.class);
    }

    public ComponentClassResolver buildComponentClassResolver(ServiceResources resources)
    {
        ComponentClassResolverImpl service = resources.autobuild(ComponentClassResolverImpl.class);

        // Allow the resolver to clean its cache when the source is invalidated

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Builds the source of {@link Messages} containing validation messages. The contributions are paths to message
     * bundles (resource paths within the classpath); the default contribution is "org/apache/tapestry/internal/ValidationMessages".
     */
    public ValidationMessagesSource build(Collection<String> configuration, UpdateListenerHub updateListenerHub,

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

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry.PageRenderSupport} inside this thread's {@link
     * Environment}.
     */
    public PageRenderSupport buildPageRenderSupport(EnvironmentalShadowBuilder builder)
    {
        return builder.build(PageRenderSupport.class);
    }

    /**
     * Builds a proxy to the current {@link org.apache.tapestry.services.FormSupport} inside this thread's {@link
     * org.apache.tapestry.services.Environment}.
     */
    public FormSupport buildFormSupport(EnvironmentalShadowBuilder builder)
    {
        return builder.build(FormSupport.class);
    }

    /**
     * Allows the exact steps in the component class transformation process to be defined.
     */
    public ComponentClassTransformWorker build(List<ComponentClassTransformWorker> configuration)
    {
        return _chainBuilder.build(ComponentClassTransformWorker.class, configuration);
    }

    /**
     * Analyzes properties to determine the data types, used to {@linkplain #contributeBeanBlockSource(org.apache.tapestry.ioc.Configuration)}
     * locale display and edit blocks} for properties.  The default behaviors look for a {@link
     * org.apache.tapestry.beaneditor.DataType} annotation before deriving the data type from the property type.
     */
    @Marker(Primary.class)
    public DataTypeAnalyzer build(List<DataTypeAnalyzer> configuration)
    {
        return _chainBuilder.build(DataTypeAnalyzer.class, configuration);
    }

    /**
     * A chain of command for providing values for {@link Inject}-ed fields in component classes. The service's
     * configuration can be extended to allow for different automatic injections (based on some combination of field
     * type and field name).
     */

    public InjectionProvider build(List<InjectionProvider> configuration)
    {
        return _chainBuilder.build(InjectionProvider.class, configuration);
    }


    /**
     * Initializes the application.
     */
    @Marker(Primary.class)
    public ApplicationInitializer build(Logger logger, List<ApplicationInitializerFilter> configuration)
    {
        ApplicationInitializer terminator = new ApplicationInitializer()
        {
            public void initializeApplication(Context context)
            {
                _applicationGlobals.storeContext(context);
            }
        };

        return _pipelineBuilder.build(logger, ApplicationInitializer.class, ApplicationInitializerFilter.class,
                                      configuration, terminator);
    }

    public HttpServletRequestHandler build(Logger logger, List<HttpServletRequestFilter> configuration,

                                           @Primary
                                           final RequestHandler handler)
    {
        HttpServletRequestHandler terminator = new HttpServletRequestHandler()
        {
            public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
                    throws IOException
            {
                _requestGlobals.storeServletRequestResponse(servletRequest, servletResponse);

                Request request = new RequestImpl(servletRequest);
                Response response = new ResponseImpl(servletResponse);

                // Transition from the Servlet API-based pipeline, to the Tapestry-based pipeline.

                return handler.service(request, response);
            }
        };

        return _pipelineBuilder.build(logger, HttpServletRequestHandler.class, HttpServletRequestFilter.class,
                                      configuration, terminator);
    }

    @Marker(Primary.class)
    public RequestHandler build(Logger logger, List<RequestFilter> configuration,

                                @Primary
                                final Dispatcher masterDispatcher)
    {
        RequestHandler terminator = new RequestHandler()
        {
            public boolean service(Request request, Response response) throws IOException
            {
                _requestGlobals.storeRequestResponse(request, response);

                return masterDispatcher.dispatch(request, response);
            }
        };

        return _pipelineBuilder.build(logger, RequestHandler.class, RequestFilter.class, configuration, terminator);
    }

    public ServletApplicationInitializer build(Logger logger, List<ServletApplicationInitializerFilter> configuration,

                                               @Primary
                                               final ApplicationInitializer initializer)
    {
        ServletApplicationInitializer terminator = new ServletApplicationInitializer()
        {
            public void initializeApplication(ServletContext context)
            {
                _applicationGlobals.storeServletContext(context);

                // And now, down the (Web) ApplicationInitializer pipeline ...

                initializer.initializeApplication(new ContextImpl(context));
            }
        };

        return _pipelineBuilder.build(logger, ServletApplicationInitializer.class,
                                      ServletApplicationInitializerFilter.class, configuration, terminator);
    }

    /**
     * The component event result processor used for normal component requests.
     */
    @Marker({ Primary.class, Traditional.class })
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

        return _strategyBuilder.build(registry);
    }

    /**
     * The default data type analyzer is the final analyzer consulted and identifies the type entirely pased on the
     * property type, working against its own configuration (mapping property type class to data type).
     */
    public DataTypeAnalyzer buildDefaultDataTypeAnalyzer(ServiceResources resources)
    {
        DefaultDataTypeAnalyzer service = resources.autobuild(DefaultDataTypeAnalyzer.class);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    public TranslatorSource buildTranslatorSource(ServiceResources resources)
    {
        TranslatorSourceImpl service = resources
                .autobuild(TranslatorSourceImpl.class);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    @Marker(Primary.class)
    public ObjectRenderer build(Map<Class, ObjectRenderer> configuration)
    {
        StrategyRegistry<ObjectRenderer> registry = StrategyRegistry.newInstance(ObjectRenderer.class, configuration);

        return _strategyBuilder.build(registry);
    }

    public static ComponentMessagesSource build(UpdateListenerHub updateListenerHub,

                                                @ContextProvider AssetFactory contextAssetFactory,

                                                @Inject @Value("WEB-INF/${tapestry.app-name}.properties")
                                                String appCatalog)
    {
        ComponentMessagesSourceImpl service = new ComponentMessagesSourceImpl(contextAssetFactory
                .getRootResource(), appCatalog);

        updateListenerHub.addUpdateListener(service);

        return service;
    }

    /**
     * Returns a {@link ClassFactory} that can be used to create extra classes around component classes. This
     * ClassFactory will be cleared whenever an underlying component class is discovered to have changed. Use of this
     * class factory implies that your code will become aware of this (if necessary) to discard any cached object (alas,
     * this currently involves dipping into the internals side to register for the correct notifications). Failure to
     * properly clean up can result in really nasty PermGen space memory leaks.
     */
    @Marker(ComponentLayer.class)
    public ClassFactory buildComponentClassFactory()
    {
        return _shadowBuilder.build(_componentInstantiatorSource, "classFactory", ClassFactory.class);
    }


    /**
     * Ordered contributions to the MasterDispatcher service allow different URL matching strategies to occur.
     */
    @Marker(Primary.class)
    public Dispatcher buildMasterDispatcher(List<Dispatcher> configuration)
    {
        return _chainBuilder.build(Dispatcher.class, configuration);
    }

    public PropertyConduitSource buildPropertyConduitSource()
    {
        PropertyConduitSourceImpl service = new PropertyConduitSourceImpl(_propertyAccess, _componentClassFactory);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Builds a shadow of the RequestGlobals.request property. Note again that the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Request buildRequest()
    {
        return _shadowBuilder.build(_requestGlobals, "request", Request.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.HTTPServletRequest property.  Generally, you should inject the {@link
     * Request} service instead, as future version of Tapestry may operate beyond just the servlet API.
     */
    public HttpServletRequest buildHttpServletRequest()
    {
        return _shadowBuilder.build(_requestGlobals, "HTTPServletRequest", HttpServletRequest.class);
    }

    /**
     * Builds a shadow of the RequestGlobals.response property. Note again that the shadow can be an ordinary singleton,
     * even though RequestGlobals is perthread.
     */
    public Response buildResponse()
    {
        return _shadowBuilder.build(_requestGlobals, "response", Response.class);
    }

    /**
     * Contributes the default "session" strategy.
     */
    public void contributeApplicationStatePersistenceStrategySource(
            MappedConfiguration<String, ApplicationStatePersistenceStrategy> configuration,

            Request request)
    {
        configuration.add("session", new SessionApplicationStatePersistenceStrategy(request));
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
     * a warning). A direct to the containing page is sent.</dd> <dt>{@link org.apache.tapestry.StreamResponse}</dt>
     * <dd>The stream response is sent as the actual reply.</dd> <dt>URL</dt> <dd>Sends a redirect to a (presumably)
     * external URL</dd> </dl>
     */
    public void contributeComponentEventResultProcessor(

            @InjectService("ComponentInstanceResultProcessor")
            ComponentEventResultProcessor componentInstanceProcessor,

            ComponentClassResolver componentClassResolver,

            MappedConfiguration<Class, ComponentEventResultProcessor> configuration)
    {
        configuration.add(Link.class, new ComponentEventResultProcessor<Link>()
        {
            public void processResultValue(Link value) throws IOException
            {
                _response.sendRedirect(value);
            }
        });

        configuration.add(URL.class, new ComponentEventResultProcessor<URL>()
        {
            public void processResultValue(URL value) throws IOException
            {
                _response.sendRedirect(value.toExternalForm());
            }
        });

        configuration.add(String.class, new StringResultProcessor(_requestPageCache, _actionRenderResponseGenerator));

        configuration.add(Class.class, new ClassResultProcessor(componentClassResolver, _requestPageCache,
                                                                _actionRenderResponseGenerator));

        configuration.add(Component.class, componentInstanceProcessor);

        configuration.add(StreamResponse.class, new StreamResponseResultProcessor(_response));
    }


    /**
     * Contributes handlers for the following types: <dl> <dt>Object</dt> <dd>Failure case, added to provide more useful
     * exception message</dd> <dt>{@link RenderCommand}</dt> <dd>Typically, a {@link org.apache.tapestry.Block}</dd>
     * <dt>{@link Component}</dt> <dd>Renders the component and its body</dd> <dt>{@link
     * org.apache.tapestry.json.JSONObject}</dt> <dd>The JSONObject is returned as a text/javascript response</dd>
     * <dt>{@link org.apache.tapestry.StreamResponse}</dt> <dd>The stream response is sent as the actual response</dd>
     * </dl>
     */

    public void contributeAjaxComponentEventResultProcessor(
            MappedConfiguration<Class, ComponentEventResultProcessor> configuration, ObjectLocator locator)
    {
        configuration.add(RenderCommand.class, locator.autobuild(RenderCommandComponentEventResultProcessor.class));
        configuration.add(Component.class, locator.autobuild(AjaxComponentInstanceEventResultProcessor.class));
        configuration.add(JSONObject.class, new JSONObjectEventResultProcessor(_response));
        configuration.add(StreamResponse.class, new StreamResponseResultProcessor(_response));
    }

    /**
     * The MasterDispatcher is a chain-of-command of individual Dispatchers, each handling (like a servlet) a particular
     * kind of incoming request. <dl> <dt>RootPath</dt> <dd>Renders the start page for the "/" request</dd>
     * <dt>Asset</dt> <dd>Provides access to classpath assets</dd> <dt>PageRender</dt> <dd>Identifies the {@link
     * org.apache.tapestry.services.PageRenderRequestParameters} and forwards onto {@link PageRenderRequestHandler}</dd>
     * <dt>ComponentEvent</dt> <dd>Identifies the {@link ComponentEventRequestParameters} and forwards onto the {@link
     * ComponentEventRequestHandler}</dd> </dl>
     */
    public void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration,
                                           ObjectLocator locator)
    {
        // Looks for the root path and renders the start page. This is maintained for compatibility
        // with earlier versions of Tapestry 5, it is recommended that an Index page be used instead.

        configuration.add("RootPath",
                          locator.autobuild(RootPathDispatcher.class),
                          "before:Asset");

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
     * org.apache.tapestry.services.Request}, {@link org.apache.tapestry.ioc.Location}, {@link
     * org.apache.tapestry.ComponentResources}, {@link org.apache.tapestry.EventContext}, List, and Object[].
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

        configuration.add(Request.class, new RequestRenderer());

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
     * The MarkupRenderer service is used to render a full page as markup.  Supports an ordered configuration of {@link
     * org.apache.tapestry.services.MarkupRendererFilter}s.
     *
     * @param pageRenderQueue handles the bulk of the work
     * @param logger          used to log errors building the pipeline
     * @param configuration   filters on this service
     * @return the service
     * @see #contributeMarkupRenderer(org.apache.tapestry.ioc.OrderedConfiguration, org.apache.tapestry.Asset,
     *      org.apache.tapestry.Asset, ValidationMessagesSource, org.apache.tapestry.ioc.services.SymbolSource,
     *      AssetSource)
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

        return _pipelineBuilder.build(logger, MarkupRenderer.class, MarkupRendererFilter.class, configuration,
                                      terminator);
    }


    /**
     * Adds page render filters, each of which provides an {@link org.apache.tapestry.annotations.Environmental}
     * service.  Filters often provide {@link Environmental} services needed by components as they render. <dl>
     * <dt>PageRenderSupport</dt>  <dd>Provides {@link PageRenderSupport}</dd> <dt>ClientBehaviorSupport</dt>
     * <dd>Provides {@link org.apache.tapestry.internal.services.ClientBehaviorSupport}</dd> <dt>Heartbeat</dt>
     * <dd>Provides {@link org.apache.tapestry.services.Heartbeat}</dd> <dt>DefaultValidationDecorator</dt> <dd>Provides
     * {@link org.apache.tapestry.ValidationDecorator} (as an instance of {@link org.apache.tapestry.internal.DefaultValidationDecorator})</dd>
     * </dl>
     */
    public void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration,

                                         @Path("${tapestry.default-stylesheet}")
                                         final Asset stylesheetAsset,

                                         @Path("${tapestry.field-error-marker}")
                                         final Asset fieldErrorIcon,

                                         final ValidationMessagesSource validationMessagesSource,

                                         final SymbolSource symbolSource,

                                         final AssetSource assetSource)
    {
        MarkupRendererFilter pageRenderSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentHeadBuilder builder = new DocumentHeadBuilderImpl();

                PageRenderSupportImpl support = new PageRenderSupportImpl(builder, symbolSource, assetSource,

                                                                          // Core scripts added to any page that uses scripting

                                                                          "${tapestry.scriptaculous}/prototype.js",
                                                                          "${tapestry.scriptaculous}/scriptaculous.js",
                                                                          "${tapestry.scriptaculous}/effects.js",

                                                                          // Uses functions defined by the prior three

                                                                          "org/apache/tapestry/tapestry.js");

                support.addStylesheetLink(stylesheetAsset, null);

                _environment.push(PageRenderSupport.class, support);

                renderer.renderMarkup(writer);

                builder.updateDocument(writer.getDocument());

                _environment.pop(PageRenderSupport.class);
            }
        };

        MarkupRendererFilter clientBehaviorSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                PageRenderSupport pageRenderSupport = _environment.peekRequired(PageRenderSupport.class);

                ClientBehaviorSupportImpl clientBehaviorSupport = new ClientBehaviorSupportImpl(pageRenderSupport);

                _environment.push(ClientBehaviorSupport.class, clientBehaviorSupport);

                renderer.renderMarkup(writer);

                _environment.pop(ClientBehaviorSupport.class);

                clientBehaviorSupport.writeInitializationScript();
            }
        };

        MarkupRendererFilter heartbeat = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                Heartbeat heartbeat = new HeartbeatImpl();

                heartbeat.begin();

                _environment.push(Heartbeat.class, heartbeat);

                renderer.renderMarkup(writer);

                _environment.pop(Heartbeat.class);

                heartbeat.end();
            }
        };

        MarkupRendererFilter defaultValidationDecorator = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                Messages messages = validationMessagesSource.getValidationMessages(_threadLocale.getLocale());

                ValidationDecorator decorator = new DefaultValidationDecorator(_environment, messages, fieldErrorIcon,
                                                                               writer);

                _environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer);

                _environment.pop(ValidationDecorator.class);
            }
        };


        configuration.add("PageRenderSupport", pageRenderSupport);
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:PageRenderSupport");
        configuration.add("Heartbeat", heartbeat, "after:PageRenderSupport");
        configuration.add("DefaultValidationDecorator", defaultValidationDecorator, "after:Heartbeat");
    }


    /**
     * A wrapper around {@link org.apache.tapestry.internal.services.PageRenderQueue} used for partial page renders.
     * Supports an ordered configuration of {@link org.apache.tapestry.services.PartialMarkupRendererFilter}s.
     *
     * @param logger        used to log warnings creating the pipeline
     * @param configuration filters for the service
     * @param renderQueue   does most of the work
     * @return the service
     * @see #contributePartialMarkupRenderer(org.apache.tapestry.ioc.OrderedConfiguration, org.apache.tapestry.Asset,
     *      ValidationMessagesSource)
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

        return _pipelineBuilder.build(logger, PartialMarkupRenderer.class, PartialMarkupRendererFilter.class,
                                      configuration, terminator);
    }

    /**
     * Contributes {@link PartialMarkupRendererFilter}s used when rendering a partial Ajax response.  This is an analog
     * to {@link #contributeMarkupRenderer(org.apache.tapestry.ioc.OrderedConfiguration, org.apache.tapestry.Asset,
     * org.apache.tapestry.Asset, ValidationMessagesSource, org.apache.tapestry.ioc.services.SymbolSource, AssetSource)}
     * } and overlaps it to some degree. <dl> <dt>   PageRenderSupport     </dt> <dd>Provides {@link
     * org.apache.tapestry.PageRenderSupport}</dd> <dt>ClientBehaviorSupport</dt> <dd>Provides {@link
     * org.apache.tapestry.internal.services.ClientBehaviorSupport}</dd> <dt>Heartbeat</dt> <dd>Provides {@link
     * org.apache.tapestry.services.Heartbeat}</dd> <dt>DefaultValidationDecorator</dt> <dd>Provides {@link
     * org.apache.tapestry.ValidationDecorator} (as an instance of {@link org.apache.tapestry.internal.DefaultValidationDecorator})</dd>
     * </dl>
     */
    public void contributePartialMarkupRenderer(OrderedConfiguration<PartialMarkupRendererFilter> configuration,

                                                @Path("${tapestry.field-error-marker}")
                                                final Asset fieldErrorIcon,

                                                final ValidationMessagesSource validationMessagesSource)
    {
        PartialMarkupRendererFilter pageRenderSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                String uid = Long.toHexString(System.currentTimeMillis());

                String namespace = ":" + uid;

                PartialRenderPageRenderSupport support = new PartialRenderPageRenderSupport(
                        namespace);

                _environment.push(PageRenderSupport.class, support);

                renderer.renderMarkup(writer, reply);

                support.update(reply);

                _environment.pop(PageRenderSupport.class);
            }
        };

        PartialMarkupRendererFilter clientBehaviorSupport = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                PageRenderSupport pageRenderSupport = _environment.peekRequired(PageRenderSupport.class);

                ClientBehaviorSupportImpl support = new ClientBehaviorSupportImpl(pageRenderSupport);

                _environment.push(ClientBehaviorSupport.class, support);

                renderer.renderMarkup(writer, reply);

                _environment.pop(ClientBehaviorSupport.class);

                support.writeInitializationScript();
            }
        };

        PartialMarkupRendererFilter heartbeat = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                Heartbeat heartbeat = new HeartbeatImpl();

                heartbeat.begin();

                _environment.push(Heartbeat.class, heartbeat);

                renderer.renderMarkup(writer, reply);

                _environment.pop(Heartbeat.class);

                heartbeat.end();
            }
        };

        PartialMarkupRendererFilter defaultValidationDecorator = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                Messages messages = validationMessagesSource.getValidationMessages(_threadLocale.getLocale());

                ValidationDecorator decorator = new DefaultValidationDecorator(_environment, messages, fieldErrorIcon,
                                                                               writer);

                _environment.push(ValidationDecorator.class, decorator);

                renderer.renderMarkup(writer, reply);

                _environment.pop(ValidationDecorator.class);
            }
        };


        configuration.add("PageRenderSupport", pageRenderSupport);
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:PageRenderSupport");
        configuration.add("Heartbeat", heartbeat, "after:PageRenderSupport");
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
        configuration.add("flash", new FlashPersistentFieldStrategy(request));
        configuration.add("client", clientStrategy);
    }

    public void contributeValidationMessagesSource(Configuration<String> configuration)
    {
        configuration.add("org/apache/tapestry/internal/ValidationMessages");
    }

    public ValueEncoderSource build(Map<Class, ValueEncoderFactory> configuration)
    {
        ValueEncoderSourceImpl service = new ValueEncoderSourceImpl(configuration);

        _componentInstantiatorSource.addInvalidationListener(service);

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

    public PageRenderRequestHandler buildPageRenderRequestHandler(List<PageRenderRequestFilter> configuration,
                                                                  Logger logger, ServiceResources resources)
    {
        return _pipelineBuilder.build(logger, PageRenderRequestHandler.class, PageRenderRequestFilter.class,
                                      configuration, resources.autobuild(PageRenderRequestHandlerImpl.class));
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
     * Builds the component action request handler for traditional (non-Ajax) requests. These typically result in a
     * redirect to a Tapestry render URL.
     *
     * @see org.apache.tapestry.internal.services.ComponentEventRequestHandlerImpl
     */
    @Marker(Traditional.class)
    public ComponentEventRequestHandler buildComponentEventRequestHandler(
            List<ComponentEventRequestFilter> configuration, Logger logger, ServiceResources resources)
    {
        return _pipelineBuilder.build(logger, ComponentEventRequestHandler.class, ComponentEventRequestFilter.class,
                                      configuration, resources.autobuild(ComponentEventRequestHandlerImpl.class));
    }

    /**
     * Builds the action request handler for Ajax requests, based on {@link org.apache.tapestry.internal.services.AjaxComponentEventRequestHandler}.
     * Filters on the request handler are supported here as well.
     */
    @Marker(Ajax.class)
    public ComponentEventRequestHandler buildAjaxComponentEventRequestHandler(
            List<ComponentEventRequestFilter> configuration, Logger logger, ServiceResources resources)
    {
        return _pipelineBuilder.build(logger, ComponentEventRequestHandler.class, ComponentEventRequestFilter.class,
                                      configuration, resources.autobuild(AjaxComponentEventRequestHandler.class));
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
     * @see TapestryModule#contributeClasspathAssetAliasManager(MappedConfiguration, String, String)
     */
    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        // Remember this is request-to-request time, presumably it'll take the developer more than
        // one second to make a change, save it, and switch back to the browser.

        configuration.add(TapestryConstants.FILE_CHECK_INTERVAL_SYMBOL, "1 s");
        configuration.add(TapestryConstants.FILE_CHECK_UPDATE_TIMEOUT_SYMBOL, "50 ms");

        // This should be overridden for particular applications.
        configuration.add(TapestryConstants.SUPPORTED_LOCALES_SYMBOL, "en,it,zh_CN");

        configuration.add("tapestry.default-cookie-max-age", "7 d");

        configuration.add("tapestry.start-page-name", "start");

        configuration.add("tapestry.default-stylesheet", "org/apache/tapestry/default.css");
        configuration.add("tapestry.field-error-marker", "org/apache/tapestry/field-error-marker.gif");

        configuration.add("tapestry.page-pool.soft-limit", "5");
        configuration.add("tapestry.page-pool.soft-wait", "10 ms");
        configuration.add("tapestry.page-pool.hard-limit", "20");
        configuration.add("tapestry.page-pool.active-window", "10 m");

        configuration.add(TapestryConstants.SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS_SYMBOL, "false");

        configuration.add(TapestryConstants.FORCE_ABSOLUTE_URIS_SYMBOL, "false");

        configuration.add(TapestryConstants.PRODUCTION_MODE_SYMBOL, "true");

        configuration.add(TapestryConstants.COMPRESS_WHITESPACE_SYMBOL, "true");

        configuration.add(TapestryConstants.SECURE_PAGE, "false");

        // This is designed to make it easy to keep synchronized with script.aculo.ous. As we
        // support a new version, we create a new folder, and update the path entry. We can then
        // delete the old version folder (or keep it around). This should be more manageable than
        // overwriting the local copy with updates (it's too easy for files deleted between scriptaculous
        // releases to be accidentally left lying around). There's also a ClasspathAliasManager
        // contribution based on the path.

        configuration.add("tapestry.scriptaculous", "classpath:${tapestry.scriptaculous.path}");
        configuration.add("tapestry.scriptaculous.path", "org/apache/tapestry/scriptaculous_1_8");

        // Likewise for WebFX DatePicker, currently version 1.0.6

        configuration.add("tapestry.datepicker.path", "org/apache/tapestry/datepicker_106");
        configuration.add("tapestry.datepicker", "classpath:${tapestry.datepicker.path}");

        configuration.add(PersistentFieldManagerImpl.META_KEY, PersistentFieldManagerImpl.DEFAULT_STRATEGY);

        configuration.add(TapestryConstants.RESPONSE_CONTENT_TYPE, "text/html");
        configuration.add(TapestryConstants.RESPONSE_ENCODING, "UTF-8");
    }

    public PageTemplateLocator build(@ContextProvider AssetFactory contextAssetFactory,

                                     ComponentClassResolver componentClassResolver)
    {
        return new PageTemplateLocatorImpl(contextAssetFactory.getRootResource(), componentClassResolver);
    }

    public ComponentInstantiatorSource build(@Builtin ClassFactory classFactory,

                                             ComponentClassTransformer transformer,

                                             Logger logger,

                                             InternalRequestGlobals internalRequestGlobals)
    {
        ComponentInstantiatorSourceImpl source = new ComponentInstantiatorSourceImpl(logger, classFactory
                .getClassLoader(), transformer, internalRequestGlobals);

        _updateListenerHub.addUpdateListener(source);

        return source;
    }

    public ComponentClassTransformer buildComponentClassTransformer(ServiceResources resources)
    {
        ComponentClassTransformerImpl transformer = resources
                .autobuild(ComponentClassTransformerImpl.class);

        _componentInstantiatorSource.addInvalidationListener(transformer);

        return transformer;
    }

    public PagePool build(PageLoader pageLoader, ComponentMessagesSource componentMessagesSource,
                          ServiceResources resources)
    {
        PagePoolImpl service = resources.autobuild(PagePoolImpl.class);

        // This covers invalidations due to changes to classes

        pageLoader.addInvalidationListener(service);

        // This covers invalidation due to changes to message catalogs (properties files)

        componentMessagesSource.addInvalidationListener(service);

        // ... and this covers invalidations due to changes to templates

        _componentTemplateSource.addInvalidationListener(service);

        // Give the service a chance to clean up its own cache periodically as well

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    public PageLoader buildPageLoader(ServiceResources resources)
    {
        PageLoaderImpl service = resources.autobuild(PageLoaderImpl.class);

// Recieve invalidations when the class loader is discarded (due to a component class
// change). The notification is forwarded to the page loader's listeners.

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    @Scope(PERTHREAD_SCOPE)
    public RequestPageCache build(PagePool pagePool)
    {
        RequestPageCacheImpl service = new RequestPageCacheImpl(pagePool);

        _perthreadManager.addThreadCleanupListener(service);

        return service;
    }

    public ResourceCache build(ResourceDigestGenerator digestGenerator)
    {
        ResourceCacheImpl service = new ResourceCacheImpl(digestGenerator);

        _updateListenerHub.addUpdateListener(service);

        return service;
    }

    public ComponentTemplateSource build(TemplateParser parser, PageTemplateLocator locator)
    {
        ComponentTemplateSourceImpl service = new ComponentTemplateSourceImpl(parser, locator);

        _updateListenerHub.addUpdateListener(service);

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
        return new ContextAssetFactory(_request, globals.getContext(), optimizer);
    }

    public CookieSink buildCookieSink()
    {
        return new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                _requestGlobals.getHTTPServletResponse().addCookie(cookie);
            }

        };
    }

    public CookieSource buildCookieSource()
    {
        return new CookieSource()
        {

            public Cookie[] getCookies()
            {
                return _requestGlobals.getHTTPServletRequest().getCookies();
            }

        };
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

        return _chainBuilder.build(BindingFactory.class, configuration);
    }

    /**
     * Adds content types for "css" and "js" file extensions. <dl> <dt>css</dt> <dd>test/css</dd> <dt>js</dt>
     * <dd>text/javascript</dd> </dl>
     */
    @SuppressWarnings({ "JavaDoc" })
    public void contributeResourceStreamer(MappedConfiguration<String, String> configuration)
    {
        configuration.add("css", "text/css");
        configuration.add("js", "text/javascript");
    }

    /**
     * Adds a listener to the {@link org.apache.tapestry.internal.services.ComponentInstantiatorSource} that clears the
     * {@link PropertyAccess} and {@link TypeCoercer} caches on a class loader invalidation.  In addition, forces the
     * realization of {@link ComponentClassResolver} at startup.
     */
    public void contributeApplicationInitializer(OrderedConfiguration<ApplicationInitializerFilter> configuration,
                                                 final PropertyAccess propertyAccess, final TypeCoercer typeCoercer,
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

                _componentInstantiatorSource.addInvalidationListener(listener);

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
            private final Map<String, Object> _keywords = newCaseInsensitiveMap();

            {
                _keywords.put("true", Boolean.TRUE);
                _keywords.put("false", Boolean.FALSE);
                _keywords.put("null", null);
            }

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                String key = expression.trim();

                if (_keywords.containsKey(key)) return new LiteralBinding(description, _keywords.get(key), location);

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
            private final Pattern _pattern = Pattern.compile("^\\s*(-?\\d+)\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

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
            private final Pattern _pattern = Pattern
                    .compile("^\\s*(-?\\d+)\\s*\\.\\.\\s*(-?\\d+)\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

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
            private final Pattern _pattern = Pattern
                    .compile("^\\s*(\\-?((\\d+\\.)|(\\d*\\.\\d+)))\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

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

            private final Pattern _pattern = Pattern.compile("^\\s*'(.*)'\\s*$");

            public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                                      String expression, Location location)
            {
                Matcher matcher = _pattern.matcher(expression);

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


    public PersistentFieldStrategy buildClientPersistentFieldStrategy(LinkFactory linkFactory,
                                                                      ServiceResources resources)
    {
        ClientPersistentFieldStrategy service = resources
                .autobuild(ClientPersistentFieldStrategy.class);

        linkFactory.addListener(service);

        return service;
    }

    /**
     * Contributes filters: <dl> <dt>Ajax</dt> <dd>Determines if the request is Ajax oriented, and redirects to an
     * alternative handler if so</dd> <dt>ImmediateRender</dt> <dd>When {@linkplain
     * org.apache.tapestry.TapestryConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS_SYMBOL immediate action response
     * rendering} is enabled, generates the markup response (instead of a page redirect response, which is the normal
     * behavior) </dd> <dt>Secure</dt> <dd>Sends a redirect if an non-secure request accesses a secure page</dd></dl>
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

        configuration.add("Ajax", new AjaxFilter(_request, ajaxHandler));

        configuration.add("ImmediateRender", locator.autobuild(ImmediateActionRenderResponseFilter.class));

        configuration.add("Secure", secureFilter, "before:Ajax");
    }

    public ComponentClassCache buildComponentClassCache(@ComponentLayer ClassFactory classFactory)
    {
        ComponentClassCacheImpl service = new ComponentClassCacheImpl(classFactory);

        _componentInstantiatorSource.addInvalidationListener(service);

        return service;
    }

    /**
     * Chooses one of two implementations, based on the configured mode.
     */
    public ActionRenderResponseGenerator buildActionRenderResponseGenerator(

            @Symbol(TapestryConstants.SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS_SYMBOL)
            boolean immediateMode,

            ObjectLocator locator)
    {
        if (immediateMode) return locator.autobuild(ImmediateActionRenderResponseGenerator.class);

        return locator.autobuild(ActionRenderResponseGeneratorImpl.class);
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
}
