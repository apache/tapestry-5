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

package org.apache.tapestry.integration.app1.components;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Parameter;

public class OutputRating
{
    @Parameter
    private int _rating;

    void beginRender(MarkupWriter writer)
    {
        if (_rating <= 0)
        {
            writer.write("-");
            return;
        }

        // Want 1 - 5 stars
        int stars = ((_rating - 1) / 20) + 1;

        for (int i = 0; i < stars; i++)
            writer.write("*");
    }
}
