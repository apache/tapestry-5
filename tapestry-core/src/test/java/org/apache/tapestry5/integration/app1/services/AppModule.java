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

package org.apache.tapestry5.integration.app1.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.integration.app1.data.ToDoItem;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.internal.services.GenericValueEncoderFactory;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.JettyRunner;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * I was just dying to see how fast requests are!
 */
public class AppModule
{
    /**
     * Used to disambiguate services in this module from services in other modules that share the same service
     * interface.
     */
    @Target(
            { PARAMETER, FIELD })
    @Retention(RUNTIME)
    @Documented
    public @interface Local
    {

    }

    public void contributeAlias(Configuration<AliasContribution> configuration)
    {
        BaseURLSource source = new BaseURLSource()
        {
            public String getBaseURL(boolean secure)
            {
                String protocol = secure ? "https" : "http";

                // This is all a bit jury-rigged together.  This is for running the app
                // interactively; Selenium doesn't seem to handle the transition to https.
                int port = secure ? JettyRunner.DEFAULT_SECURE_PORT : JettyRunner.DEFAULT_PORT;

                return String.format("%s://localhost:%d", protocol, port);
            }
        };

        configuration.add(AliasContribution.create(BaseURLSource.class, source));
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
                }
                finally
                {
                    long elapsed = System.nanoTime() - startTime;

                    log.info(String.format("Request time: %5.2f s -- %s", elapsed * 10E-10d, request.getPath()));
                }
            }
        };
    }

    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,

                                         @Local RequestFilter filter)
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

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en,fr,de");
        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, "false");
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, "true");

        configuration.add(SymbolConstants.SECURE_ENABLED, "true");

        configuration.add("app.injected-symbol", "Symbol contributed to ApplicationDefaults");
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

                if (result != null) return result;

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
                    if (t.getTitle().toLowerCase().contains(titleLower)) result.add(t);
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
                                                    final MusicLibrary library,
                                                    final ToDoDatabase todoDatabase)
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
    }


    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration)
    {
        configuration.add("ReverseStringsWorker", new ReverseStringsWorker());
    }
}
