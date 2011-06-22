// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;

/**
 * A node representing a comment embedded in the source input.
 */
public class CommentToken extends TemplateToken
{
    private final String comment;

    public CommentToken(String comment, Location location)
    {
        super(TokenType.COMMENT, location);

        this.comment = comment;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return String.format("Comment[%s]", comment);
    }
}
