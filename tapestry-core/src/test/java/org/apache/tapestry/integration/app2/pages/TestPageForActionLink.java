// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.integration.app2.pages;

import org.apache.tapestry.annotation.InjectPage;
import org.apache.tapestry.annotation.OnEvent;

public class TestPageForActionLink
{
    @InjectPage("ResultPageForActionLink")
    private ResultPageForActionLink resultPage;

    @OnEvent(component = "link1")
    public ResultPageForActionLink onClick(int number)
    {
        resultPage.setNumber(number);
        return resultPage;
    }
}
