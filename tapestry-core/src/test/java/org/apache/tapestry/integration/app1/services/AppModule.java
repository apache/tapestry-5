// Copyright 2006, 2007 The Apache Software Foundation
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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.List;

import org.apache.tapestry.integration.app1.data.Track;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Marker;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;
import org.slf4j.Logger;

/**
 * I was just dying to see how fast requests are!
 */
public class AppModule
{
    /**
     * Used to disambiguate services in this module from services in other modules that share the
     * same service interface.
     */
    @Target(
    { PARAMETER, FIELD })
    @Retention(RUNTIME)
    @Documented
    public @interface Local
    {

    }

    public RequestFilter buildTimingFilter(final Logger log)
    {
        return new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler)
                    throws IOException
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

    @Local
    RequestFilter filter)
    {
        configuration.add("Timing", filter);
    }

    public void contributeClasspathAssetAliasManager(
            MappedConfiguration<String, String> configuration)
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

    public static void contributeApplicationDefaults(
            MappedConfiguration<String, String> configuration)
    {
        configuration.add("tapestry.supported-locales", "en,fr");
        configuration.add("app.injected-symbol", "Symbol contributed to ApplicationDefaults");
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
