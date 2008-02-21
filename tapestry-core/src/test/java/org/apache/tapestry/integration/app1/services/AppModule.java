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

package org.apache.tapestry.integration.app1.services;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.integration.app1.data.Track;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Marker;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.*;
import org.apache.tapestry.test.JettyRunner;
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
                long startTime = System.currentTimeMillis();

                try
                {
                    return handler.service(request, response);
                }
                finally
                {
                    long elapsed = System.currentTimeMillis() - startTime;

                    log.info(String.format("Request time: %d ms", elapsed));
                }
            }
        };
    }

    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,

                                         @Local RequestFilter filter)
    {
        configuration.add("Timing", filter);
    }

    public void contributeClasspathAssetAliasManager(MappedConfiguration<String, String> configuration)
    {
        configuration.add("app1/", "org/apache/tapestry/integration/app1/");
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
        configuration.add(TapestryConstants.SUPPORTED_LOCALES_SYMBOL, "en,fr");
        configuration.add(TapestryConstants.PRODUCTION_MODE_SYMBOL, "false");
        configuration.add(TapestryConstants.COMPRESS_WHITESPACE_SYMBOL, "false");

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

        return new MusicLibrary()
        {
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
}
