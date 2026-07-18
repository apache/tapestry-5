// Copyright 2010, 2013, 2026 The Apache Software Foundation
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.ContextAssetProtectionRule;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Set;

class ContextAssetRequestHandlerTest
{
    private static final ContextAssetProtectionRule NEVER_BLOCK = (path) -> false;

    private static final ContextAssetProtectionRule BLOCK_MAP_FILES = (path) -> path.toLowerCase().endsWith(".map");

    private static final Resource CONTEXT_ROOT = new ClasspathResource("");

    private static final ResourceStreamer ALWAYS_STREAM = new ResourceStreamer()
    {
        @Override
        public boolean streamResource(Resource resource, String providedChecksum, Set<Options> options)
        {
            return true;
        }

        @Override
        public boolean streamResource(StreamableResource resource, String providedChecksum, Set<Options> options)
        {
            throw new UnsupportedOperationException();
        }
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "web-Inf/classes/hibernate.cfg.xml",
            "Meta-Inf/MANIFEST.mf",
            "Index.tml",
            "folder/FolderIndex.TML",
            "\\WEB-INF/something.jpg",
            "\\//WEB-INF/something.jpg",
            "//WEB-INF/something.jpg",
            "//\\\\WEB-INF/something.jpg"
    })
    void ensureAssetsAreRejected(String path) throws IOException
    {
        // ARRANGE
        ContextAssetRequestHandler handler = new ContextAssetRequestHandler(null, null, NEVER_BLOCK);

        // ACT
        boolean providedResponse = handler.handleAssetRequest(null, null, "fake-checksum/" + path);

        // ASSERT
        assertFalse(providedResponse, "Handler should return false for invalid path.");
    }

    @Test
    void contextAssetProtectionRuleIsConsulted() throws IOException
    {
        // ARRANGE
        ContextAssetRequestHandler handler = new ContextAssetRequestHandler(null, null, BLOCK_MAP_FILES);

        // ACT
        boolean providedResponse = handler.handleAssetRequest(null, null, "fake-checksum/app.js.map");

        // ASSERT
        assertFalse(providedResponse, "Handler should return false when the protection rule blocks the path.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "context-asset-request-handler-test/valid-asset.js",
            "context-asset-request-handler-test/valid-asset.js.map"
    })
    void validAssetsAreStreamedWhenNotBlocked(String path) throws IOException
    {
        // ARRANGE
        ContextAssetRequestHandler handler = new ContextAssetRequestHandler(ALWAYS_STREAM, CONTEXT_ROOT, NEVER_BLOCK);

        // ACT
        boolean providedResponse = handler.handleAssetRequest(null, null, "fake-checksum/" + path);

        // ASSERT
        assertTrue(providedResponse, "Handler should return true for a valid, existing asset that no rule blocks.");
    }
}
