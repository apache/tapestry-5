// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.activationctx.pages;

import org.apache.tapestry5.annotations.InjectPage;

/**
 * TAP5-2070 and TAP5-1659 Integration test index page
 */
public class Index
{
    
    @InjectPage
    private NoContext noContext;

    @InjectPage
    private OneContext oneContext;
    
    @InjectPage
    private TwoContext twoContext;

    public String[] getTwoValues()
    {
        return new String[] {
                "One",
                "Two"
        };
    }
    
    public Class<?> getNoContextClass() {
        return NoContext.class;
    }

    public Class<?> getOneContextClass() {
        return OneContext.class;
    }

    public Class<?> getTwoContextClass() {
        return TwoContext.class;
    }

    public NoContext getNoContext()
    {
        return noContext;
    }

    public OneContext getOneContext()
    {
        return oneContext;
    }

    public TwoContext getTwoContext()
    {
        return twoContext;
    }

}
