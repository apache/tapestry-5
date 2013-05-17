// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.wro4j.services;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper around a WRO4J {@link ro.isdc.wro.model.resource.processor.ResourcePreProcessor}. This can represent
 * a compilation process (such as CoffeeScript to JavaScript), or a transformation process (such as minimizing
 * JavaScript or CSS).
 *
 * @see ResourceProcessorSource
 * @since 5.4
 */
public interface ResourceProcessor
{
    /**
     * Processes an input stream, producing an output stream.
     *
     * @param operationDescription
     *         used to {@linkplain org.apache.tapestry5.ioc.OperationTracker#perform(String, org.apache.tapestry5.ioc.IOOperation) track the operation}
     * @param inputURL
     *         represents the resource being processed (typically, just used for error reporting)
     * @param input
     *         stream of bytes to process
     * @return processed stream
     * @throws IOException
     */
    InputStream process(String operationDescription, String inputURL, InputStream input) throws IOException;
}
