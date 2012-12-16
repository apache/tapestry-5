// Copyright 2009, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.test;

/**
 * Responsible for writing an error report for the current test, after an assertion fails. The HTML
 * source for the page is downloaded and written to a
 * file in the TestNG output directory (in a file named after the test).
 */
public interface ErrorReporter
{
    /**
     * Writes an error report file into the TestNG output directory, based on the name of the test,
     * containing the current page content.
     *
     * @param reportText text to store in an associated .txt file, describing the failure.
     */
    void writeErrorReport(String reportText);
}
