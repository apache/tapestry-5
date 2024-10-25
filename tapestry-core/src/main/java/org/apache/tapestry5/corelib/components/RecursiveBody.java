// Licensed to the Apache License, Version 2.0 (the "License");
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.RecursiveContext;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Environment;

/**
 * <p>
 * Component that marks the place in the template the
 * recursion should happen. It should only be used inside
 * {@link Recursive}, otherwise an exception will be
 * thrown.
 * </p>
 * <p>
 * This was contributed by <a href="https://www.pubfactory.com">KGL PubFactory</a>.
 * </p>
 * @since 5.9.0
 */
public class RecursiveBody 
{
    
    @Inject
    private Environment environment;
    
    @Environmental
    private RecursiveContext context;

    void beginRender(MarkupWriter writer) 
    {
        final RecursiveContext recursiveContext = environment.peek(RecursiveContext.class);
        if (recursiveContext != null) 
        {
            final Element placeholder = writer
                    .element(
                            Recursive.RECURSIVE_INSERTION_POINT_ELEMENT_NAME,
                            "id", recursiveContext.getId());
            context.registerPlaceholder(placeholder);
        }
        
    }

    boolean beginRenderTemplate(MarkupWriter writer) 
    {
        return false; // throw away any body this component instance might have in its declaration
    }

    void afterRender(MarkupWriter writer) 
    {
        writer.end();
    }

}