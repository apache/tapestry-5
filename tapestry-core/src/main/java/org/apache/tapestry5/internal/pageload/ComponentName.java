// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

/**
 * Used to construct component names.
 */
public class ComponentName
{
    private final String pageName;

    final String nestedId;

    final String completeId;

    ComponentName(String pageName)
    {
        this.pageName = pageName;
        this.nestedId = null;
        this.completeId = pageName;
    }

    private ComponentName(String pageName, String nestedId, String completeId)
    {
        this.completeId = completeId;
        this.nestedId = nestedId;
        this.pageName = pageName;
    }

    ComponentName child(String embeddedId)
    {
        String newNestedId = nestedId == null
                             ? embeddedId
                             : nestedId + "." + embeddedId;

        return new ComponentName(pageName, newNestedId, pageName + ":" + newNestedId);
    }
}
