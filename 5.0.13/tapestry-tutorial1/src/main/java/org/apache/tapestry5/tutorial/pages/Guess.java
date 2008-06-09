// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.tutorial.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

public class Guess
{
    @Persist
    private int target;

    @Property
    private int guess;

    @Persist
    @Property
    private String message;

    @Persist
    private int count;

    @InjectPage
    private GameOver gameOver;

    Object onActionFromLink(int guess)
    {
        count++;

        if (guess == target) return gameOver.initialize(count);

        if (guess < target)
            message = String.format("%d is too low.", guess);
        else
            message = String.format("%d is too high.", guess);

        return null;
    }

    Object initialize(int target)
    {
        this.target = target;
        count = 0;

        return this;
    }

    public int getTarget()
    {
        return target;
    }
}
