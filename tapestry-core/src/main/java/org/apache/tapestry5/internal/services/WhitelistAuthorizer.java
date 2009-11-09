// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.AssetPathAuthorizer;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AssetPathAuthorizer that determines access rights based on exact matching to a contributed whitelist. 
 * Any resource not explicitly specified in the whitelist is denied access.
 */
public class WhitelistAuthorizer implements AssetPathAuthorizer
{
    
    public List<Order> order()
    {
        return Arrays.asList(Order.ALLOW, Order.DENY);
    }

    //hash the resource paths for fast lookups.
    private final Map<String, Boolean> _paths;
    
    public WhitelistAuthorizer(Collection<String> paths)
    {
        _paths = new ConcurrentHashMap<String, Boolean>();
        for(String path : paths)
        {
            _paths.put(path, true);
        }
    }
    
    public boolean accessAllowed(String resourcePath)
    {
        return (_paths.containsKey(resourcePath));
    }

    public boolean accessDenied(String resourcePath)
    {
        return !_paths.containsKey(resourcePath);
    }

}
