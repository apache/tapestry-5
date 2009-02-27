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

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.integration.app1.services.MusicLibrary;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.util.Date;

public class ProgressiveDemo
{
    @Inject
    @Property
    private MusicLibrary musicLibrary;

    @Property
    private String context2;

    @InjectComponent
    private Grid music;

    public Date getNow()
    {
        return new Date();
    }


    void onProgressiveDisplayFromDisp2(String context)
    {
        context2 = context;
    }

    Object onProgressiveDisplayFromProgressiveGrid()
    {

        // sleep(1000);

        return music;
    }

    public static void sleep(int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (Exception ex)
        {
        }
    }


    Object onActionFromRefresh()
    {
        // sleep(100);

        return this;
    }
}
