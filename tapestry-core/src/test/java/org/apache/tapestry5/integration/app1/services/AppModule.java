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
package org.apache.tapestry5.integration.app1.services;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.integration.app1.data.Address;
import org.apache.tapestry5.integration.app1.data.Entity;
import org.apache.tapestry5.integration.app1.data.ToDoItem;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.internal.services.GenericValueEncoderFactory;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Value;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.modules.Bootstrap4Module;
import org.apache.tapestry5.modules.NoBootstrapModule;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.EditBlockContribution;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.ResourceDigestGenerator;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.ValueLabelProvider;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;
import org.apache.tapestry5.services.pageload.PagePreloader;
import org.apache.tapestry5.services.pageload.PreloaderMode;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.slf4j.Logger;

/**
 * I was just dying to see how fast requests are!
 */
//@ImportModule(Bootstrap4Module.class)
//@ImportModule(NoBootstrapModule.class)
public class AppModule
{

    final public static String D3_URL_SYMBOL = "d3.url";

    /**
     * Used to disambiguate services in this module from services in other modules that share the
     * same service
     * interface.
     */
    @Target(
            {PARAMETER, FIELD})
    @Retention(RUNTIME)
    @Documented
    public @interface Local
    {

    }

    public static void bind(ServiceBinder binder)
    {
        binder.bind(Reloadable.class);
        binder.bind(MessageAccess.class);
    }

    public static void contributeValidatorMacro(MappedConfiguration<String, String> configuration)
    {
        configuration.add("password", "required,lengthBetweenTwoAndThree");
        configuration.add("lengthBetweenTwoAndThree", "minlength=2,maxlength=3");
    }

    @Contribute(ServiceOverride.class)
    public void setupCustomBaseURLSource(MappedConfiguration<Class, Object> configuration)
    {
        BaseURLSource source = new BaseURLSource()
        {
            public String getBaseURL(boolean secure)
            {
                String protocol = secure ? "https" : "http";

                // This is all a bit jury-rigged together. This is for running the app
                // interactively; Selenium doesn't seem to handle the transition to https.
                int port = secure ? 8443 : 9090;

                return String.format("%s://localhost:%d", protocol, port);
            }
        };

        configuration.add(BaseURLSource.class, source);
    }

    @Marker(Local.class)
    public RequestFilter buildTimingFilter(final Logger log)
    {
        return new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler) throws IOException
            {
                long startTime = System.nanoTime();

                try
                {
                    return handler.service(request, response);
                } finally
                {
                    long elapsed = System.nanoTime() - startTime;

                    log.info(String.format("Request time: %5.2f s -- %s", elapsed * 10E-10d, request.getPath()));
                }
            }
        };
    }

    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,

                                         @Local
                                         RequestFilter filter)
    {
        configuration.add("Timing", filter);
    }

    public UserAuthenticator buildUserAuthenticator()
    {
        return new UserAuthenticator()
        {
            public boolean isValid(String userName, String plaintextPassword)
            {
                return plaintextPassword.equals("tapestry");
            }
        };
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en,fr,de");
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, false);
        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, false);
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, false);

        configuration.add(SymbolConstants.SECURE_ENABLED, true);

        configuration.add("app.injected-symbol", "Symbol contributed to ApplicationDefaults");

        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "testing, testing, 1... 2... 3...");

        // This is the emphasis of testing at this point.
        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");

        configuration.add(D3_URL_SYMBOL, "cdnjs.cloudflare.com/ajax/libs/d3/3.0.0/d3.js");
        configuration.add(SymbolConstants.PRELOADER_MODE, PreloaderMode.ALWAYS);
//        configuration.add(SymbolConstants.ERROR_CSS_CLASS, "yyyy");
//        configuration.add(SymbolConstants.DEFAULT_STYLESHEET, "classpath:/org/apache/tapestry5/integration/app1/app1.css");
    }

    public static void contributeIgnoredPathsFilter(Configuration<String> configuration)
    {
        configuration.add("/unreachable");
    }

    public ToDoDatabase buildToDoDatabase()
    {
        return new ToDoDatabaseImpl();
    }

    public MusicLibrary buildMusicLibrary(Logger log)
    {
        URL library = getClass().getResource("iTunes.xml");

        final List<Track> tracks = new MusicLibraryParser(log).parseTracks(library);

        final Map<Long, Track> idToTrack = CollectionFactory.newMap();

        for (Track t : tracks)
        {
            idToTrack.put(t.getId(), t);
        }

        return new MusicLibrary()
        {
            public Track getById(long id)
            {
                Track result = idToTrack.get(id);

                if (result != null)
                    return result;

                throw new IllegalArgumentException(String.format("No track with id #%d.", id));
            }

            public List<Track> getTracks()
            {
                return tracks;
            }

            public List<Track> findByMatchingTitle(String title)
            {
                String titleLower = title.toLowerCase();

                List<Track> result = CollectionFactory.newList();

                for (Track t : tracks)
                {
                    if (t.getTitle().toLowerCase().contains(titleLower))
                        result.add(t);
                }

                return result;
            }
        };
    }

    @Marker(French.class)
    public Greeter buildFrenchGreeter()
    {
        return new Greeter()
        {
            public String getGreeting()

            {
                return "Bonjour!";
            }
        };
    }

    public Greeter buildDefaultGreeter()
    {
        return new Greeter()
        {
            public String getGreeting()

            {
                return "Hello";
            }
        };
    }

    public static void contributeValueEncoderSource(MappedConfiguration<Class, ValueEncoderFactory> configuration,
                                                    final MusicLibrary library, final ToDoDatabase todoDatabase)
    {
        ValueEncoder<Track> trackEncoder = new ValueEncoder<Track>()
        {
            public String toClient(Track value)
            {
                return Long.toString(value.getId());
            }

            public Track toValue(String clientValue)
            {
                long id = Long.parseLong(clientValue);

                return library.getById(id);
            }
        };

        configuration.add(Track.class, GenericValueEncoderFactory.create(trackEncoder));

        ValueEncoder<ToDoItem> todoEncoder = new ValueEncoder<ToDoItem>()
        {
            public String toClient(ToDoItem value)
            {
                return String.valueOf(value.getId());
            }

            public ToDoItem toValue(String clientValue)
            {
                long id = Long.parseLong(clientValue);

                return todoDatabase.get(id);
            }
        };

        configuration.add(ToDoItem.class, GenericValueEncoderFactory.create(todoEncoder));

        final ValueEncoder<Entity> encoder = new ValueEncoder<Entity>()
        {
            public String toClient(Entity value)
            {
                return value.getId();
            }

            public Entity toValue(String clientValue)
            {
                Entity entity = new Entity();
                entity.setId(clientValue);
                entity.setLabel("label" + clientValue);
                return entity;
            }
        };

        ValueEncoderFactory<Entity> valueEncoderFactory = new ValueEncoderFactory<Entity>()
        {

            public ValueEncoder<Entity> create(Class<Entity> type)
            {
                return encoder;
            }
        };

        configuration.add(Entity.class, valueEncoderFactory);
    }

    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker2> configuration)
    {
        configuration.add("ReverseStringsWorker", new ReverseStringsWorker());
    }

    public static void contributeComponentMessagesSource(@Value("context:WEB-INF/pre-app.properties")
                                                         Resource preappResource, OrderedConfiguration<Resource> configuration)
    {
        configuration.add("PreApp", preappResource, "before:AppCatalog");
    }

    // Remove this in 5.5:
    @Contribute(ResourceDigestGenerator.class)
    public static void protectPropertiesFiles(Configuration<String> configuration)
    {
        configuration.add("properties");
    }

    public void contributeValueLabelProvider(MappedConfiguration<Class, ValueLabelProvider> configuration)
    {
        configuration.add(Track.class, new ValueLabelProvider<Track>()
        {

            public String getLabel(Track value)
            {
                return value.getTitle();
            }
        });
    }

    @Contribute(ComponentClassResolver.class)
    public static void setupAlphaLibrary(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("lib/alpha", "org.apache.tapestry5.integration.locallib.alpha"));
    }

    @Contribute(ClientWhitelist.class)
    public static void provideWhitelistAnalyzer(OrderedConfiguration<WhitelistAnalyzer> configuration)
    {
        configuration.add("TestAnalyzer", new WhitelistAnalyzer()
        {

            public boolean isRequestOnWhitelist(Request request)
            {
                return true;
            }
        }, "before:*");
    }

    @Contribute(PagePreloader.class)
    public static void setupPagePreload(Configuration<String> configuration)
    {
        configuration.add("index");
        configuration.add("core/exceptionreport");
        configuration.add("core/t5dashboard");
    }
    

	public static void contributeDefaultDataTypeAnalyzer(
			@SuppressWarnings("rawtypes") MappedConfiguration<Class, String> configuration) 
	{
    	configuration.add(Address.class, "address");
    }

	@Contribute(BeanBlockSource.class)
    public static void provideDefaultBeanBlocks(Configuration<BeanBlockContribution> configuration) 
	{
		configuration.add( new EditBlockContribution("address", "PropertyEditBlocks", "object"));
	}

}
