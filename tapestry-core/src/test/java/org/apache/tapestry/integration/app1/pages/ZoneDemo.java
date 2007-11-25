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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.Block;
import org.apache.tapestry.ioc.annotations.Inject;
import org.slf4j.Logger;

public class ZoneDemo
{
    @Inject
    private Logger _logger;

    private String _name;

    private static final String[] NAMES = {"Fred & Wilma", "Mr. <Roboto>", "Grim Fandango"};

    @Inject
    private Block _showName;

    public String[] getNames()
    {
        return NAMES;
    }


    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    Object onActionFromSelect(String name)
    {
        _name = name;

        _logger.info("Selected: '" + _name + "'");

        return _showName;
    }
}
