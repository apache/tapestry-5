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

package org.apache.tapestry5.internal.webresources;

import com.github.sommeri.less4j.LessSource;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.ResourceDependencies;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ResourceLessSource extends LessSource
{
    private final Resource resource;

    private final ResourceDependencies dependencies;

    ResourceLessSource(Resource resource, ResourceDependencies dependencies)
    {
        this.resource = resource;
        this.dependencies = dependencies;
    }

    @Override
    public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile, StringSourceException
    {
        Resource relative = resource.forFile(filename);

        if (!relative.exists())
        {
            throw new FileNotFound();
        }

        dependencies.addDependency(relative);

        return new ResourceLessSource(relative, dependencies);
    }

    @Override
    public String getContent() throws FileNotFound, CannotReadFile
    {
        // Adapted from Less's URLSource
        Reader input = null;
        try
        {
            input = new InputStreamReader(resource.openStream());
            String content = IOUtils.toString(input).replace("\r\n", "\n");

            return content;
        } catch (FileNotFoundException ex)
        {
            throw new FileNotFound();
        } catch (IOException ex)
        {
            throw new CannotReadFile();
        } finally
        {
            InternalUtils.close(input);
        }
    }

    @Override
    public byte[] getBytes() throws FileNotFound, CannotReadFile
    {
        Reader input = null;
        try
        {
            input = new InputStreamReader(resource.openStream());

            return IOUtils.toByteArray(input);
        } catch (FileNotFoundException ex)
        {
            throw new FileNotFound();
        } catch (IOException ex)
        {
            throw new CannotReadFile();
        } finally
        {
            InternalUtils.close(input);
        }

    }
}
