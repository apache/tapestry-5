// Copyright 2010 The Apache Software Foundation
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



/**
 * Exposes page pool settings as managed properties of a MBean.
 * 
 * @since 5.2.0
 */
public interface PagePoolImplMBean
{
    /**
     * Returns the soft limit.
     * 
     * @see org.apache.tapestry5.SymbolConstants#PAGE_POOL_SOFT_LIMIT
     */
    int getSoftLimit();
    
    /**
     * Sets the soft limit.
     */
    void setSoftLimit(int softLimit);
    
    /**
     * Returns the soft wait.
     * 
     * @see org.apache.tapestry5.SymbolConstants#PAGE_POOL_SOFT_WAIT
     */
    long getSoftWait();
    
    /**
     * Sets the soft wait.
     */
    void setSoftWait(long softWait);
    
    /**
     * Returns the hard limit.
     * 
     * @see org.apache.tapestry5.SymbolConstants#PAGE_POOL_HARD_LIMIT
     * 
     * @deprecated The hard limit will be removed in a later release of Tapestry, as the maximum number of instance
     *             is easily controlled by limiting the number of request handling threads in the servlet container.
     */
    int getHardLimit();
    
    /**
     * Sets the hard limit.
     * 
     * @deprecated The hard limit will be removed in a later release of Tapestry, as the maximum number of instance
     *             is easily controlled by limiting the number of request handling threads in the servlet container.
     */
    void setHardLimit(int hardLimit);
    
    /**
     * Returns the active window.
     * 
     * @see org.apache.tapestry5.SymbolConstants#PAGE_POOL_ACTIVE_WINDOW
     */
    long getActiveWindow();
    
    /**
     * Sets the active window.
     */
    void setActiveWindow(long activeWindow);
}
