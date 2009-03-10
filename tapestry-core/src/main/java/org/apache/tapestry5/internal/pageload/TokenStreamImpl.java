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

import java.util.List;

class TokenStreamImpl implements TokenStream
{
    private final List<TemplateToken> tokens;

    private int index = 0;

    TokenStreamImpl(List<TemplateToken> tokens)
    {
        this.tokens = tokens;
    }

    public boolean more()
    {
        return index < tokens.size();
    }

    public TokenType peekType()
    {
        checkMore();

        return tokens.get(index).getTokenType();
    }

    private void checkMore()
    {
        if (!more())
            throw new IllegalStateException("No more template tokens.");
    }

    public <T extends TemplateToken> T next(Class<T> type)
    {
        return type.cast(next());
    }

    public TemplateToken next()
    {
        checkMore();

        return tokens.get(index++);
    }
}
