// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.NullFieldStrategy;

/**
 * Treats nulls to or from the client as if they were 0's.
 */
public class ZeroNullFieldStrategy implements NullFieldStrategy
{
    /**
     * Returns the value 0.
     */
    public Object replaceToClient()
    {
        return 0L;
    }

    /**
     * Returns "0".
     */
    public String replaceFromClient()
    {
        return "0";
    }
}
