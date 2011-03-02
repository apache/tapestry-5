// Copyright 2010, 2011 The Apache Software Foundation
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
package org.example.testapp.pages;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class RemotePoolManagement
{

    public Object getSampleValue() throws Exception
    {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        return server.getAttribute(new ObjectName("org.example.testapp.services:service=Sample"), "SampleValue");
    }

}
