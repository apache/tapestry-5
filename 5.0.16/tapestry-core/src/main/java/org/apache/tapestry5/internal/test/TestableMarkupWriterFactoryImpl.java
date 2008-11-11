// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.MarkupWriterFactory;

@Scope(ScopeConstants.PERTHREAD)
public class TestableMarkupWriterFactoryImpl implements TestableMarkupWriterFactory
{
    private final MarkupWriterFactory delegate;

    private MarkupWriter lastCreated;

    /**
     * Using Core annotation to reference to framework-provided version, which this implementation wraps around.
     */
    public TestableMarkupWriterFactoryImpl(@Core MarkupWriterFactory delegate)
    {
        this.delegate = delegate;
    }

    public MarkupWriter getLatestMarkupWriter()
    {
        return lastCreated;
    }

    public MarkupWriter newMarkupWriter(ContentType contentType)
    {
        return save(delegate.newMarkupWriter(contentType));
    }

    public MarkupWriter newPartialMarkupWriter(ContentType contentType)
    {
        return save(delegate.newPartialMarkupWriter(contentType));
    }

    public MarkupWriter newMarkupWriter(String pageName)
    {
        return save(delegate.newMarkupWriter(pageName));
    }

    private MarkupWriter save(MarkupWriter writer)
    {
        lastCreated = writer;

        return writer;
    }
}
