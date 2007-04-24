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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.services.FormSupport;

/**
 * TODO: This *must* be moved to org.apache.tapestry or org.apache.tapestry.services (it is part of
 * the framework's public API). Alternately, we can move some of the API inside {@link FormSupport}.
 */
public interface FormParameterLookup
{
    /**
     * Returns the query parameter value for the given name. Returns null if no such parameter is in
     * the request. For a multi-valued parameter, returns just the first value.
     */
    String getParameter(String name);

    // TODO: Multi-valued parameters? Also, it probably should be
    // getValue() or getParameterValue().
}
