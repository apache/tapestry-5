// Copyright 2007, 2008 The Apache Software Foundation
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
 * A block is a collection of static text and elements, and components, derived from a component template. In the
 * template, a block is demarcated using the &lt;t:block&gt; or &lt;t:parameter&gt; elements. The interface defines no
 * methods, but the provided implementations of Block are capable of rendering their contents on demand.
 * <p/>
 * Tapestry includes coecions from String to {@link org.apache.tapestry5.Renderable} and {@link
 * org.apache.tapestry5.Renderable} to Block. This means that components that take Block parameters may be bound to
 * literal strings, to arbitrary numbers (or other objects, with the expectation that they will be converted to
 * strings), or to renderable objects such as components.
 */
public interface Block
{

}
