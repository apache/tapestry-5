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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.util.TextStreamResponse;

/**
 * Have to start somewhere!
 */
public class Start
{
    Object onActionFromTextStreamResponse()
    {
        String text = "<html><body>Success!</body></html>";

        return new TextStreamResponse("text/html", text);
    }

    Object onActionFromBadReturnType()
    {
        // What is Tapestry supposed to do with this? Let's see than Exception Report page.
        return 20;
    }
}
