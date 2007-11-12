// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.services;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.dom.Document;

/**
 * Page render commands are invoked at the start of the page render cycle and at the end. This
 * allows them to perform initial startup or final cleanup (or both).
 * <p/>
 * When commands are invoked (by the default {@link PageRenderInitializer}), the
 * {@link MarkupWriter} and {@link Document} will have already been set. Most commands deal with
 * storing new environmental services into the Environment.
 *
 * @see PageRenderInitializer
 */
public interface PageRenderCommand
{
    /**
     * Invoked to perform an initial setup.
     */
    void setup(Environment environment);

    /**
     * Invoked to peform final cleanup.
     */
    void cleanup(Environment environment);
}
