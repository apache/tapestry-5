// Copyright 2010, 2011 The Apache Software Foundation
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


package org.apache.tapestry5;

import org.apache.tapestry5.ajax.MultiZoneUpdate;

/**
 * Extends {@link ClientElement} with the concept of a body, a Block that can be rendered to provide the content
 * within. The primary implementation of this is the {@link org.apache.tapestry5.corelib.components.Zone} component, which exposes its client id and body for
 * use with {@link MultiZoneUpdate}.
 * 
 * @since 5.2.3
 * @see MultiZoneUpdate#add(ClientBodyElement)
 */
public interface ClientBodyElement extends ClientElement
{
    Block getBody();
}
