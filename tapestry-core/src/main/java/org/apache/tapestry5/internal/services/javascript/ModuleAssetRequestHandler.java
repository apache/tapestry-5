// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.assets.AssetRequestHandler;

import java.io.IOException;

/**
 * Handler contributed to {@link AssetDispatcher} with key "module-root". It interprets the extra path as a module name,
 * and searches for the corresponding JavaScript module.
 */
public class ModuleAssetRequestHandler implements AssetRequestHandler
{
    @Override
    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        return false;
    }
}
