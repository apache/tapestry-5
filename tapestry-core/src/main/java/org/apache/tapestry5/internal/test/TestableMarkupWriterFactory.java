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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.services.MarkupWriterFactory;

/**
 * Extension of {@link MarkupWriterFactory} that tracks the most recently created markup writer so that it can be
 * accessed after the page has rendered.
 */
public interface TestableMarkupWriterFactory extends MarkupWriterFactory
{
    /**
     * Returns the most recently created markup writer.
     */
    MarkupWriter getLatestMarkupWriter();
}
