// Copyright 2013-2014 The Apache Software Foundation
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

package org.apache.tapestry5.internal.webresources;

import org.mozilla.javascript.ScriptableObject;

public interface RhinoExecutor
{
    /**
     * Invokes the named function, which must return a scriptable object (typically, a JavaScript Object).
     *
     * @param functionName
     *         name of function visible to the executor's scope (e.g., loaded from the scripts associated
     *         with the executor).
     * @param arguments
     *         Arguments to pass to the object which must be convertable to JavaScript types; Strings work well here.
     * @return result of invoking the function.
     */
    ScriptableObject invokeFunction(String functionName, Object... arguments);

    /**
     * Discards the executor, returning it to the pool for reuse.
     */
    void discard();
}
