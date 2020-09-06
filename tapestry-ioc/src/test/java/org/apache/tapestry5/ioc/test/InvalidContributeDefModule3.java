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

package org.apache.tapestry5.ioc.test;

import java.util.List;

import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;

public class InvalidContributeDefModule3
{
    @Marker(BlueMarker.class)
    public Runnable build(final List<String> configuration)
    {

        return new Runnable()
        {
            @Override
            public void run()
            {

            }

        };
    }

    /**
     * Its a contribute method, but to a service that does not exist.
     */
    @Contribute(NameListHolder.class)
    @BlueMarker
    public void provideConfiguration(OrderedConfiguration<String> configuration)
    {

    }
}
