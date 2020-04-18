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

import org.apache.tapestry5.ioc.services.ExceptionAnalysis;

import java.io.PrintWriter;

/**
 * Used by the default {@link org.apache.tapestry5.services.ExceptionReporter} implementation to convert an exception into
 * a stream of text that can be stored to a file. Other applications include sending the text via e-mail or other messaging
 * service.
 *
 * @since 5.4
 */
public interface ExceptionReportWriter
{
    /**
     * Analyzes the exception (using the {@link org.apache.tapestry5.ioc.services.ExceptionAnalyzer} service)
     * and then writes the result to the writer.
     * @param writer the PrintWriter to write to, not null
     * @param exception the exception to look at, possibly null
     */
    void writeReport(PrintWriter writer, Throwable exception);

    /**
     * Writes the analyzed exception to the writer.
     * @param writer the PrintWriter to write to, not null
     * @param exception the exception to look at, possibly null
     */
    void writeReport(PrintWriter writer, ExceptionAnalysis exception);
}
