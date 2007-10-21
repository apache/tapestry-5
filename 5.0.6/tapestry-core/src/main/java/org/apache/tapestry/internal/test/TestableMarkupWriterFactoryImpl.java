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

import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.services.MarkupWriterFactory;

@Scope(PERTHREAD_SCOPE)
public class TestableMarkupWriterFactoryImpl implements TestableMarkupWriterFactory
{
    private final MarkupWriterFactory _delegate;

    private MarkupWriter _lastCreated;

    public TestableMarkupWriterFactoryImpl(@InjectService("MarkupWriterFactory")
    MarkupWriterFactory delegate)
    {
        _delegate = delegate;
    }

    public MarkupWriter getLatestMarkupWriter()
    {
        return _lastCreated;
    }

    public MarkupWriter newMarkupWriter()
    {
        MarkupWriter result = _delegate.newMarkupWriter();

        _lastCreated = result;

        return result;
    }
}
