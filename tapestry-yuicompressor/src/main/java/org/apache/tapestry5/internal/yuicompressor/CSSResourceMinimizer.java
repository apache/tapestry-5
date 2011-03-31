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

package org.apache.tapestry5.internal.yuicompressor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.tapestry5.ioc.OperationTracker;
import org.slf4j.Logger;

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * Uses {@link CssCompressor} to reduce the size of CSS content.
 * 
 * @since 5.3.0
 */
public class CSSResourceMinimizer extends AbstractMinimizer
{
    public CSSResourceMinimizer(Logger logger, OperationTracker tracker)
    {
        super(logger, tracker, "CSS");
    }

    @Override
    protected void doMinimize(Reader input, Writer output) throws IOException
    {
        new CssCompressor(input).compress(output, -1);
    }

}
