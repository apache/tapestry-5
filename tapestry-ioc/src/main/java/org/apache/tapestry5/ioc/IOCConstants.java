// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

public class IOCConstants
{
    /**
     * @deprecated Use {@link org.apache.tapestry5.ioc.ScopeConstants#DEFAULT} instead.
     */
    public static final String DEFAULT_SCOPE = ScopeConstants.DEFAULT;

    /**
     * @deprecated Use {@link org.apache.tapestry5.ioc.ScopeConstants#PERTHREAD} instead.
     */
    public static final String PERTHREAD_SCOPE = ScopeConstants.PERTHREAD;

    public static final String MODULE_BUILDER_MANIFEST_ENTRY_NAME = "Tapestry-Module-Classes";

    public static final String MASTER_OBJECT_PROVIDER_SERVICE_ID = "MasterObjectProvider";

    private IOCConstants()
    {
    }
}
