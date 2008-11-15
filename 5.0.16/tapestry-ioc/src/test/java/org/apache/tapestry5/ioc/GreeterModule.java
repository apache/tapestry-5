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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Marker;

public class GreeterModule
{
    @Marker(YellowMarker.class)
    public NameListHolder buildYellowThing()
    {
        return null;
    }

    public Greeter buildHelloGreeter()
    {
        return new Greeter()
        {
            public String getGreeting()
            {
                return "Hello";
            }
        };
    }

    public Greeter buildGoodbyeGreeter()
    {
        return new Greeter()
        {
            public String getGreeting()
            {
                return "Goodbye";
            }
        };
    }

    @Marker(BlueMarker.class)
    public Greeter buildBlueGreeter()
    {
        return new Greeter()
        {
            public String getGreeting()
            {
                return "Blue";
            }
        };
    }

    @Marker(RedMarker.class)
    public Greeter buildRedGreeter1()
    {
        return null;
    }

    @Marker(RedMarker.class)
    public Greeter buildRedGreeter2()
    {
        return null;
    }

    public Greeter buildInjectedBlueGreeter(@BlueMarker
    Greeter greeter)
    {
        return greeter;
    }

    public Greeter buildInjectedRedGreeter(@RedMarker
    Greeter greeter)
    {
        return greeter;
    }

    public Greeter buildInjectedYellowGreeter(@YellowMarker
    Greeter greeter)
    {
        return greeter;
    }

    public Greeter buildGreeter(@InjectService("${greeter}")
    Greeter greeter)
    {
        return greeter;
    }

    public void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add("greeter", "HelloGreeter");
    }
}
