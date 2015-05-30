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

/**
 * Interface implemented by a page used for reporting exceptions.
 *
 * Alternately,  implemented by services to report request handling exceptions. This is invoked <em>before</em> the exception report page is rendered.
 * The default implementation converts the exception into a well formatted text file, with content similar to the default
 * {@link org.apache.tapestry5.corelib.pages.ExceptionReport} page, and stores this file on the file system.
 *
 * Exception report files are stored beneath a root directory, with intermediate folders for the day (e.g., "2014-06-02"), hour, and minute.
 *
 * Directories are created as necessary; however, there is nothing in place to delete these exceptions reports.
 *
 * @see org.apache.tapestry5.SymbolConstants#EXCEPTION_REPORTS_DIR
 * @see org.apache.tapestry5.services.RequestExceptionHandler
 * @see org.apache.tapestry5.services.ExceptionReportWriter
 */
public interface ExceptionReporter
{
    /**
     * Used to communicate to the page what exception is to be reported.
     *
     * @param exception
     *         runtime exception thrown during processing of the request
     */
    void reportException(Throwable exception);
}