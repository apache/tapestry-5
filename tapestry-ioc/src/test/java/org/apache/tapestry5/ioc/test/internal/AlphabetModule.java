// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test.internal;

import java.util.List;

import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.test.BlueMarker;
import org.apache.tapestry5.ioc.test.GreenMarker;
import org.apache.tapestry5.ioc.test.NameListHolder;
import org.apache.tapestry5.ioc.test.NameListHolder2;

public class AlphabetModule
{
    @Marker(BlueMarker.class)
    public NameListHolder buildGreek(final List<String> configuration)
    {

        return new NameListHolder()
        {
            @Override
            public List<String> getNames()
            {
                return configuration;
            }
        };
    }

    @Contribute(NameListHolder.class)
    @BlueMarker
    public void provideGreekConfiguration(OrderedConfiguration<String> configuration)
    {
        configuration.add("Alpha", "Alpha", "before:*");
        configuration.add("Beta", "Beta", "after:Alpha");
    }

    @Contribute(NameListHolder.class)
    @GreenMarker
    public void addToHebrew(OrderedConfiguration<String> configuration)
    {
        configuration.add("Alef", "Alef", "before:*");
        configuration.add("Bet", "Bet", "after:Alef");
        configuration.add("Gimel", "Gimel", "after:Bet");
    }

    @Marker(BlueMarker.class)
    public NameListHolder2 buildServiceWithEmptyConfiguration(final List<String> configuration)
    {
        return new NameListHolder2()
        {
            @Override
            public List<String> getNames()
            {
                return configuration;
            }
        };

    }
}
