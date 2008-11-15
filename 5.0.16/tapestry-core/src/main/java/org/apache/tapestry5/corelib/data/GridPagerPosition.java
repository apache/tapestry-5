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

package org.apache.tapestry5.corelib.data;

import org.apache.tapestry5.corelib.components.Grid;

/**
 * Used by the {@link Grid} component to control where the pager portion of the Grid should be displayed.
 */
public enum GridPagerPosition
{
    /**
     * Position the pager above the Grid's table.
     */
    TOP(true, false),

    /**
     * Position the pager below the Grid's table (this is the default).
     */
    BOTTOM(false, true),

    /**
     * Show the pager above and below the Grid's table.
     */
    BOTH(true, true),

    /**
     * Don't show a pager (the application will need to supply its own navigation mechanism).
     */
    NONE(false, false);

    private final boolean matchTop;

    private final boolean matchBottom;

    private GridPagerPosition(boolean matchTop, boolean matchBottom)
    {
        this.matchTop = matchTop;
        this.matchBottom = matchBottom;
    }

    public boolean isMatchBottom()
    {
        return matchBottom;
    }

    public boolean isMatchTop()
    {
        return matchTop;
    }

}
