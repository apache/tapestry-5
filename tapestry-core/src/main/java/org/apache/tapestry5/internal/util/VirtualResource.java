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

package org.apache.tapestry5.internal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.tapestry5.commons.Resource;

/**
 * Base class for virtual resources: resources that are not simply mapped to stored files, but are assembled, as necessary,
 * on the fly. This is used inside Tapestry to expose the application's localized message catalog as a module.
 * Subclasses should implement the {@link org.apache.tapestry5.commons.Resource#openStream()} method to return a stream of
 * the contents of the virtual resource.
 *
 * @see org.apache.tapestry5.services.javascript.ModuleManager
 * @see org.apache.tapestry5.internal.services.javascript.ModuleDispatcher
 * @since 5.4
 */
public abstract class VirtualResource implements Resource
{
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    private <T> T unsupported(String name)
    {
        throw new UnsupportedOperationException(String.format("Method %s() is not supported for a VirtualResource.", name));
    }

    public boolean exists()
    {

        return true;
    }

    public URL toURL()
    {
        return unsupported("toURL");
    }

    public Resource forLocale(Locale locale)
    {
        return unsupported("forLocale");
    }

    public Resource forFile(String relativePath)
    {
        return this;
    }

    public Resource withExtension(String extension)
    {
        return unsupported("withExtension");
    }

    public String getFolder()
    {
        return unsupported("getFolder");
    }

    public String getFile()
    {
        return unsupported("getFile");
    }

    public String getPath()
    {
        return unsupported("getPath");
    }

    protected InputStream toInputStream(String content) throws IOException
    {
        return toInputStream(content.getBytes(UTF8));
    }

    protected InputStream toInputStream(byte[] content) throws IOException
    {
        return new ByteArrayInputStream(content);
    }

    @Override
    public boolean isVirtual()
    {
        return true;
    }
}
