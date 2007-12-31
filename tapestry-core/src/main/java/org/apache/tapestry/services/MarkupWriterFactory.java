// Copyright 2006, 2007 The Apache Software Foundation
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
// See the License for the specific language governing permissions andand
// limitations under the License.

package org.apache.tapestry.services;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.util.ContentType;

/**
 * Source for {@link org.apache.tapestry.MarkupWriter} instances.
 */
public interface MarkupWriterFactory
{
    /**
     * This will likely expand in the future to control things like output format and encoding, etc.
     *
     * @param contentType
     */
    MarkupWriter newMarkupWriter(ContentType contentType);
}
