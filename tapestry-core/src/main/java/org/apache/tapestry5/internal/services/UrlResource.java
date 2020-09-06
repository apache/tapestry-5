// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.util.VirtualResource;

public class UrlResource extends VirtualResource {
    
    final public static URL PLACEHOLDER_URL;
    
    static {
        try
        {
            PLACEHOLDER_URL = new URL("http://tapestry.apache.org");
        }
        catch (MalformedURLException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    final private URL url;
    
    private Boolean exists;
    
    private InputStream inputStream;

    public UrlResource()
    {
        this(PLACEHOLDER_URL);
        exists = true;
    }
    
    public UrlResource(URL url)
    {
        super();
        this.url = url;
    }

    @Override
    public InputStream openStream() throws IOException
    {
        if (exists == null) 
        {
            try
            {
                inputStream = url.openStream();
                exists = true;
            }
            catch (IOException e)
            {
                exists = false;
                throw e;
            }
        }
        return inputStream;
    }

    @Override
    public URL toURL()
    {
        return url;
    }

    @Override
    public boolean exists()
    {
        if (exists == null)
        {
            try
            {
                openStream();
            }
            catch (IOException e)
            {
                // openStream() will always set the exists field.
            }
        }
        return exists;
    }

    @Override
    public Resource forLocale(Locale locale)
    {
        return this;
    }

    @Override
    public String toString()
    {
        return "UrlResource [url=" + url + "]";
    }

}
