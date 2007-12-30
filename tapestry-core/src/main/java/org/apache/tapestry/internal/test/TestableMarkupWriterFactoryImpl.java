// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.internal.test;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.util.ContentType;
import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.services.Core;
import org.apache.tapestry.services.MarkupWriterFactory;

@Scope(PERTHREAD_SCOPE)
public class TestableMarkupWriterFactoryImpl implements TestableMarkupWriterFactory
{
    private final MarkupWriterFactory _delegate;

    private MarkupWriter _lastCreated;

    /**
     * Using Core annotation to reference to framework-provided version, which this implementation wraps
     * around.
     */
    public TestableMarkupWriterFactoryImpl(@Core MarkupWriterFactory delegate)
    {
        _delegate = delegate;
    }

    public MarkupWriter getLatestMarkupWriter()
    {
        return _lastCreated;
    }

    public MarkupWriter newMarkupWriter(ContentType contentType)
    {
        MarkupWriter result = _delegate.newMarkupWriter(contentType);

        _lastCreated = result;

        return result;
    }
}
