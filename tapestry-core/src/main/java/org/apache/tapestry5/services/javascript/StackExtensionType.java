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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;

/**
 * Defines the types of extensions to a JavaScript stack that can be contributed to an extensible JavaScript stack.
 * 
 * @since 5.3
 * @see StackExtension
 * @see ExtensibleJavaScriptStack
 */
public enum StackExtensionType
{
    /**
     * A JavaScript library. The extension value will be converted using {@link AssetSource#getExpandedAsset(String)},
     * meaning that {@linkplain SymbolSource symbols} will be expanded.
     */
    LIBRARY,

    /**
     * A stylesheet. The extension value will be converted using {@link AssetSource#getExpandedAsset(String)},
     * meaning that {@linkplain SymbolSource symbols} will be expanded.
     */
    STYLESHEET,

    /** Extra JavaScript initialization (rarely used). No symbol expansion takes place. */
    INITIALIZATION;
}
