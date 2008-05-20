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

package org.apache.tapestry5.internal.services;

/**
 * Determines the reuest encoding for the given page and applies that to the request, so that parameters may be properly
 * decoded.
 */
public interface RequestEncodingInitializer
{
    /**
     * Initializes the request encoding to match the encoding defined for the page.
     *
     * @param pageName logical name of the page
     */
    void initializeRequestEncoding(String pageName);
}
