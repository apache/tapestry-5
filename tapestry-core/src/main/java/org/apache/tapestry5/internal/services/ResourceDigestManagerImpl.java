// Copyright 2006, 2007, 2008, 2009, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.services.InvalidationListener;

import java.util.Map;

public class ResourceDigestManagerImpl implements ResourceDigestManager
{
    public String getDigest(Resource resource)
    {
        return null;
    }

    public boolean requiresDigest(Resource resource)
    {
        return false;
    }

    public void addInvalidationListener(InvalidationListener listener)
    {
    }

    public void addInvalidationCallback(Runnable callback)
    {
    }

    public void clearOnInvalidation(Map<?, ?> map)
    {
    }
}
