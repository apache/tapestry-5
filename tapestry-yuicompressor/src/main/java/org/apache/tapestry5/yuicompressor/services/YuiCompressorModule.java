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

package org.apache.tapestry5.yuicompressor.services;

import org.apache.tapestry5.internal.yuicompressor.JavaScriptResourceMinimizer;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.assets.ResourceMinimizer;

import com.yahoo.platform.yui.compressor.YUICompressor;

/**
 * Sets up Tapestry to compress JavaScript assets using {@link YUICompressor}.
 * 
 * @since 5.3.0
 */
public class YuiCompressorModule
{
    @Contribute(ResourceMinimizer.class)
    @Primary
    public static void setupJavaScriptMinimizer(MappedConfiguration<String, ResourceMinimizer> configuration)
    {
        configuration.addInstance("text/javascript", JavaScriptResourceMinimizer.class);
    }
}
