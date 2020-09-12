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
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.test.BlueMarker;
import org.apache.tapestry5.ioc.test.GreenMarker;
import org.apache.tapestry5.ioc.test.NameListHolder;
import org.apache.tapestry5.ioc.test.RedMarker;

public class AlphabetModule2
{
    @Marker(GreenMarker.class)
    public NameListHolder buildHebrew(final List<String> configuration)
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

    @Marker(BlueMarker.class)
    public NameListHolder buildAnotherGreek(final List<String> configuration)
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
    public void extendGreekConfiguration(OrderedConfiguration<String> configuration)
    {
        configuration.add("Gamma", "Gamma", "after:Beta");
        configuration.add("Delta", "Delta", "after:Gamma");
    }

    @Contribute(NameListHolder.class)
    @BlueMarker
    @Local
    public void contributeXyz(OrderedConfiguration<String> configuration)
    {
        configuration.add("Epsilon", "Epsilon", "after:*");
    }

    @Contribute(NameListHolder.class)
    @GreenMarker
    public void someMoreHebrewLetters(OrderedConfiguration<String> configuration)
    {
        configuration.add("Dalet", "Dalet", "after:Gimel");
        configuration.add("He", "He", "after:Dalet");
    }

    public void contributeHebrew(OrderedConfiguration<String> configuration)
    {
        configuration.add("Vav", "Vav", "after:He");
    }
}
