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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.internal.parser.TokenType;

/**
 * Provides cursor index into a {@link org.apache.tapestry5.internal.parser.ComponentTemplate}.
 */
interface TokenStream
{
    /**
     * Returns true if there are more tokens available.
     */
    boolean more();

    /**
     * Peeks at the type of the next token.
     */
    TokenType peekType();

    /**
     * Returns the next token and casts it to the indicated type.
     */
    <T extends TemplateToken> T next(Class<T> type);

    /**
     * Returns the next token.
     */
    TemplateToken next();
}
