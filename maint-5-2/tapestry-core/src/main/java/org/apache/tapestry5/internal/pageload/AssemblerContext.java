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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.runtime.RenderCommand;

import java.util.List;

/**
 * Used when constructing a {@link org.apache.tapestry5.internal.pageload.AssemblerContext}, encapsulating the
 * assembler, the {@link org.apache.tapestry5.internal.pageload.TokenStream} for the component's template, and helping
 * to consolidate composable render commands (that is, a series of render commands that are not components can be
 * replaced with a single {@link org.apache.tapestry5.internal.pageload.CompositeRenderCommand} which reduces the number
 * of render operations for the page).
 */
class AssemblerContext implements TokenStream
{
    final ComponentAssembler assembler;

    final TokenStream stream;

    private final List<RenderCommand> composable = CollectionFactory.newList();

    AssemblerContext(ComponentAssembler assembler, TokenStream stream)
    {
        this.assembler = assembler;
        this.stream = stream;
    }

    public boolean more()
    {
        return stream.more();
    }

    public TemplateToken next()
    {
        return stream.next();
    }

    public <T extends TemplateToken> T next(Class<T> type)
    {
        return stream.next(type);
    }

    public TokenType peekType()
    {
        return stream.peekType();
    }

    void addComposable(RenderCommand command)
    {
        composable.add(command);
    }

    void flushComposable()
    {
        switch (composable.size())
        {
            case 0:
                return;

            case 1:
                addRenderCommand(composable.get(0));
                break;

            default:
                addRenderCommand(new CompositeRenderCommand(composable.toArray(new RenderCommand[composable.size()])));
                break;
        }

        composable.clear();
    }

    void add(PageAssemblyAction action)
    {
        flushComposable();

        assembler.add(action);
    }

    private void addRenderCommand(final RenderCommand command)
    {
        assembler.add(new PageAssemblyAction()
        {
            public void execute(PageAssembly pageAssembly)
            {
                pageAssembly.addRenderCommand(command);
            }
        });
    }
}
