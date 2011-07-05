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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.services.Response;

/**
 * Indicates how the content inside a {@link StreamableResource} is (potentially) compressed.
 * 
 * @since 5.3
 */
public enum CompressionStatus
{
    /**
     * The content may be compressed but has not yet been compressed. This is true for most text-oriented content types,
     * but not found most image content types.
     */
    COMPRESSABLE,

    /**
     * The content has been compressed, which must be reflected in the {@link Response}'s content encoding.
     */
    COMPRESSED,

    /**
     * The content is not compressable. This is usually the case for image content types, where the native format
     * of the content already includes compression.
     */
    NOT_COMPRESSABLE;
}
