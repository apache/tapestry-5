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
 * A token containing an expression expansion from the template. Expression expansions look like Ant variables, i.e.,
 * "${xyz}", where xyz is a binding expression. It may have a prefix or not ("prop:" will be the default prefix if not
 * specified).
 */
public class ExpansionToken extends TemplateToken
{
    private final String expression;

    public ExpansionToken(String expression, Location location)
    {
        super(TokenType.EXPANSION, location);

        this.expression = expression;
    }

    public String getExpression()
    {
        return expression;
    }

    @Override
    public String toString()
    {
        return String.format("Expression[%s]", expression);
    }
}
