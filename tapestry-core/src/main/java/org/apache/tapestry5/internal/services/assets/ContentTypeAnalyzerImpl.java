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

import java.util.Map;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.services.assets.ContentTypeAnalyzer;

public class ContentTypeAnalyzerImpl implements ContentTypeAnalyzer
{
    private final Context context;

    private final Map<String, String> configuration;

    public ContentTypeAnalyzerImpl(Context context, Map<String, String> configuration)
    {
        this.context = context;
        this.configuration = configuration;
    }

    public String getContentType(Resource resource)
    {
        String extension = TapestryInternalUtils.toFileSuffix(resource.getFile());

        String contentType = configuration.get(extension);

        if (contentType != null)
            return contentType;

        contentType = context.getMimeType(resource.getFile());

        if (contentType != null)
            return contentType;

        return "application/octet-stream";
    }

}
