// Copyright 2008, 2009, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal.antlr;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;

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

    /**
     * Scans identifiers and keywords here, and leaves every other token to the generated lexer.
     * An identifier is a Java identifier without identifier-ignorable characters, read by code point.
     * A keyword is an identifier that matches NULL, TRUE, FALSE or THIS, ignoring ASCII case.
     */
    @Override
    public Token nextToken()
    {
        // Whitespace has to go first, or the generated lexer would skip it and then read the identifier behind it.

        while (isWhitespace(input.LA(1)))
        {
            input.consume();
        }

        if (!isIdentifierStart(codePointAt(1)))
        {
            return super.nextToken();
        }

        state.token = null;
        state.channel = Token.DEFAULT_CHANNEL;
        state.tokenStartCharIndex = input.index();
        state.tokenStartCharPositionInLine = input.getCharPositionInLine();
        state.tokenStartLine = input.getLine();
        state.text = null;

        do
        {
            consumeCodePoint();
        } while (isIdentifierPart(codePointAt(1)));

        state.type = identifierType(getText());

        return emit();
    }

    private static boolean isWhitespace(int c)
    {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    private static boolean isIdentifierStart(int codePoint)
    {
        return Character.isJavaIdentifierStart(codePoint) && !Character.isIdentifierIgnorable(codePoint);
    }

    private static boolean isIdentifierPart(int codePoint)
    {
        return Character.isJavaIdentifierPart(codePoint) && !Character.isIdentifierIgnorable(codePoint);
    }

    /**
     * Returns the code point starting at the given lookahead position, or {@link CharStream#EOF}.
     */
    private int codePointAt(int i)
    {
        int high = input.LA(i);

        if (high != CharStream.EOF && Character.isHighSurrogate((char) high))
        {
            int low = input.LA(i + 1);

            if (low != CharStream.EOF && Character.isLowSurrogate((char) low))
            {
                return Character.toCodePoint((char) high, (char) low);
            }
        }

        return high;
    }

    private void consumeCodePoint()
    {
        boolean supplementary = Character.isSupplementaryCodePoint(codePointAt(1));

        input.consume();

        if (supplementary)
        {
            input.consume();
        }
    }

    private static int identifierType(String text)
    {
        // Only ASCII letters fold, so that 'thıs' (dotless i) stays an identifier.

        StringBuilder folded = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);

            if (c >= 'A' && c <= 'Z')
            {
                c = (char) (c + ('a' - 'A'));
            }

            folded.append(c);
        }

        switch (folded.toString())
        {
            case "null":
                return PropertyExpressionLexer.NULL;
            case "true":
                return PropertyExpressionLexer.TRUE;
            case "false":
                return PropertyExpressionLexer.FALSE;
            case "this":
                return PropertyExpressionLexer.THIS;
            default:
                return PropertyExpressionLexer.IDENTIFIER;
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
