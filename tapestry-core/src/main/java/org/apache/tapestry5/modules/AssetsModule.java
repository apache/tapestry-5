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

import java.util.List;
import java.util.Map;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.http.services.ApplicationGlobals;
import org.apache.tapestry5.http.services.CompressionAnalyzer;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.internal.AssetConstants;
import org.apache.tapestry5.internal.services.AssetSourceImpl;
import org.apache.tapestry5.internal.services.ClasspathAssetAliasManagerImpl;
import org.apache.tapestry5.internal.services.ClasspathAssetFactory;
import org.apache.tapestry5.internal.services.ContextAssetFactory;
import org.apache.tapestry5.internal.services.ExternalUrlAssetFactory;
import org.apache.tapestry5.internal.services.IdentityAssetPathConverter;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.internal.services.assets.AssetChecksumGeneratorImpl;
import org.apache.tapestry5.internal.services.assets.AssetPathConstructorImpl;
import org.apache.tapestry5.internal.services.assets.CSSURLRewriter;
import org.apache.tapestry5.internal.services.assets.ClasspathAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.CompressionAnalyzerImpl;
import org.apache.tapestry5.internal.services.assets.ContentTypeAnalyzerImpl;
import org.apache.tapestry5.internal.services.assets.ContextAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.JavaScriptStackAssembler;
import org.apache.tapestry5.internal.services.assets.JavaScriptStackAssemblerImpl;
import org.apache.tapestry5.internal.services.assets.JavaScriptStackMinimizeDisabler;
import org.apache.tapestry5.internal.services.assets.MasterResourceMinimizer;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTrackerImpl;
import org.apache.tapestry5.internal.services.assets.SRSCachingInterceptor;
import org.apache.tapestry5.internal.services.assets.SRSCompressedCachingInterceptor;
import org.apache.tapestry5.internal.services.assets.SRSCompressingInterceptor;
import org.apache.tapestry5.internal.services.assets.SRSMinimizingInterceptor;
import org.apache.tapestry5.internal.services.assets.StackAssetRequestHandler;
import org.apache.tapestry5.internal.services.assets.StreamableResourceSourceImpl;
import org.apache.tapestry5.internal.services.assets.UTF8ForTextAssets;
import org.apache.tapestry5.internal.services.messages.ClientLocalizationMessageResource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Decorate;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.AssetFactory;
import org.apache.tapestry5.services.AssetPathConverter;
import org.apache.tapestry5.services.AssetRequestDispatcher;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClasspathAssetAliasManager;
import org.apache.tapestry5.services.ClasspathAssetProtectionRule;
import org.apache.tapestry5.services.ClasspathProvider;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ContextProvider;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.assets.ContentTypeAnalyzer;
import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;

/**
 * @since 5.3
 */
@Marker(Core.class)
public class AssetsModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(AssetFactory.class, ClasspathAssetFactory.class).withSimpleId();
        binder.bind(AssetPathConverter.class, IdentityAssetPathConverter.class);
        binder.bind(AssetPathConstructor.class, AssetPathConstructorImpl.class);
        binder.bind(ClasspathAssetAliasManager.class, ClasspathAssetAliasManagerImpl.class);
        binder.bind(AssetSource.class, AssetSourceImpl.class);
        binder.bind(StreamableResourceSource.class, StreamableResourceSourceImpl.class);
        binder.bind(CompressionAnalyzer.class, CompressionAnalyzerImpl.class);
        binder.bind(ContentTypeAnalyzer.class, ContentTypeAnalyzerImpl.class);
        binder.bind(ResourceChangeTracker.class, ResourceChangeTrackerImpl.class);
        binder.bind(ResourceMinimizer.class, MasterResourceMinimizer.class);
        binder.bind(AssetChecksumGenerator.class, AssetChecksumGeneratorImpl.class);
        binder.bind(JavaScriptStackAssembler.class, JavaScriptStackAssemblerImpl.class);
    }

    @Contribute(AssetSource.class)
    public void configureStandardAssetFactories(MappedConfiguration<String, AssetFactory> configuration,
                                                @ContextProvider
                                                AssetFactory contextAssetFactory,

                                                @ClasspathProvider
                                                AssetFactory classpathAssetFactory)
    {
        configuration.add(AssetConstants.CONTEXT, contextAssetFactory);
        configuration.add(AssetConstants.CLASSPATH, classpathAssetFactory);
        configuration.add(AssetConstants.HTTP, new ExternalUrlAssetFactory(AssetConstants.HTTP));
        configuration.add(AssetConstants.HTTPS, new ExternalUrlAssetFactory(AssetConstants.HTTPS));
        configuration.add(AssetConstants.FTP, new ExternalUrlAssetFactory(AssetConstants.FTP));
        configuration.add(AssetConstants.PROTOCOL_RELATIVE, new ExternalUrlAssetFactory(AssetConstants.PROTOCOL_RELATIVE));
    }


    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void setupSymbols(MappedConfiguration<String, Object> configuration)
    {
        // Minification may be enabled in production mode, but unless a minimizer is provided, nothing
        // will change.
        configuration.add(SymbolConstants.MINIFICATION_ENABLED, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.ASSET_URL_FULL_QUALIFIED, false);

        configuration.add(SymbolConstants.ASSET_PATH_PREFIX, "assets");

        configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "${tapestry.asset.root}/bootstrap");
        configuration.add(SymbolConstants.FONT_AWESOME_ROOT, "${tapestry.asset.root}/font_awesome");

        configuration.add("tapestry.asset.root", "classpath:META-INF/assets/tapestry5");
        configuration.add(SymbolConstants.OMIT_EXPIRATION_CACHE_CONTROL_HEADER, "max-age=60,must-revalidate");
    }

    // The use of decorators is to allow third-parties to get their own extensions
    // into the pipeline.

    @Decorate(id = "GZipCompression", serviceInterface = StreamableResourceSource.class)
    public StreamableResourceSource enableCompression(StreamableResourceSource delegate,
                                                      @Symbol(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED)
                                                      boolean gzipEnabled, @Symbol(TapestryHttpSymbolConstants.MIN_GZIP_SIZE)
                                                      int compressionCutoff,
                                                      AssetChecksumGenerator checksumGenerator)
    {
        return gzipEnabled
                ? new SRSCompressingInterceptor(delegate, compressionCutoff, checksumGenerator)
                : null;
    }

    @Decorate(id = "CacheCompressed", serviceInterface = StreamableResourceSource.class)
    @Order("before:GZIpCompression")
    public StreamableResourceSource enableCompressedCaching(StreamableResourceSource delegate,
                                                            @Symbol(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED)
                                                            boolean gzipEnabled, ResourceChangeTracker tracker)
    {
        return gzipEnabled
                ? new SRSCompressedCachingInterceptor(delegate, tracker)
                : null;
    }

    @Decorate(id = "Cache", serviceInterface = StreamableResourceSource.class)
    @Order("after:GZipCompression")
    public StreamableResourceSource enableUncompressedCaching(StreamableResourceSource delegate,
                                                              ResourceChangeTracker tracker)
    {
        return new SRSCachingInterceptor(delegate, tracker);
    }

    // Goes after cache, to ensure that what we are caching is the minified version.
    @Decorate(id = "Minification", serviceInterface = StreamableResourceSource.class)
    @Order("after:Cache,TextUTF8")
    public StreamableResourceSource enableMinification(StreamableResourceSource delegate, ResourceMinimizer minimizer,
                                                       @Symbol(SymbolConstants.MINIFICATION_ENABLED)
                                                       boolean enabled)
    {
        return enabled
                ? new SRSMinimizingInterceptor(delegate, minimizer)
                : null;
    }

    // Ordering this after minification means that the URL replacement happens first;
    // then the minification, then the uncompressed caching, then compression, then compressed
    // cache.
    @Decorate(id = "CSSURLRewrite", serviceInterface = StreamableResourceSource.class)
    @Order("after:Minification")
    public StreamableResourceSource enableCSSURLRewriting(StreamableResourceSource delegate,
                                                          OperationTracker tracker,
                                                          AssetSource assetSource,
                                                          AssetChecksumGenerator checksumGenerator,
                                                          @Symbol(SymbolConstants.STRICT_CSS_URL_REWRITING) boolean strictCssUrlRewriting)
    {
        return new CSSURLRewriter(delegate, tracker, assetSource, checksumGenerator, strictCssUrlRewriting);
    }

    @Decorate(id = "DisableMinificationForStacks", serviceInterface = StreamableResourceSource.class)
    @Order("before:Minification")
    public StreamableResourceSource setupDisableMinificationByJavaScriptStack(StreamableResourceSource delegate,
                                                                              @Symbol(SymbolConstants.MINIFICATION_ENABLED)
                                                                              boolean enabled,
                                                                              JavaScriptStackSource javaScriptStackSource,
                                                                              Request request)
    {
        return enabled
                ? new JavaScriptStackMinimizeDisabler(delegate, javaScriptStackSource, request)
                : null;
    }

    /**
     * Ensures that all "text/*" assets are given the UTF-8 charset.
     *
     * @since 5.4
     */
    @Decorate(id = "TextUTF8", serviceInterface = StreamableResourceSource.class)
    @Order("after:Cache")
    public StreamableResourceSource setupTextAssetsAsUTF8(StreamableResourceSource delegate)
    {
        return new UTF8ForTextAssets(delegate);
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
     * <dt>svg</dt>
     * <dd>image/svg+xml</dd>
     * <dt>swf</dt>
     * <dd>application/x-shockwave-flash</dd>
     * <dt>woff</dt>
     * <dd>application/font-woff</dd>
     * <dt>tff</dt> <dd>application/x-font-ttf</dd>
     * <dt>eot</dt> <dd>application/vnd.ms-fontobject</dd>
     * </dl>
     */
    @Contribute(ContentTypeAnalyzer.class)
    public void setupDefaultContentTypeMappings(MappedConfiguration<String, String> configuration)
    {
        configuration.add("css", "text/css");
        configuration.add("js", "text/javascript");
        configuration.add("gif", "image/gif");
        configuration.add("jpg", "image/jpeg");
        configuration.add("jpeg", "image/jpeg");
        configuration.add("png", "image/png");
        configuration.add("swf", "application/x-shockwave-flash");
        configuration.add("svg", "image/svg+xml");
        configuration.add("woff", "application/font-woff");
        configuration.add("ttf", "application/x-font-ttf");
        configuration.add("eot", "application/vnd.ms-fontobject");
    }

    /**
     * Disables compression for the following content types:
     * <ul>
     * <li>image/jpeg</li>
     * <li>image/gif</li>
     * <li>image/png</li>
     * <li>image/svg+xml</li>
     * <li>application/x-shockwave-flash</li>
     * <li>application/font-woff</li>
     * <li>application/x-font-ttf</li>
     * <li>application/vnd.ms-fontobject</li>
     * </ul>
     */
    @Contribute(CompressionAnalyzer.class)
    public void disableCompressionForImageTypes(MappedConfiguration<String, Boolean> configuration)
    {
        configuration.add("image/*", false);
        configuration.add("image/svg+xml", true);
        configuration.add("application/x-shockwave-flash", false);
        configuration.add("application/font-woff", false);
        configuration.add("application/x-font-ttf", false);
        configuration.add("application/vnd.ms-fontobject", false);
    }

    @Marker(ContextProvider.class)
    public static AssetFactory buildContextAssetFactory(ApplicationGlobals globals,
                                                        AssetPathConstructor assetPathConstructor,
                                                        ResponseCompressionAnalyzer compressionAnalyzer,
                                                        ResourceChangeTracker resourceChangeTracker,
                                                        StreamableResourceSource streamableResourceSource)
    {
        return new ContextAssetFactory(compressionAnalyzer, resourceChangeTracker, streamableResourceSource, assetPathConstructor, globals.getContext());
    }

    @Contribute(ClasspathAssetAliasManager.class)
    public static void addApplicationAndTapestryMappings(MappedConfiguration<String, String> configuration,

                                                         @Symbol(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
                                                         String appPackage)
    {
        configuration.add("tapestry", "org/apache/tapestry5");

        configuration.add("app", toPackagePath(appPackage));
    }

    /**
     * Contributes an handler for each mapped classpath alias, as well handlers for context assets
     * and stack assets (combined {@link org.apache.tapestry5.services.javascript.JavaScriptStack} files).
     */
    @Contribute(Dispatcher.class)
    @AssetRequestDispatcher
    public static void provideBuiltinAssetDispatchers(MappedConfiguration<String, AssetRequestHandler> configuration,

                                                      @ContextProvider
                                                      AssetFactory contextAssetFactory,

                                                      @Autobuild
                                                      StackAssetRequestHandler stackAssetRequestHandler,

                                                      ClasspathAssetAliasManager classpathAssetAliasManager,
                                                      ResourceStreamer streamer,
                                                      AssetSource assetSource,
                                                      ClasspathAssetProtectionRule classpathAssetProtectionRule)
    {
        Map<String, String> mappings = classpathAssetAliasManager.getMappings();

        for (String folder : mappings.keySet())
        {
            String path = mappings.get(folder);

            configuration.add(folder, new ClasspathAssetRequestHandler(streamer, assetSource, path, classpathAssetProtectionRule));
        }

        configuration.add(RequestConstants.CONTEXT_FOLDER,
                new ContextAssetRequestHandler(streamer, contextAssetFactory.getRootResource()));

        configuration.add(RequestConstants.STACK_FOLDER, stackAssetRequestHandler);

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

    private static String toPackagePath(String packageName)
    {
        return packageName.replace('.', '/');
    }

    /**
     * Contributes:
     * <dl>
     * <dt>ClientLocalization</dt>
     * <dd>A virtual resource of formatting symbols for decimal numbers</dd>
     * <dt>Core</dt>
     * <dd>Built in messages used by Tapestry's default validators and components</dd>
     * <dt>AppCatalog</dt>
     * <dd>The Resource defined by {@link SymbolConstants#APPLICATION_CATALOG}</dd>
     * <dt>
     *     </dl>
     *
     * @since 5.2.0
     */
    @Contribute(ComponentMessagesSource.class)
    public static void setupGlobalMessageCatalog(AssetSource assetSource,
                                                 @Symbol(SymbolConstants.APPLICATION_CATALOG)
                                                 Resource applicationCatalog, OrderedConfiguration<Resource> configuration)
    {
        configuration.add("ClientLocalization", new ClientLocalizationMessageResource());
        configuration.add("Core", assetSource.resourceForPath("org/apache/tapestry5/core.properties"));
        configuration.add("AppCatalog", applicationCatalog);
    }

    @Contribute(Dispatcher.class)
    @Primary
    public static void setupAssetDispatch(OrderedConfiguration<Dispatcher> configuration,
                                          @AssetRequestDispatcher
                                          Dispatcher assetDispatcher)
    {

        // This goes first because an asset to be streamed may have an file
        // extension, such as
        // ".html", that will confuse the later dispatchers.

        configuration.add("Asset", assetDispatcher, "before:ComponentEvent");
    }
    
    @Primary
    public static ClasspathAssetProtectionRule buildClasspathAssetProtectionRule(
            List<ClasspathAssetProtectionRule> rules, ChainBuilder chainBuilder)
    {
        return chainBuilder.build(ClasspathAssetProtectionRule.class, rules);
    }
    
    public static void contributeClasspathAssetProtectionRule(
            OrderedConfiguration<ClasspathAssetProtectionRule> configuration) 
    {
        ClasspathAssetProtectionRule classFileRule = (s) -> s.toLowerCase().endsWith(".class");
        configuration.add("ClassFile", classFileRule);
        ClasspathAssetProtectionRule propertiesFileRule = (s) -> s.toLowerCase().endsWith(".properties");
        configuration.add("PropertiesFile", propertiesFileRule);
        ClasspathAssetProtectionRule xmlFileRule = (s) -> s.toLowerCase().endsWith(".xml");
        configuration.add("XMLFile", xmlFileRule);
        ClasspathAssetProtectionRule folderRule = (s) -> isFolderToBlock(s);
        configuration.add("Folder", folderRule);
    }
    
    final private static boolean isFolderToBlock(String path) 
    {
        path = path.replace('\\', '/');
        final int lastIndex = path.lastIndexOf('/');
        if (lastIndex >= 0)
        {
            path = path.substring(lastIndex);
        }
        return !path.contains(".");
    }
    
}
