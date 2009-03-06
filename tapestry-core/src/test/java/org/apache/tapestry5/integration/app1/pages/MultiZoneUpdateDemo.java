// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.util.Date;

public class MultiZoneUpdateDemo
{
    @Inject
    private Block fredBlock, barneyBlock;

    public Date getNow()
    {
        return new Date();
    }

    Object onActionFromUpdate()
    {
        return new MultiZoneUpdate("fred", fredBlock).add("barney", barneyBlock).add("dino", "His dog, Dino.");
    }

    public String[] getOptions()
    {
        return new String[] { "Red", "Green", "Blue" };
    }

    public ValueEncoder getEncoder()
    {
        return new StringValueEncoder();
    }
}
