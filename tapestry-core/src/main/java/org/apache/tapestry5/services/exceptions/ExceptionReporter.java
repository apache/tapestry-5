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

package org.apache.tapestry5.services.exceptions;

/**
 * Services used to report request handling exceptions. This is invoked <em>before</em> the exception report page is rendered.
 * The default implementation converts the exception into a well formatted text file, with content similar to the default
 * {@link org.apache.tapestry5.corelib.pages.ExceptionReport} page, and stores this file on the file system.
 * <p/>
 * Exception report files are stored beneath a root directory, with intermediate folders for year, month, day, hour, and minute.
 * <p/>
 * Directories are created as necessary; however, there is nothing in place to delete exceptions.
 *
 * @see org.apache.tapestry5.SymbolConstants#EXCEPTION_REPORTS_DIR
 * @since 5.4
 */
public interface ExceptionReporter
{
    /**
     * Records the exception.
     */
    void reportException(Throwable exception);
}
