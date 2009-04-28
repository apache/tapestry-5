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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.internal.services.ContextResource;
import org.apache.tapestry5.ioc.internal.services.ResourceSymbolProvider;
import org.apache.tapestry5.services.Context;

/**
 * Makes a {@link org.apache.tapestry5.ioc.Resource} in the {@link org.apache.tapestry5.services.Context} available as a
 * {@link org.apache.tapestry5.ioc.services.SymbolProvider}
 *
 * @since 5.1.0.5
 */
public class ContextResourceSymbolProvider extends ResourceSymbolProvider
{
    public ContextResourceSymbolProvider(Context context, String path)
    {
        super(new ContextResource(context, path));
    }
}
