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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.InjectContainer;

/**
 * Forces a client element to render its client id by ensuring that
 * {@link org.apache.tapestry5.ClientElement#getClientId() ClientElement#getClientId()}
 * is called. This is sometimes needed because, by design, most components (those that
 * implement {@link ClientElement}) only render a client-side ID if their getClientId
 * method is called sometime during the server-side DOM render.
 *
 * See the {@link org.apache.tapestry5.corelib.components.Any Any} component
 * for an example of use.
 * 
 * @tapestrydoc
 */
public class RenderClientId
{
    @InjectContainer
    private ClientElement element;

    @AfterRender
    void ensureId()
    {
        element.getClientId();
    }
}
