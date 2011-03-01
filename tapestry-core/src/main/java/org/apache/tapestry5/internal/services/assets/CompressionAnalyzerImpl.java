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

import org.apache.tapestry5.services.assets.CompressionAnalyzer;

public class CompressionAnalyzerImpl implements CompressionAnalyzer
{
    private final Map<String, Boolean> configuration;

    public CompressionAnalyzerImpl(Map<String, Boolean> configuration)
    {
        this.configuration = configuration;
    }

    public boolean isCompressable(String contentType)
    {
        assert contentType != null;

        int x = contentType.indexOf(';');

        String key = x < 0 ? contentType : contentType.substring(0, x);

        Boolean result = configuration.get(key);

        return result == null ? true : result.booleanValue();
    }
}
