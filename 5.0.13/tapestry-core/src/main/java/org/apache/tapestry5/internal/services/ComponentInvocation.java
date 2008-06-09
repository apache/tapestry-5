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

import java.util.List;

public interface ComponentInvocation
{
    /**
     * @return A path taking the format <em>target-path</em>/e1/e2?&q1=v1&q2=v2. where the <em>target-path</em> is the
     *         path provided by the invocation target; e1 and e2 are elements of the context; q1 and q2 are the
     *         parameters.
     */
    String buildURI(boolean isForm);

    String[] getContext();

    String[] getActivationContext();

    void addParameter(String parameterName, String value);

    List<String> getParameterNames();

    String getParameterValue(String name);

    InvocationTarget getTarget();
}
