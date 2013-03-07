// Copyright 2013 The Apache Software Foundation
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

import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.ioc.Resource;

import java.io.IOException;

/**
 * Utility used by {@link org.apache.tapestry5.services.assets.AssetRequestHandler} implementations
 * where the first folder in the extra path is actually a computed checksum of the resource's content.
 *
 * @since 5.4
 */
public class ChecksumPath {
  public final String checksum;

  public final String resourcePath;

  private final ResourceStreamer streamer;

  public ChecksumPath(ResourceStreamer streamer, String baseFolder, String extraPath) {
    this.streamer = streamer;
    int slashx = extraPath.indexOf('/');

    checksum = extraPath.substring(0, slashx);

    String morePath = extraPath.substring(slashx + 1);

    resourcePath = baseFolder == null
        ? morePath
        : baseFolder + "/" + morePath;
  }

  public boolean stream(Resource resource) throws IOException {
    if (resource == null) {
      return false;
    }

    // TODO: Handle incorrect checksum ... maybe with a redirect?

    streamer.streamResource(resource);

    return true;
  }
}
