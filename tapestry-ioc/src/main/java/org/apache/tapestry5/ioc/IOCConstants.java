// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.ioc;

public class IOCConstants
{
    public static final String MODULE_BUILDER_MANIFEST_ENTRY_NAME = "Tapestry-Module-Classes";

    public static final String MASTER_OBJECT_PROVIDER_SERVICE_ID = "MasterObjectProvider";

    /**
     * Name of a JVM System Property (but not, alas, a configuration symbol) that is used to disable
     * live service reloading entirely (i.e., reverting to Tapestry 5.1 behavior).
     * 
     * @since 5.2.2
     */
    public static final String SERVICE_CLASS_RELOADING_ENABLED = "tapestry.service-reloading-enabled";
}
