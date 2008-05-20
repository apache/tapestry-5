// Copyright  2008 The Apache Software Foundation
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

package org.apache.tapestry.internal;

import org.apache.tapestry.NullFieldStrategy;

/**
 * Default strategy, which is to do nothing: null values stay null.
 */
public class DefaultNullFieldStrategy implements NullFieldStrategy
{
    /**
     * Returns null.
     */
    public Object replaceToClient()
    {
        return null;
    }

    /**
     * Returns the empty string.
     */
    public String replaceFromClient()
    {
        return null;
    }
}
