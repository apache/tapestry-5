// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.ajax;

import org.apache.tapestry5.json.JSONObject;

/**
 * A callback used with {@link AjaxResponseRenderer#addCallback(JSONCallback)}.
 *
 * @since 5.3
 */
public interface JSONCallback
{
    /**
     * Modify the reply, typically by adding additional keys.
     *
     * @param reply the reply JSON object to be sent to the client
     */
    void run(JSONObject reply);

}
