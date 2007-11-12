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

/**
 * Responsible for setup and cleanup of the rendering of a page. The implementation of this is based
 * on an ordered list of {@link PageRenderCommand}s.
 */
public interface PageRenderInitializer
{
    /**
     * Perform any initial setup, by invoking {@link PageRenderCommand#setup(Environment)}.
     * Execution occurs in normal order.
     *
     * @param writer the markup writer that will be used to generate all output markup
     */
    void setup(MarkupWriter writer);

    /**
     * Peform any post-render cleanup, by invoking {@link PageRenderCommand#cleanup(Environment)}.
     * Execution order is reversed.
     *
     * @param writer the markup writer used to generate all output markup in the document
     */
    void cleanup(MarkupWriter writer);
}
