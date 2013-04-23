// Copyright 2011-2013 The Apache Software Foundation
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

package org.apache.tapestry5.yuicompressor.modules;

import com.yahoo.platform.yui.compressor.YUICompressor;
import org.apache.tapestry5.internal.yuicompressor.CSSResourceMinimizer;
import org.apache.tapestry5.internal.yuicompressor.JavaScriptResourceMinimizer;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.assets.ResourceMinimizer;

/**
 * Sets up Tapestry to compress JavaScript assets using {@link YUICompressor}.
 * 
 * @since 5.3
 */
public class YuiCompressorModule
{
    /**
     * Contibutes minimizers for <code>text/javascript</code> and <code>test/css</code>.
     * 
     */
    @Contribute(ResourceMinimizer.class)
    @Primary
    public static void contributeMinimizers(MappedConfiguration<String, ResourceMinimizer> configuration)
    {
        configuration.addInstance("text/javascript", JavaScriptResourceMinimizer.class);
        configuration.addInstance("text/css", CSSResourceMinimizer.class);
    }
}
