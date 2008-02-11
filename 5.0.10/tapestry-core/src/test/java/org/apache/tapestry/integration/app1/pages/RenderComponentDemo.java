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
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.ioc.annotations.Inject;

public class RenderComponentDemo
{
    @Persist
    private boolean _enabled;

    @Inject
    private Block _optional;

    public boolean isEnabled()
    {
        return _enabled;
    }

    public void setEnabled(boolean enable)
    {
        _enabled = enable;
    }

    public Object getThing()
    {
        return _enabled ? _optional : null;
    }
}
