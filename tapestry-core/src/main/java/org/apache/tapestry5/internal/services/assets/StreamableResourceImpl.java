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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamableResourceImpl extends BaseStreamableResourceImpl
{
    private final AssetChecksumGenerator assetChecksumGenerator;

    public StreamableResourceImpl(String description, String contentType, CompressionStatus compression, long lastModified,
                                  BytestreamCache bytestreamCache, AssetChecksumGenerator assetChecksumGenerator)
    {
        super(lastModified, description, bytestreamCache, contentType, compression);

        this.assetChecksumGenerator = assetChecksumGenerator;
    }

    public String getChecksum() throws IOException
    {
        // Currently, we rely on AssetChecksumGenerator to manage a cache, but that may be better done
        // here (but must be threadsafe).
        return assetChecksumGenerator.generateChecksum(this);
    }

}
