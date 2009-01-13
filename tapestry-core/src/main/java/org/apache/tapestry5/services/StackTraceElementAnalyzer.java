// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Used by {@link org.apache.tapestry5.corelib.components.ExceptionDisplay} to characterize each stack frame that is
 * presented.  Implemented as a chain-of-command service.
 *
 * @since 5.1.0.0
 */
@UsesOrderedConfiguration(StackTraceElementAnalyzer.class)
public interface StackTraceElementAnalyzer
{
    /**
     * Returns the CSS class appropriate to the frame.
     *
     * @param frame stack trace element to be analyzed
     * @return the CSS class name, or null
     */
    String classForFrame(StackTraceElement frame);
}
