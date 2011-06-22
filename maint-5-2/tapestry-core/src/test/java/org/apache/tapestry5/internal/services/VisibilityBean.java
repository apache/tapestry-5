// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.annotations.Retain;

/**
 * Used to test some issues related to visibility.
 */
public class VisibilityBean
{
    // Got to some real effort to provoke some name collisions!

    @Retain
    public static int _$myStatic;

    @Retain
    protected String _$myProtected;

    @Retain
    String _$myPackagePrivate;

    @Retain
    public String _$myPublic;

    @Retain
    private long $myLong;

    public long getMyLong()
    {
        return $myLong;
    }

    public void setMyLong(long myLong)
    {
        $myLong = myLong;
    }

}
