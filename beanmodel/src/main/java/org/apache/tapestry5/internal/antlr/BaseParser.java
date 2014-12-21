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

package org.apache.tapestry5.internal.antlr;

import org.antlr.runtime.Parser;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.RecognizerSharedState;

public class BaseParser extends Parser
{
    public BaseParser(TokenStream tokenStream)
    {
        super(tokenStream);
    }

    public BaseParser(TokenStream tokenStream,
                      RecognizerSharedState recognizerSharedState)
    {
        super(tokenStream, recognizerSharedState);
    }

    @Override
    public void emitErrorMessage(String message)
    {
        // This is caught and more properly reported later.
        throw new RuntimeException(message);
    }
}
