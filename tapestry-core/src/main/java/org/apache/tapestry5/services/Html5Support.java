// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.services;

import org.apache.tapestry5.SymbolConstants;

/**
 * Service related to Tapestry's support of HTML5 features.
 * @since 5.4
 */
public interface Html5Support
{
    
    /**
     * Tells whether HTML5 is supported. The default implementation returns the value of the
     * {@link SymbolConstants#ENABLE_HTML5_SUPPORT} symbol.
     * 
     * @return <code>true</code> or <code>false</code>.
     */
    boolean isHtml5SupportEnabled();
    
}
