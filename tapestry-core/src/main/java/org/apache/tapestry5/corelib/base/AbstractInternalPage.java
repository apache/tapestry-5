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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Base page for Tapestry internal pages, that should suppress any application changes to the core stack's CSS.
 * CSS from the core stack is suppressed, instead the internal stack (which exists for just this purpose)
 * is imported.
 *
 * @since 5.4
 */
public abstract class AbstractInternalPage
{
    @Inject @Property(write = false)
    protected Request request;

    void setupRender()
    {
        request.setAttribute(InternalConstants.SUPPRESS_CORE_STYLESHEETS, true);
    }

    @Import(stack = "internal")
    void beginRender()
    {

    }
}
