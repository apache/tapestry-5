// Copyright 2007 The Apache Software Foundation
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


/**
 * Interface for any kind of object (typically, a component) that can provide a {@linkplain #getClientId() client-side
 * id}, typically used in the generation of client-side (JavaScript) logic. For components, the client id will be null
 * or innaccurate until after the component has rendered itself. Inside of any kind of loop, the clientId property is
 * only accurate just after the component has rendered, and before it renders again.
 * <p/>
 * Some components must be configured to provide a client id. In many cases, the client id matches the component's
 * {@linkplain ComponentResourcesCommon#getId() component id}, typically passed through {@link
 * RenderSupport#allocateClientId(String)} to ensure uniqueness.
 */
public interface ClientElement
{
    /**
     * Returns a unique id for the element. This value will be unique for any given rendering of a page. This value is
     * intended for use as the id attribute of the client-side element, and will be used with any DHTML/Ajax related
     * JavaScript.
     */
    String getClientId();
}
