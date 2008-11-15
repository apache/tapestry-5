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

package org.apache.tapestry5.internal.renderers;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ObjectRenderer;

import java.io.*;
import java.util.Set;

/**
 * Responsible for rendering a {@link Location}. It is designed to only perform the full output (which includes a
 * snippet of the source file) once per render. This requires the use of the "perthread" scope (since the service
 * tracks, internally, which locations have already been rendered, to avoid repetition).
 */
@Scope(ScopeConstants.PERTHREAD)
public class LocationRenderer implements ObjectRenderer<Location>
{
    private static final int RANGE = 5;

    private final Set<Location> rendered = CollectionFactory.newSet();

    public void render(Location location, MarkupWriter writer)
    {
        writer.write(location.toString());

        /** If the full details were already rendered this request, then skip the rest. */
        if (rendered.contains(location)) return;

        rendered.add(location);

        Resource r = location.getResource();
        int line = location.getLine();

        // No line number? then nothing more to render.

        if (line <= 0) return;

        if (!r.exists()) return;


        int start = line - RANGE;
        int end = line + RANGE;

        writer.element("table", "class", "t-location-outer");

        LineNumberReader reader = null;

        try
        {
            InputStream is = r.openStream();
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
            InternalUtils.close(reader);
        }

        writer.end(); // div
    }
}
