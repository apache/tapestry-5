// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Html5Support;

public class Html5SupportImpl implements Html5Support
{
    
    final private boolean enabled;
    
    public Html5SupportImpl(@Inject @Symbol(SymbolConstants.ENABLE_HTML5_SUPPORT) final boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean isHtml5SupportEnabled()
    {
        return enabled;
    }

}
