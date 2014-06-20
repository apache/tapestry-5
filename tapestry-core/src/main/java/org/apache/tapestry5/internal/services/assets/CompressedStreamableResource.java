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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZip compressed representation of a {@link StreamableResource}.
 *
 * @since 5.4
 */
public class CompressedStreamableResource extends StreamableResourceImpl
{
    public CompressedStreamableResource(StreamableResource base, AssetChecksumGenerator assetChecksumGenerator) throws IOException
    {
        super(base.getDescription(), base.getContentType(), CompressionStatus.COMPRESSED, base.getLastModified(), compressContent(base), assetChecksumGenerator, base.getResponseCustomizer());

        assert base.getCompression() == CompressionStatus.COMPRESSABLE;
    }

    private static BytestreamCache compressContent(StreamableResource resource) throws IOException
    {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream(resource.getSize());
        OutputStream compressor = new BufferedOutputStream(new GZIPOutputStream(compressed));

        resource.streamTo(compressor);

        compressor.close();

        return new BytestreamCache(compressed);
    }
}
