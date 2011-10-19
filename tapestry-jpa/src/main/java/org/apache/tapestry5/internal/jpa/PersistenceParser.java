// Copyright 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.jpa;

import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.List;

public class PersistenceParser
{

    public List<TapestryPersistenceUnitInfo> parse(final InputStream stream)
    {

        final PersistenceContentHandler handler = new PersistenceContentHandler();

        try
        {
            final XMLReader reader = XMLReaderFactory.createXMLReader();

            reader.setContentHandler(handler);

            reader.parse(new InputSource(stream));

            return handler.getPersistenceUnits();

        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            InternalUtils.close(stream);
        }
    }

}
