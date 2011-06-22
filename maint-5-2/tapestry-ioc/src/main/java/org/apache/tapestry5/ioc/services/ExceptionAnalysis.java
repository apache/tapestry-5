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

package org.apache.tapestry5.ioc.services;

import java.util.List;

/**
 * An analysis of an exception (including nested exceptions).
 * <p/>
 * TODO: Make serializable and/or convert to XML format.
 */
public interface ExceptionAnalysis
{
    /**
     * Returns the analyzed exception info for each exception. The are ordered outermost exception to innermost.
     */
    List<ExceptionInfo> getExceptionInfos();
}
