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

package org.apache.tapestry5.modules;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentParameterConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldValidationSupport;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.NullFieldStrategy;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.Renderable;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.Validator;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.annotations.DiscardAfter;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.PageAttached;
import org.apache.tapestry5.annotations.PageDetached;
import org.apache.tapestry5.annotations.PageLoaded;
import org.apache.tapestry5.annotations.PageReset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Secure;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.beaneditor.DataTypeConstants;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beanmodel.internal.services.BeanModelSourceImpl;
import org.apache.tapestry5.beanmodel.internal.services.PropertyConduitSourceImpl;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.ObjectProvider;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.BasicDataTypeAnalyzers;
import org.apache.tapestry5.commons.internal.services.AnnotationDataTypeAnalyzer;
import org.apache.tapestry5.commons.internal.services.DefaultDataTypeAnalyzer;
import org.apache.tapestry5.commons.internal.services.StringInterner;
import org.apache.tapestry5.commons.internal.services.StringInternerImpl;
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.commons.services.DataTypeAnalyzer;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.StrategyRegistry;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.corelib.components.PropertyDisplay;
import org.apache.tapestry5.corelib.components.PropertyEditor;
import org.apache.tapestry5.corelib.data.SecureOption;
import org.apache.tapestry5.corelib.pages.PropertyDisplayBlocks;
import org.apache.tapestry5.corelib.pages.PropertyEditBlocks;
import org.apache.tapestry5.grid.GridConstants;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalSymbols;
import org.apache.tapestry5.http.modules.TapestryHttpModule;
import org.apache.tapestry5.http.services.ApplicationGlobals;
import org.apache.tapestry5.http.services.ApplicationInitializer;
import org.apache.tapestry5.http.services.ApplicationInitializerFilter;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.HttpServletRequestFilter;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.internal.ComponentOverrideImpl;
import org.apache.tapestry5.internal.DefaultNullFieldStrategy;
import org.apache.tapestry5.internal.DefaultValueLabelProvider;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.InternalSymbols;
import org.apache.tapestry5.internal.PropertyOverridesImpl;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.ZeroNullFieldStrategy;
import org.apache.tapestry5.internal.alerts.AlertManagerImpl;
import org.apache.tapestry5.internal.beaneditor.EnvironmentMessages;
import org.apache.tapestry5.internal.beaneditor.MessagesConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.PrimitiveFieldConstraintGenerator;
import org.apache.tapestry5.internal.beaneditor.ValidateAnnotationConstraintGenerator;
import org.apache.tapestry5.internal.bindings.AssetBindingFactory;
import org.apache.tapestry5.internal.bindings.BlockBindingFactory;
import org.apache.tapestry5.internal.bindings.ComponentBindingFactory;
import org.apache.tapestry5.internal.bindings.ContextBindingFactory;
import org.apache.tapestry5.internal.bindings.LiteralBindingFactory;
import org.apache.tapestry5.internal.bindings.MessageBindingFactory;
import org.apache.tapestry5.internal.bindings.NullFieldStrategyBindingFactory;
import org.apache.tapestry5.internal.bindings.PropBindingFactory;
import org.apache.tapestry5.internal.bindings.RenderVariableBindingFactory;
import org.apache.tapestry5.internal.bindings.SymbolBindingFactory;
import org.apache.tapestry5.internal.bindings.TranslateBindingFactory;
import org.apache.tapestry5.internal.bindings.ValidateBindingFactory;
import org.apache.tapestry5.internal.dynamic.DynamicTemplateParserImpl;
import org.apache.tapestry5.internal.grid.CollectionGridDataSource;
import org.apache.tapestry5.internal.grid.NullDataSource;
import org.apache.tapestry5.internal.renderers.AvailableValuesRenderer;
import org.apache.tapestry5.internal.renderers.ComponentResourcesRenderer;
import org.apache.tapestry5.internal.renderers.EventContextRenderer;
import org.apache.tapestry5.internal.renderers.ListRenderer;
import org.apache.tapestry5.internal.renderers.LocationRenderer;
import org.apache.tapestry5.internal.renderers.ObjectArrayRenderer;
import org.apache.tapestry5.internal.renderers.RequestRenderer;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.services.ajax.AjaxFormUpdateFilter;
import org.apache.tapestry5.internal.services.ajax.AjaxResponseRendererImpl;
import org.apache.tapestry5.internal.services.ajax.MultiZoneUpdateEventResultProcessor;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.internal.services.exceptions.ExceptionReportWriterImpl;
import org.apache.tapestry5.internal.services.exceptions.ExceptionReporterImpl;
import org.apache.tapestry5.internal.services.linktransform.LinkTransformerImpl;
import org.apache.tapestry5.internal.services.linktransform.LinkTransformerInterceptor;
import org.apache.tapestry5.internal.services.messages.PropertiesFileParserImpl;
import org.apache.tapestry5.internal.services.meta.ContentTypeExtractor;
import org.apache.tapestry5.internal.services.meta.MetaAnnotationExtractor;
import org.apache.tapestry5.internal.services.meta.MetaWorkerImpl;
import org.apache.tapestry5.internal.services.meta.UnknownActivationContextExtractor;
import org.apache.tapestry5.internal.services.rest.DefaultOpenApiDescriptionGenerator;
import org.apache.tapestry5.internal.services.rest.DefaultOpenApiTypeDescriber;
import org.apache.tapestry5.internal.services.rest.MappedEntityManagerImpl;
import org.apache.tapestry5.internal.services.security.ClientWhitelistImpl;
import org.apache.tapestry5.internal.services.security.LocalhostOnly;
import org.apache.tapestry5.internal.services.templates.DefaultTemplateLocator;
import org.apache.tapestry5.internal.services.templates.PageTemplateLocator;
import org.apache.tapestry5.internal.transform.ActivationRequestParameterWorker;
import org.apache.tapestry5.internal.transform.ApplicationStateWorker;
import org.apache.tapestry5.internal.transform.BindParameterWorker;
import org.apache.tapestry5.internal.transform.CachedWorker;
import org.apache.tapestry5.internal.transform.ComponentWorker;
import org.apache.tapestry5.internal.transform.DiscardAfterWorker;
import org.apache.tapestry5.internal.transform.EnvironmentalWorker;
import org.apache.tapestry5.internal.transform.HeartbeatDeferredWorker;
import org.apache.tapestry5.internal.transform.ImportWorker;
import org.apache.tapestry5.internal.transform.InjectComponentWorker;
import org.apache.tapestry5.internal.transform.InjectContainerWorker;
import org.apache.tapestry5.internal.transform.InjectNamedProvider;
import org.apache.tapestry5.internal.transform.InjectPageWorker;
import org.apache.tapestry5.internal.transform.InjectServiceWorker;
import org.apache.tapestry5.internal.transform.InjectWorker;
import org.apache.tapestry5.internal.transform.LogWorker;
import org.apache.tapestry5.internal.transform.MixinAfterWorker;
import org.apache.tapestry5.internal.transform.MixinWorker;
import org.apache.tapestry5.internal.transform.OnEventWorker;
import org.apache.tapestry5.internal.transform.OperationWorker;
import org.apache.tapestry5.internal.transform.PageActivationContextWorker;
import org.apache.tapestry5.internal.transform.PageLifecycleAnnotationWorker;
import org.apache.tapestry5.internal.transform.PageResetAnnotationWorker;
import org.apache.tapestry5.internal.transform.ParameterWorker;
import org.apache.tapestry5.internal.transform.PersistWorker;
import org.apache.tapestry5.internal.transform.PropertyWorker;
import org.apache.tapestry5.internal.transform.RenderCommandWorker;
import org.apache.tapestry5.internal.transform.RenderPhaseMethodWorker;
import org.apache.tapestry5.internal.transform.RetainWorker;
import org.apache.tapestry5.internal.transform.SessionAttributeWorker;
import org.apache.tapestry5.internal.transform.SupportsInformalParametersWorker;
import org.apache.tapestry5.internal.transform.UnclaimedFieldWorker;
import org.apache.tapestry5.internal.translator.NumericTranslator;
import org.apache.tapestry5.internal.translator.NumericTranslatorSupport;
import org.apache.tapestry5.internal.translator.StringTranslator;
import org.apache.tapestry5.internal.util.RenderableAsBlock;
import org.apache.tapestry5.internal.util.StringRenderable;
import org.apache.tapestry5.internal.validator.ValidatorMacroImpl;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.ComponentLayer;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Operation;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.LazyAdvisor;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.PipelineBuilder;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.ioc.services.StrategyBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.services.UpdateListener;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.json.modules.JSONModule;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.Ajax;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockOverrideSource;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestFilter;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.ComponentLibraryInfo;
import org.apache.tapestry5.services.ComponentLibraryInfoSource;
import org.apache.tapestry5.services.ComponentMessages;
import org.apache.tapestry5.services.ComponentOverride;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.ComponentTemplates;
import org.apache.tapestry5.services.ContextPathEncoder;
import org.apache.tapestry5.services.ContextProvider;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.DateUtilities;
import org.apache.tapestry5.services.DefaultObjectRenderer;
import org.apache.tapestry5.services.DisplayBlockContribution;
import org.apache.tapestry5.services.EditBlockContribution;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.EnvironmentalShadowBuilder;
import org.apache.tapestry5.services.ExceptionReportWriter;
import org.apache.tapestry5.services.ExceptionReporter;
import org.apache.tapestry5.services.FieldTranslatorSource;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.FieldValidatorSource;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.apache.tapestry5.services.Html5Support;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.HttpStatus;
import org.apache.tapestry5.services.InitializeActivePageName;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.LinkCreationHub;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.services.MarkupRendererFilter;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.NullFieldStrategySource;
import org.apache.tapestry5.services.ObjectRenderer;
import org.apache.tapestry5.services.PageDocumentGenerator;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PageRenderRequestFilter;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;
import org.apache.tapestry5.services.PartialTemplateRenderer;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.RelativeElementPosition;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.ResourceDigestGenerator;
import org.apache.tapestry5.services.ResponseRenderer;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.StackTraceElementAnalyzer;
import org.apache.tapestry5.services.StackTraceElementClassConstants;
import org.apache.tapestry5.services.StreamPageContent;
import org.apache.tapestry5.services.Traditional;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TranslatorAlternatesSource;
import org.apache.tapestry5.services.TranslatorSource;
import org.apache.tapestry5.services.URLEncoder;
import org.apache.tapestry5.services.ValidationConstraintGenerator;
import org.apache.tapestry5.services.ValidationDecoratorFactory;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.ValueLabelProvider;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.dynamic.DynamicTemplate;
import org.apache.tapestry5.services.dynamic.DynamicTemplateParser;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.ModuleManager;
import org.apache.tapestry5.services.linktransform.ComponentEventLinkTransformer;
import org.apache.tapestry5.services.linktransform.LinkTransformer;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.apache.tapestry5.services.meta.FixedExtractor;
import org.apache.tapestry5.services.meta.MetaDataExtractor;
import org.apache.tapestry5.services.meta.MetaWorker;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManagerImpl;
import org.apache.tapestry5.services.pageload.PreloaderMode;
import org.apache.tapestry5.services.rest.MappedEntityManager;
import org.apache.tapestry5.services.rest.OpenApiDescriptionGenerator;
import org.apache.tapestry5.services.rest.OpenApiTypeDescriber;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.InjectionProvider2;
import org.apache.tapestry5.validator.Checked;
import org.apache.tapestry5.validator.Email;
import org.apache.tapestry5.validator.Max;
import org.apache.tapestry5.validator.MaxLength;
import org.apache.tapestry5.validator.Min;
import org.apache.tapestry5.validator.MinLength;
import org.apache.tapestry5.validator.None;
import org.apache.tapestry5.validator.Regexp;
import org.apache.tapestry5.validator.Required;
import org.apache.tapestry5.validator.Unchecked;
import org.apache.tapestry5.validator.ValidatorMacro;
import org.slf4j.Logger;

/**
 * The root module for Tapestry.
 */
@Marker(Core.class)
@ImportModule(
        {InternalModule.class, AssetsModule.class, PageLoadModule.class, JavaScriptModule.class, CompatibilityModule.class, DashboardModule.class, TapestryHttpModule.class, JSONModule.class})
public final class TapestryModule
{
    private final PipelineBuilder pipelineBuilder;

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
        this.chainBuilder = chainBuilder;
        this.environment = environment;
        this.strategyBuilder = strategyBuilder;
        this.propertyAccess = propertyAccess;
        this.request = request;
        this.response = response;
        this.environmentalBuilder = environmentalBuilder;
        this.endOfRequestEventHub = endOfRequestEventHub;
    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(PersistentLocale.class, PersistentLocaleImpl.class);
        binder.bind(ApplicationStateManager.class, ApplicationStateManagerImpl.class);
        binder.bind(ApplicationStatePersistenceStrategySource.class,
                ApplicationStatePersistenceStrategySourceImpl.class);
        binder.bind(BindingSource.class, BindingSourceImpl.class);
        binder.bind(FieldValidatorSource.class, FieldValidatorSourceImpl.class);
        binder.bind(Cookies.class, CookiesImpl.class);
        binder.bind(FieldValidatorDefaultSource.class, FieldValidatorDefaultSourceImpl.class);
        binder.bind(ResourceDigestGenerator.class, ResourceDigestGeneratorImpl.class);  // Remove in 5.5
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
        binder.bind(NumericTranslatorSupport.class);
        binder.bind(ClientDataEncoder.class, ClientDataEncoderImpl.class);
        binder.bind(ComponentEventLinkEncoder.class, ComponentEventLinkEncoderImpl.class);
        binder.bind(PageRenderLinkSource.class, PageRenderLinkSourceImpl.class);
        binder.bind(ValidatorMacro.class, ValidatorMacroImpl.class);
        binder.bind(PropertiesFileParser.class, PropertiesFileParserImpl.class);
        binder.bind(PageActivator.class, PageActivatorImpl.class);
        binder.bind(Dispatcher.class, AssetDispatcher.class).withSimpleId();
        binder.bind(TranslatorAlternatesSource.class, TranslatorAlternatesSourceImpl.class);
        binder.bind(MetaWorker.class, MetaWorkerImpl.class);
        binder.bind(LinkTransformer.class, LinkTransformerImpl.class);
        binder.bind(SelectModelFactory.class, SelectModelFactoryImpl.class);
        binder.bind(DynamicTemplateParser.class, DynamicTemplateParserImpl.class);
        binder.bind(AjaxResponseRenderer.class, AjaxResponseRendererImpl.class);
        binder.bind(AlertManager.class, AlertManagerImpl.class);
        binder.bind(ValidationDecoratorFactory.class, ValidationDecoratorFactoryImpl.class);
        binder.bind(PropertyConduitSource.class, PropertyConduitSourceImpl.class).eagerLoad();
        binder.bind(ClientWhitelist.class, ClientWhitelistImpl.class);
        binder.bind(MetaDataLocator.class, MetaDataLocatorImpl.class);
        binder.bind(ComponentClassCache.class, ComponentClassCacheImpl.class);
        binder.bind(PageActivationContextCollector.class, PageActivationContextCollectorImpl.class);
        binder.bind(StringInterner.class, StringInternerImpl.class);
        binder.bind(ValueEncoderSource.class, ValueEncoderSourceImpl.class);
        binder.bind(PathConstructor.class, PathConstructorImpl.class);
        binder.bind(DateUtilities.class, DateUtilitiesImpl.class);
        binder.bind(PartialTemplateRenderer.class, PartialTemplateRendererImpl.class);
        binder.bind(ExceptionReporter.class, ExceptionReporterImpl.class);
        binder.bind(ExceptionReportWriter.class, ExceptionReportWriterImpl.class);
        binder.bind(ComponentOverride.class, ComponentOverrideImpl.class).eagerLoad();
        binder.bind(Html5Support.class, Html5SupportImpl.class);
        binder.bind(MappedEntityManager.class, MappedEntityManagerImpl.class);
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
     * Contributes the factory for several built-in binding prefixes ("asset",
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


    @Contribute(ComponentClassResolver.class)
    public static void provideCoreAndAppLibraries(Configuration<LibraryMapping> configuration,
                                                  @Symbol(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                  String appRootPackage)
    {
        configuration.add(new LibraryMapping(InternalConstants.CORE_LIBRARY, "org.apache.tapestry5.corelib"));
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
     * <dt>Operation</dt> <dd>Support for the {@link Operation} method annotation</dd>
     * </dl>
     */
    @Contribute(ComponentClassTransformWorker2.class)
    @Primary
    public static void provideTransformWorkers(
            OrderedConfiguration<ComponentClassTransformWorker2> configuration,
            MetaWorker metaWorker,
            ComponentClassResolver resolver,
            ComponentDependencyRegistry componentDependencyRegistry)
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
     * {@link org.apache.tapestry5.commons.internal.services.DefaultDataTypeAnalyzer} service (
     * {@link #contributeDefaultDataTypeAnalyzer(org.apache.tapestry5.commons.MappedConfiguration)} )</dd>
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
        BasicDataTypeAnalyzers.provideDefaultDataTypeAnalyzers(configuration);
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
    @Contribute(FieldValidatorSource.class)
    public static void setupCoreFrameworkValidators(MappedConfiguration<String, Validator> configuration)
    {
        configuration.addInstance("required", Required.class);
        configuration.addInstance("minlength", MinLength.class);
        configuration.addInstance("maxlength", MaxLength.class);
        configuration.addInstance("min", Min.class);
        configuration.addInstance("max", Max.class);
        configuration.addInstance("regexp", Regexp.class);
        configuration.addInstance("email", Email.class);
        configuration.addInstance("checked", Checked.class);
        configuration.addInstance("unchecked", Unchecked.class);
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
     * <dd>Stores the request and response into {@link org.apache.tapestry5.http.services.RequestGlobals} at the start of the
     * pipeline</dd>
     * <dt>IgnoredPaths</dt>
     * <dd>Identifies requests that are known (via the IgnoredPathsFilter service's configuration) to be mapped to other
     * applications</dd>
     * <dt>GZip</dt>
     * <dd>Handles GZIP compression of response streams (if supported by client)</dd>
     * </dl>
     */
    public void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration,

                                                    @InjectService("IgnoredPathsFilter")
                                                    HttpServletRequestFilter ignoredPathsFilter)
    {
        configuration.add("IgnoredPaths", ignoredPathsFilter);
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
     * <dd>Stores the request and response into the {@link org.apache.tapestry5.http.services.RequestGlobals} service (this
     * is repeated at the end of the pipeline, in case any filter substitutes the request or response).
     * <dt>EndOfRequest</dt>
     * <dd>Notifies internal services that the request has ended</dd>
     * </dl>
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration, Context context,

                                         @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                         boolean productionMode,
                                         
                                         final PageClassLoaderContextManager pageClassLoaderContextManager)
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
                                                  NumericTranslatorSupport support, Html5Support html5Support)
    {

        configuration.add(String.class, new StringTranslator());

        Class[] types = new Class[]
                {Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class,
                        BigDecimal.class};

        for (Class type : types)
        {
            String name = type.getSimpleName().toLowerCase();

            configuration.add(type, new NumericTranslator(name, type, support, html5Support));
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
    public static void contributeTypeCoercer(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration,

                                             final ObjectLocator objectLocator,

                                             @Builtin
                                             final ThreadLocale threadLocale,

                                             @Core
                                             final AssetSource assetSource,

                                             @Core
                                             final DynamicTemplateParser dynamicTemplateParser)
    {
        CoercionTuple<ComponentResources, PropertyOverrides> componentResourcesToPropertyOverrides = CoercionTuple.create(ComponentResources.class, PropertyOverrides.class,
                new Coercion<ComponentResources, PropertyOverrides>()
                {
                    public PropertyOverrides coerce(ComponentResources input)
                    {
                        return new PropertyOverridesImpl(input);
                    }
                });
        configuration.add(componentResourcesToPropertyOverrides.getKey(), componentResourcesToPropertyOverrides);


        // See TAP5-2184 for why this causes some trouble!
        CoercionTuple<String, SelectModel> stringToSelectModel = CoercionTuple.create(String.class, SelectModel.class, new Coercion<String, SelectModel>()
        {
            public SelectModel coerce(String input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        });
        configuration.add(stringToSelectModel.getKey(), stringToSelectModel);

        CoercionTuple<Map, SelectModel> mapToSelectModel = CoercionTuple.create(Map.class, SelectModel.class, new Coercion<Map, SelectModel>()
        {
            @SuppressWarnings("unchecked")
            public SelectModel coerce(Map input)
            {
                return TapestryInternalUtils.toSelectModel(input);
            }
        });
        configuration.add(mapToSelectModel.getKey(), mapToSelectModel);

        CoercionTuple<Collection, GridDataSource> collectionToGridDataSource = CoercionTuple.create(Collection.class, GridDataSource.class,
                new Coercion<Collection, GridDataSource>()
                {
                    public GridDataSource coerce(Collection input)
                    {
                        return new CollectionGridDataSource(input);
                    }
                });
        configuration.add(collectionToGridDataSource.getKey(), collectionToGridDataSource);

        CoercionTuple<Void, GridDataSource> voidToGridDataSource = CoercionTuple.create(void.class, GridDataSource.class, new Coercion<Void, GridDataSource>()
        {
            private final GridDataSource source = new NullDataSource();

            public GridDataSource coerce(Void input)
            {
                return source;
            }
        });
        configuration.add(voidToGridDataSource.getKey(), voidToGridDataSource);

        CoercionTuple<List, SelectModel> listToSelectModel = CoercionTuple.create(List.class, SelectModel.class, new Coercion<List, SelectModel>()
        {
            private SelectModelFactory selectModelFactory;

            @SuppressWarnings("unchecked")
            public SelectModel coerce(List input)
            {
                // This doesn't look thread safe, but it is because its a one-time transition from null
                // to another value, and a race condition is harmless.
                if (selectModelFactory == null)
                {
                    selectModelFactory = objectLocator.getService(SelectModelFactory.class);
                }

                return selectModelFactory.create(input);
            }
        });
        configuration.add(listToSelectModel.getKey(), listToSelectModel);

        CoercionTuple<String, Pattern> stringToPattern = CoercionTuple.create(String.class, Pattern.class, new Coercion<String, Pattern>()
        {
            public Pattern coerce(String input)
            {
                return Pattern.compile(input);
            }
        });
        configuration.add(stringToPattern.getKey(), stringToPattern);

        CoercionTuple<ComponentResourcesAware, ComponentResources> componentResourcesAwareToComponentResources = CoercionTuple.create(ComponentResourcesAware.class, ComponentResources.class,
                new Coercion<ComponentResourcesAware, ComponentResources>()
                {

                    public ComponentResources coerce(ComponentResourcesAware input)
                    {
                        return input.getComponentResources();
                    }
                });
        configuration.add(componentResourcesAwareToComponentResources.getKey(), componentResourcesAwareToComponentResources);

        CoercionTuple<String, Renderable> stringToRenderable = CoercionTuple.create(String.class, Renderable.class, new Coercion<String, Renderable>()
        {
            public Renderable coerce(String input)
            {
                return new StringRenderable(input);
            }
        });
        configuration.add(stringToRenderable.getKey(), stringToRenderable);

        CoercionTuple<Renderable, Block> renderableToBlock = CoercionTuple.create(Renderable.class, Block.class, new Coercion<Renderable, Block>()
        {
            public Block coerce(Renderable input)
            {
                return new RenderableAsBlock(input);
            }
        });
        configuration.add(renderableToBlock.getKey(), renderableToBlock);

        CoercionTuple<String, DateFormat> stringToDateFormat = CoercionTuple.create(String.class, DateFormat.class, new Coercion<String, DateFormat>()
        {
            public DateFormat coerce(String input)
            {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(input, threadLocale.getLocale());
                final String lenient = objectLocator.getService(SymbolSource.class).valueForSymbol(SymbolConstants.LENIENT_DATE_FORMAT);
                dateFormat.setLenient(Boolean.parseBoolean(lenient));
                return dateFormat;
            }
        });
        configuration.add(stringToDateFormat.getKey(), stringToDateFormat);

        CoercionTuple<String, Resource> stringToResource = CoercionTuple.create(String.class, Resource.class, new Coercion<String, Resource>()
        {
            public Resource coerce(String input)
            {
                return assetSource.resourceForPath(input);
            }
        });
        configuration.add(stringToResource.getKey(), stringToResource);

        CoercionTuple<Renderable, RenderCommand> renderableToRenderCommand = CoercionTuple.create(Renderable.class, RenderCommand.class,
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
                });
        configuration.add(renderableToRenderCommand.getKey(), renderableToRenderCommand);

        CoercionTuple<Date, Calendar> dateToCalendar = CoercionTuple.create(Date.class, Calendar.class, new Coercion<Date, Calendar>()
        {
            public Calendar coerce(Date input)
            {
                Calendar calendar = Calendar.getInstance(threadLocale.getLocale());
                calendar.setTime(input);
                return calendar;
            }
        });
        configuration.add(dateToCalendar.getKey(), dateToCalendar);

        CoercionTuple<Resource, DynamicTemplate> resourceToDynamicTemplate = CoercionTuple.create(Resource.class, DynamicTemplate.class,
                new Coercion<Resource, DynamicTemplate>()
                {
                    public DynamicTemplate coerce(Resource input)
                    {
                        return dynamicTemplateParser.parseTemplate(input);
                    }
                });
        configuration.add(resourceToDynamicTemplate.getKey(), resourceToDynamicTemplate);

        CoercionTuple<Asset, Resource> assetToResource = CoercionTuple.create(Asset.class, Resource.class, new Coercion<Asset, Resource>()
        {
            public Resource coerce(Asset input)
            {
                return input.getResource();
            }
        });
        configuration.add(assetToResource.getKey(), assetToResource);

        CoercionTuple<ValueEncoder, ValueEncoderFactory> valueEncoderToValueEncoderFactory = CoercionTuple.create(ValueEncoder.class, ValueEncoderFactory.class, new Coercion<ValueEncoder, ValueEncoderFactory>()
        {
            public ValueEncoderFactory coerce(ValueEncoder input)
            {
                return new GenericValueEncoderFactory(input);
            }
        });
        configuration.add(valueEncoderToValueEncoderFactory.getKey(), valueEncoderToValueEncoderFactory);
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


    /**
     * Builds the PropBindingFactory as a chain of command. The terminator of
     * the chain is responsible for ordinary
     * property names (and property paths).
     *
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
     * {@linkplain #provideDefaultBeanBlocks(org.apache.tapestry5.commons.Configuration)} locale
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
     */
    public InjectionProvider2 buildInjectionProvider(List<InjectionProvider2> configuration)
    {
        return chainBuilder.build(InjectionProvider2.class, configuration);
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
        configuration.addInstance(HttpStatus.class, HttpStatusComponentEventResultProcessor.class);

        configuration.addInstance(String.class, PageNameComponentEventResultProcessor.class);

        configuration.addInstance(Class.class, ClassResultProcessor.class);

        configuration.add(Component.class, componentInstanceProcessor);

        configuration.addInstance(StreamResponse.class, StreamResponseResultProcessor.class);

        configuration.addInstance(StreamPageContent.class, StreamPageContentResultProcessor.class);
        
        configuration.addInstance(JSONArray.class, JSONCollectionEventResultProcessor.class);
        configuration.addInstance(JSONObject.class, JSONCollectionEventResultProcessor.class);
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
     * <dt>{@link org.apache.tapestry5.http.Link}</dt>
     * <dd>Sends a JSON response to redirect to the link</dd>
     * <dt>{@link Class}</dt>
     * <dd>Treats the class as a page class and sends a redirect for a page render for that page</dd>
     * <dt>{@link org.apache.tapestry5.ajax.MultiZoneUpdate}</dt>
     * <dd>Sends a single JSON response to update the content of multiple zones
     * </dl>
     *
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
        configuration.addInstance(JSONArray.class, JSONCollectionEventResultProcessor.class);
        configuration.addInstance(StreamResponse.class, StreamResponseResultProcessor.class);
        configuration.addInstance(String.class, AjaxPageNameComponentEventResultProcessor.class);
        configuration.addInstance(Link.class, AjaxLinkComponentEventResultProcessor.class);
        configuration.addInstance(URL.class, AjaxURLComponentEventResultProcessor.class);
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
     * <dt>PageRender</dt>
     * <dd>Identifies the {@link org.apache.tapestry5.services.PageRenderRequestParameters} and forwards onto
     * {@link PageRenderRequestHandler}</dd>
     * <dt>ComponentEvent</dt>
     * <dd>Identifies the {@link ComponentEventRequestParameters} and forwards onto the
     * {@link ComponentEventRequestHandler}</dd>
     * </dl>
     */
    public static void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration,
            @Symbol(SymbolConstants.PUBLISH_OPENAPI_DEFINITON) final boolean publishOpenApiDefinition)
    {
        
        if (publishOpenApiDefinition)
        {
            configuration.addInstance("OpenAPI", OpenApiDescriptionDispatcher.class, "before:PageRender", "before:ComponentEvent");
        }
        
        // Looks for the root path and renders the start page. This is
        // maintained for compatibility
        // with earlier versions of Tapestry 5, it is recommended that an Index
        // page be used instead.

        configuration.addInstance("RootPath", RootPathDispatcher.class, "before:Asset");

        configuration.addInstance("ComponentEvent", ComponentEventDispatcher.class, "before:PageRender");

        configuration.addInstance("PageRender", PageRenderDispatcher.class);
    }

    /**
     * Contributes a default object renderer for type Object, plus specialized
     * renderers for {@link org.apache.tapestry5.http.services.Request}, {@link org.apache.tapestry5.commons.Location},
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
     * <dt>ClientBehaviorSupport (deprecated in 5.4)</dt>
     * <dd>Provides {@link ClientBehaviorSupport}</dd>
     * <dt>Heartbeat</dt>
     * <dd>Provides {@link org.apache.tapestry5.services.Heartbeat}</dd>
     * <dt>ValidationDecorator (deprecated in 5.4)</dt>
     * <dd>Provides {@link org.apache.tapestry5.ValidationDecorator} (via {@link ValidationDecoratorFactory#newInstance(org.apache.tapestry5.MarkupWriter)})</dd>
     * <dt>PageNameMeta (since 5.4)</dt>
     * <dd>Renders a {@code <meta/>} tag describing the active page name (development mode only)</dd>
     * <dt>ImportCoreStack (since 5.4) </dt>
     * <dd>Imports the "core" stack (necessary to get the Bootstrap CSS, if nothing else).</dd>
     * </dl>
     *
     * @see org.apache.tapestry5.SymbolConstants#OMIT_GENERATOR_META
     * @see org.apache.tapestry5.http.TapestryHttpSymbolConstants#PRODUCTION_MODE
     * @see org.apache.tapestry5.SymbolConstants#INCLUDE_CORE_STACK
     * @see org.apache.tapestry5.SymbolConstants#ENABLE_PAGELOADING_MASK
     */
    public void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration,

                                         final ModuleManager moduleManager,

                                         @Symbol(SymbolConstants.OMIT_GENERATOR_META)
                                         final boolean omitGeneratorMeta,

                                         @Symbol(TapestryHttpSymbolConstants.TAPESTRY_VERSION)
                                         final String tapestryVersion,

                                         @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                         boolean productionMode,

                                         @Symbol(SymbolConstants.INCLUDE_CORE_STACK)
                                         final boolean includeCoreStack,

                                         @Symbol(SymbolConstants.ENABLE_PAGELOADING_MASK)
                                         final boolean enablePageloadingMask,

                                         final ValidationDecoratorFactory validationDecoratorFactory)
    {
        MarkupRendererFilter documentLinker = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                DocumentLinkerImpl linker = new DocumentLinkerImpl(moduleManager, omitGeneratorMeta, enablePageloadingMask, tapestryVersion);

                environment.push(DocumentLinker.class, linker);

                renderer.renderMarkup(writer);

                environment.pop(DocumentLinker.class);

                linker.updateDocument(writer.getDocument());
            }
        };


        MarkupRendererFilter clientBehaviorSupport = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                ClientBehaviorSupportImpl clientBehaviorSupport = new ClientBehaviorSupportImpl();

                environment.push(ClientBehaviorSupport.class, clientBehaviorSupport);

                renderer.renderMarkup(writer);

                environment.pop(ClientBehaviorSupport.class);
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

        MarkupRendererFilter importCoreStack = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {
                renderer.renderMarkup(writer);

                environment.peekRequired(JavaScriptSupport.class).importStack(InternalConstants.CORE_STACK_NAME);
            }
        };

        configuration.add("DocumentLinker", documentLinker);
        configuration.add("ClientBehaviorSupport", clientBehaviorSupport, "after:JavaScriptSupport");
        configuration.add("Heartbeat", heartbeat);
        configuration.add("ValidationDecorator", defaultValidationDecorator);

        if (includeCoreStack)
        {
            configuration.add("ImportCoreStack", importCoreStack);
        }

        if (productionMode)
        {
            configuration.add("PageNameMeta", null);
        } else
        {
            configuration.addInstance("PageNameMeta", PageNameMetaInjector.class);
        }
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
                ClientBehaviorSupportImpl support = new ClientBehaviorSupportImpl();

                environment.push(ClientBehaviorSupport.class, support);

                renderer.renderMarkup(writer, reply);

                environment.pop(ClientBehaviorSupport.class);
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
                "en,it,es,zh_CN,pt_PT,de,ru,hr,fi_FI,sv_SE,fr,da,pt_BR,ja,el,bg,nn,nb,sr_RS,mk_MK");

        configuration.add(SymbolConstants.COOKIE_MAX_AGE, "7 d");

        configuration.add(SymbolConstants.START_PAGE_NAME, "start");

        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, true);

        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, true);

        configuration.add(MetaDataConstants.SECURE_PAGE, false);

        configuration.add(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED, true);

        // This is designed to make it easy to keep synchronized with
        // script.aculo.ous. As we support a new version, we create a new folder, and update the
        // path entry. We can then delete the old version folder (or keep it around). This should
        // be more manageable than overwriting the local copy with updates (it's too easy for
        // files deleted between scriptaculous releases to be accidentally left lying around).
        // There's also a ClasspathAliasManager contribution based on the path.

        configuration.add(SymbolConstants.SCRIPTACULOUS, "${tapestry.asset.root}/scriptaculous_1_9_0");

        // Likewise for WebFX DatePicker, currently version 1.0.6

        configuration.add(SymbolConstants.DATEPICKER, "${tapestry.asset.root}/datepicker_106");

        configuration.add(SymbolConstants.PERSISTENCE_STRATEGY, PersistenceConstants.SESSION);

        configuration.add(MetaDataConstants.RESPONSE_CONTENT_TYPE, "text/html");

        configuration.add(SymbolConstants.APPLICATION_CATALOG,
                String.format("context:WEB-INF/${%s}.properties", TapestryHttpInternalSymbols.APP_NAME));

        configuration.add(SymbolConstants.EXCEPTION_REPORT_PAGE, "ExceptionReport");

        configuration.add(SymbolConstants.OMIT_GENERATOR_META, false);

        configuration.add(SymbolConstants.SECURE_ENABLED, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.COMPACT_JSON, SymbolConstants.PRODUCTION_MODE_VALUE);

        configuration.add(SymbolConstants.ENCODE_LOCALE_INTO_PATH, true);

        configuration.add(InternalSymbols.RESERVED_FORM_CONTROL_NAMES, "reset,submit,select,id,method,action,onsubmit," + InternalConstants.CANCEL_NAME);

        configuration.add(SymbolConstants.COMPONENT_RENDER_TRACING_ENABLED, false);

        configuration.add(SymbolConstants.APPLICATION_FOLDER, "");

        // Grid component parameter defaults
        configuration.add(ComponentParameterConstants.GRID_ROWS_PER_PAGE, GridConstants.ROWS_PER_PAGE);
        configuration.add(ComponentParameterConstants.GRID_PAGER_POSITION, GridConstants.PAGER_POSITION);
        configuration.add(ComponentParameterConstants.GRID_EMPTY_BLOCK, GridConstants.EMPTY_BLOCK);
        configuration.add(ComponentParameterConstants.GRID_TABLE_CSS_CLASS, GridConstants.TABLE_CLASS);
        configuration.add(ComponentParameterConstants.GRIDPAGER_PAGE_RANGE, GridConstants.PAGER_PAGE_RANGE);
        configuration.add(ComponentParameterConstants.GRIDCOLUMNS_SORTABLE_ASSET, GridConstants.COLUMNS_SORTABLE);
        configuration.add(ComponentParameterConstants.GRIDCOLUMNS_ASCENDING_ASSET, GridConstants.COLUMNS_ASCENDING);
        configuration.add(ComponentParameterConstants.GRIDCOLUMNS_DESCENDING_ASSET, GridConstants.COLUMNS_DESCENDING);

        // FormInjector component parameter defaults
        configuration.add(ComponentParameterConstants.FORMINJECTOR_INSERT_POSITION, "above");
        configuration.add(ComponentParameterConstants.FORMINJECTOR_SHOW_FUNCTION, "highlight");

        // Palette component parameter defaults
        configuration.add(ComponentParameterConstants.PALETTE_ROWS_SIZE, 10);

        // Defaults for components that use a SelectModel
        configuration.add(ComponentParameterConstants.VALIDATE_WITH_MODEL, SecureOption.AUTO);

        // Zone component parameters defaults
        configuration.add(ComponentParameterConstants.ZONE_SHOW_METHOD, "show");
        configuration.add(ComponentParameterConstants.ZONE_UPDATE_METHOD, "highlight");

        // By default, no page is on the whitelist unless it has the @WhitelistAccessOnly annotation
        configuration.add(MetaDataConstants.WHITELIST_ONLY_PAGE, false);

        configuration.add(TapestryHttpSymbolConstants.CONTEXT_PATH, "");

        // Leaving this as the default results in a runtime error logged to the console (and a default password is used);
        // you are expected to override this symbol.
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "");

        // TAP5-2070 keep the old behavior, defaults to false
        configuration.add(MetaDataConstants.UNKNOWN_ACTIVATION_CONTEXT_CHECK, false);

        // TAP5-2197
        configuration.add(SymbolConstants.INCLUDE_CORE_STACK, true);

        // TAP5-2182
        configuration.add(SymbolConstants.FORM_GROUP_WRAPPER_CSS_CLASS, "form-group");
        configuration.add(SymbolConstants.FORM_GROUP_LABEL_CSS_CLASS, "control-label");
        configuration.add(SymbolConstants.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME, "");
        configuration.add(SymbolConstants.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS, "");
        configuration.add(SymbolConstants.FORM_FIELD_CSS_CLASS, "form-control");
        
        // Grid's default table CSS class comes from the ComponentParameterConstants.GRID_TABLE_CSS_CLASS symbol
        configuration.add(SymbolConstants.BEAN_DISPLAY_CSS_CLASS, "well dl-horizontal");
        configuration.add(SymbolConstants.BEAN_EDITOR_BOOLEAN_PROPERTY_DIV_CSS_CLASS, "input-group");
        configuration.add(SymbolConstants.ERROR_CSS_CLASS, "help-block");
        configuration.add(SymbolConstants.AJAX_FORM_LOOP_ADD_ROW_LINK_CSS_CLASS, "btn btn-default btn-sm");
        configuration.add(SymbolConstants.ERRORS_BASE_CSS_CLASS, "alert-dismissable");
        configuration.add(SymbolConstants.ERRORS_DEFAULT_CLASS_PARAMETER_VALUE, "alert alert-danger");
        configuration.add(SymbolConstants.ERRORS_CLOSE_BUTTON_CSS_CLASS, "close");

        // TAP5-1998
        configuration.add(SymbolConstants.LENIENT_DATE_FORMAT, false);

        // TAP5-2187
        configuration.add(SymbolConstants.STRICT_CSS_URL_REWRITING, false);

        configuration.add(SymbolConstants.EXCEPTION_REPORTS_DIR, "build/exceptions");

        // TAP5-1815
        configuration.add(SymbolConstants.ENABLE_HTML5_SUPPORT, false);

        configuration.add(SymbolConstants.RESTRICTIVE_ENVIRONMENT, false);

        configuration.add(SymbolConstants.ENABLE_PAGELOADING_MASK, true);
        configuration.add(SymbolConstants.PRELOADER_MODE, PreloaderMode.PRODUCTION);
        
        configuration.add(SymbolConstants.OPENAPI_VERSION, "3.0.0");
        configuration.add(SymbolConstants.PUBLISH_OPENAPI_DEFINITON, "false");
        configuration.add(SymbolConstants.OPENAPI_DESCRIPTION_PATH, "/openapi.json");
        configuration.add(SymbolConstants.OPENAPI_BASE_PATH, "/");
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
     *
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
     *
     * For elements input, select, textarea and label the hidden field is positioned after.
     *
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
        final EnvironmentImpl service = new EnvironmentImpl();

        perthreadManager.addThreadCleanupCallback(new Runnable()
        {
            public void run()
            {
                service.threadDidCleanup();
            }
        });

        return service;
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
     * <dd>Omits stack frames used to provide access to container class private members</dd>
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
        configuration.add("OperationTracker", new RegexpStackTraceElementAnalyzer(Pattern.compile("internal\\.(RegistryImpl|PerThreadOperationTracker|OperationTrackerImpl).*(run|invoke|perform)\\("), StackTraceElementClassConstants.OMITTED));
        configuration.add("Access", new RegexpStackTraceElementAnalyzer(Pattern.compile("\\.access\\$\\d+\\("), StackTraceElementClassConstants.OMITTED));

        configuration.addInstance("Application", ApplicationStackTraceElementAnalyzer.class);

    }


    /**
     * Advises the {@link org.apache.tapestry5.services.messages.ComponentMessagesSource} service so
     * that the creation
     * of {@link org.apache.tapestry5.commons.Messages} instances can be deferred.
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
     * <dt>OperationTracker</dt>
     * <dd>Tracks general information about the request using {@link OperationTracker}</dd>
     * <dt>UnknownComponentFilter (production mode only)</dt>
     * <dd>{@link org.apache.tapestry5.internal.services.ProductionModeUnknownComponentFilter} - Detects request with unknown component and aborts handling to ultimately deliver a 404 response</dd>
     * <dt>InitializeActivePageName
     * <dd>{@link InitializeActivePageName}
     * <dt>DeferredResponseRenderer</dt>
     * <dd>{@link DeferredResponseRenderer}</dd>
     * </dl>
     *
     * @since 5.2.0
     */
    public void contributeComponentRequestHandler(OrderedConfiguration<ComponentRequestFilter> configuration, @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        configuration.addInstance("OperationTracker", RequestOperationTracker.class);

        if (productionMode)
        {
            configuration.addInstance("UnknownComponentFilter", ProductionModeUnknownComponentFilter.class);
        }

        configuration.addInstance("InitializeActivePageName", InitializeActivePageName.class);
        configuration.addInstance("DeferredResponseRenderer", DeferredResponseRenderer.class);
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
        configuration.addInstance(UnknownActivationContextCheck.class, UnknownActivationContextExtractor.class);
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
                                               @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
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

    /**
     * @since 5.4
     */
    @Contribute(ValueLabelProvider.class)
    public void defaultValueLabelProviders(MappedConfiguration<Class, ValueLabelProvider> configuration)
    {
        configuration.addInstance(Object.class, DefaultValueLabelProvider.class);
        configuration.addInstance(Enum.class, EnumValueLabelProvider.class);
    }

    /**
     * @since 5.4
     */
    public ValueLabelProvider<?> buildValueLabelProvider(Map<Class, ValueLabelProvider> configuration)
    {
        return strategyBuilder.build(ValueLabelProvider.class, configuration);
    }

    @Advise(serviceInterface = ComponentInstantiatorSource.class)
    public static void componentReplacer(MethodAdviceReceiver methodAdviceReceiver,
                                         final ComponentOverride componentReplacer) throws NoSuchMethodException, SecurityException
    {

        if (componentReplacer.hasReplacements())
        {

            MethodAdvice advice = new MethodAdvice()
            {
                @Override
                public void advise(MethodInvocation invocation)
                {
                    String className = (String) invocation.getParameter(0);
                    final Class<?> replacement = componentReplacer.getReplacement(className);
                    if (replacement != null)
                    {
                        invocation.setParameter(0, replacement.getName());
                    }
                    invocation.proceed();
                }
            };

            methodAdviceReceiver.adviseMethod(
                    ComponentInstantiatorSource.class.getMethod("getInstantiator", String.class), advice);

        }
    }

    public static ComponentLibraryInfoSource buildComponentLibraryInfoSource(List<ComponentLibraryInfoSource> configuration,
                                                                             ChainBuilder chainBuilder)
    {
        return chainBuilder.build(ComponentLibraryInfoSource.class, configuration);
    }

    @Contribute(ComponentLibraryInfoSource.class)
    public static void addBuiltInComponentLibraryInfoSources(OrderedConfiguration<ComponentLibraryInfoSource> configuration)
    {
        configuration.addInstance("Maven", MavenComponentLibraryInfoSource.class);
        configuration.add("TapestryCore", new TapestryCoreComponentLibraryInfoSource());
    }
    
    public static OpenApiDescriptionGenerator buildOpenApiDocumentationGenerator(List<OpenApiDescriptionGenerator> configuration,
            ChainBuilder chainBuilder) 
    {
        return chainBuilder.build(OpenApiDescriptionGenerator.class, configuration);
    }

    public static OpenApiTypeDescriber buildOpenApiTypeDescriber(List<OpenApiTypeDescriber> configuration,
            ChainBuilder chainBuilder) 
    {
        return chainBuilder.build(OpenApiTypeDescriber.class, configuration);
    }

    @Contribute(OpenApiDescriptionGenerator.class)
    public static void addBuiltInOpenApiDocumentationGenerator(
            OrderedConfiguration<OpenApiDescriptionGenerator> configuration) {
        configuration.addInstance("Default", DefaultOpenApiDescriptionGenerator.class, "before:*");
    }

    @Contribute(OpenApiTypeDescriber.class)
    public static void addBuiltInOpenApiTypeDescriber(
            OrderedConfiguration<OpenApiTypeDescriber> configuration) {
        configuration.addInstance("Default", DefaultOpenApiTypeDescriber.class, "before:*");
    }
    
    /**
     * Contributes the package "&lt;root&gt;.rest.entities" to the configuration, 
     * so that it will be scanned for mapped entity classes.
     */
    public static void contributeMappedEntityManager(Configuration<String> configuration,
            @Symbol(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM) String appRootPackage)
    {
        configuration.add(appRootPackage + ".rest.entities");
    }
    
    public static ComponentDependencyRegistry buildComponentDependencyRegistry(
            InternalComponentInvalidationEventHub internalComponentInvalidationEventHub,
            ResourceChangeTracker resourceChangeTracker,
            ComponentTemplateSource componentTemplateSource,
            PageClassLoaderContextManager pageClassLoaderContextManager,
            ComponentInstantiatorSource componentInstantiatorSource,
            ComponentClassResolver componentClassResolver,
            TemplateParser templateParser,
            ComponentTemplateLocator componentTemplateLocator,
            PerthreadManager perthreadManager)
    {
        ComponentDependencyRegistryImpl componentDependencyRegistry = 
                new ComponentDependencyRegistryImpl(
                        pageClassLoaderContextManager,
                        componentInstantiatorSource.getProxyFactory().getPlasticManager(),
                        componentClassResolver,
                        templateParser,
                        componentTemplateLocator);
        componentDependencyRegistry.listen(internalComponentInvalidationEventHub);
        componentDependencyRegistry.listen(resourceChangeTracker);
        componentDependencyRegistry.listen(componentTemplateSource.getInvalidationEventHub());
        // TODO: remove
        componentDependencyRegistry.setupThreadCleanup(perthreadManager);
        return componentDependencyRegistry;
    }
    
    private static final class TapestryCoreComponentLibraryInfoSource implements
            ComponentLibraryInfoSource
    {
        @Override
        public ComponentLibraryInfo find(LibraryMapping libraryMapping)
        {
            ComponentLibraryInfo info = null;
            if (libraryMapping.libraryName.equals("core"))
            {

                info = new ComponentLibraryInfo();

                // the information above will probably not change in the future, or change very 
                // infrequently, so I see no problem in hardwiring them here.
                info.setArtifactId("tapestry-core");
                info.setGroupId("org.apache.tapestry");
                info.setName("Tapestry 5 core component library");
                info.setDescription("Components provided out-of-the-box by Tapestry");
                info.setDocumentationUrl("http://tapestry.apache.org/component-reference.html");
                info.setJavadocUrl("http://tapestry.apache.org/current/apidocs/");
                info.setSourceBrowseUrl("https://gitbox.apache.org/repos/asf?p=tapestry-5.git;a=summary");
                info.setSourceRootUrl("https://gitbox.apache.org/repos/asf?p=tapestry-5.git;a=blob;f=tapestry-core/src/main/java/");
                info.setIssueTrackerUrl("https://issues.apache.org/jira/browse/TAP5");
                info.setHomepageUrl("http://tapestry.apache.org");
                info.setLibraryMapping(libraryMapping);

                final InputStream inputStream = TapestryModule.class.getResourceAsStream(
                        "/META-INF/gradle/org.apache.tapestry/tapestry-core/project.properties");

                if (inputStream != null)
                {
                    Properties properties = new Properties();
                    try
                    {
                        properties.load(inputStream);
                    } catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                    info.setVersion(properties.getProperty("version"));
                }
            }
            return info;
        }
    }

}
