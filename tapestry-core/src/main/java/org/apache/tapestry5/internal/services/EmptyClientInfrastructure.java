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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.services.ClientInfrastructure;

import java.util.Collections;
import java.util.List;

/**
 * Used for Ajax responses to provide an empty stack (since, by definition, the client must already have the base
 * stack).
 *
 * @since 5.1.0.2
 */
public class EmptyClientInfrastructure implements ClientInfrastructure
{
    public List<Asset> getJavascriptStack()
    {
        return Collections.emptyList();
    }

    public List<Asset> getStylesheetStack()
    {
        return Collections.emptyList();
    }
}
