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

package org.apache.tapestry5;

import java.io.Serializable;

/**
 * An action that is associated with a component. This is used in several areas of Tapestry and is primarily an attempt
 * to externalize state for a component so that it can be recorded outside the object.
 * <p/>
 * ComponentActions should be immutable. They are often created during one request and associated with a particular
 * component instance. They are then used in a later request (with an equivalent component instance).
 * <p/>
 * ComponentActions are serializable (they are often serialized into Base64 strings for storage on the client).
 */
public interface ComponentAction<T> extends Serializable
{
    /**
     * Passed a component instance, the action should operate upon the instance.
     */
    void execute(T component);
}
