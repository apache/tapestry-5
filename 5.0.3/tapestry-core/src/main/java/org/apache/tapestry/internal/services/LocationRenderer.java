// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Set;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.ObjectRenderer;

/**
 * Responsible for rendering a {@link Location}. It is designed to only perform the full output
 * (which includes a snippet of the source file) once per render. This requires the use of the
 * "perthread" model (since the service tracks, internally,
 */
public class LocationRenderer implements ObjectRenderer<Location>
{
    private static final int RANGE = 5;

    private final Set<Location> _rendered = newSet();

    public void render(Location location, MarkupWriter writer)
    {
        writer.write(location.toString());

        /** If the full details were already rendered this request, then skip the rest. */
        if (_rendered.contains(location))
            return;

        _rendered.add(location);

        Resource r = location.getResource();
        int line = location.getLine();

        // No line number? then nothing more to render.

        if (line <= 0)
            return;

        URL url = r.toURL();

        if (url == null)
            return;

        int start = line - RANGE;
        int end = line + RANGE;

        writer.element("table", "class", "t-location-outer");

        LineNumberReader reader = null;

        try
        {
            InputStream is = new BufferedInputStream(url.openStream());
            InputStreamReader isr = new InputStreamReader(is);
            reader = new LineNumberReader(new BufferedReader(isr));

            while (true)
            {
                String input = reader.readLine();

                if (input == null)
                    break;

                int current = reader.getLineNumber();

                if (current < start)
                    continue;

                if (current > end)
                    break;

                writer.element("tr");

                writer.element("td");
                writer.attributes("class", line == current ? "t-location-line t-location-current"
                        : "t-location-line");
                writer.write(Integer.toString(current));
                writer.end();

                String css = "t-location-content";
                if (line == current)
                    css += " t-location-current";
                if (start == current)
                    css += " t-location-content-first";

                writer.element("td");
                writer.attributes("class", css);
                writer.write(input);
                writer.end();

                writer.end(); // tr
            }

            reader.close();
            reader = null;
        }
        catch (IOException ex)
        {
            writer.write(ex.toString());
        }
        finally
        {
            TapestryInternalUtils.close(reader);
        }

        writer.end(); // div
    }

}
