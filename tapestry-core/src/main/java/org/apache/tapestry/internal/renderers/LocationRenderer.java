// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.renderers;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.internal.TapestryInternalUtils;
import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.annotations.Scope;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;
import org.apache.tapestry.services.ObjectRenderer;

import java.io.*;
import java.net.URL;
import java.util.Set;

/**
 * Responsible for rendering a {@link Location}. It is designed to only perform the full output
 * (which includes a snippet of the source file) once per render. This requires the use of the
 * "perthread" scope (since the service tracks, internally, which locations have already been
 * rendered, to avoid repetition).
 */
@Scope(PERTHREAD_SCOPE)
public class LocationRenderer implements ObjectRenderer<Location>
{
    private static final int RANGE = 5;

    private final Set<Location> _rendered = newSet();

    public void render(Location location, MarkupWriter writer)
    {
        writer.write(location.toString());

        /** If the full details were already rendered this request, then skip the rest. */
        if (_rendered.contains(location)) return;

        _rendered.add(location);

        Resource r = location.getResource();
        int line = location.getLine();

        // No line number? then nothing more to render.

        if (line <= 0) return;

        URL url = r.toURL();

        if (url == null) return;

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

                if (input == null) break;

                int current = reader.getLineNumber();

                if (current < start) continue;

                if (current > end) break;

                writer.element("tr");

                writer.element("td", "class", "t-location-line");

                if (line == current) writer.getElement().addClassName("t-location-current");

                writer.write(Integer.toString(current));
                writer.end();

                Element td = writer.element("td", "class", "t-location-content");

                if (line == current) td.addClassName("t-location-current");

                if (start == current) td.addClassName("t-location-content-first");

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
