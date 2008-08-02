//  Copyright 2008 The Apache Software Foundation
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

/**
 * Used to test the functionality related to {@link org.apache.tapestry5.ioc.ServiceBinder#bind(Class,
 * ServiceBuilder)}.
 */
public class ServiceBuilderModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(Greeter.class, new ServiceBuilder<Greeter>()
        {
            public Greeter buildService(ServiceResources resources)
            {
                return new Greeter()
                {
                    public String getGreeting()
                    {
                        return "Greetings from service Greeter.";
                    }
                };
            }
        });

        binder.bind(Greeter.class, new ServiceBuilder<Greeter>()
        {
            public Greeter buildService(ServiceResources resources)
            {
                throw new RuntimeException("Failure inside ServiceBuilder callback.");
            }
        }).withId("BrokenGreeter");
    }
}
