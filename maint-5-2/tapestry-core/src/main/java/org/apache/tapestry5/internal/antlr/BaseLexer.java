// Copyright 2008, 2009 The Apache Software Foundation
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

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.RecognitionException;

public abstract class BaseLexer extends Lexer
{
    protected BaseLexer()
    {
    }

    protected BaseLexer(CharStream charStream,
                        RecognizerSharedState recognizerSharedState)
    {
        super(charStream, recognizerSharedState);
    }

    protected void stripLeadingPlus()
    {
        String text = getText();

        // For compatibility with Tapestry 5.0, we need to allow a sign of '+', which Long.parseLong()
        // doesn't accept. To keep things downstream simple, we eliminate the '+' here.

        if (text.startsWith("+"))
        {
            setText(text.substring(1));
        }
    }

    @Override
    public void reportError(RecognitionException e)
    {
        throw new RuntimeException(String.format("Unable to parse input at character position %d",
                                                 e.charPositionInLine + 1),
                                   e);
    }
}
