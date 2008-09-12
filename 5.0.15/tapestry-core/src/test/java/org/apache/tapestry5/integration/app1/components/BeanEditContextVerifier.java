// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.services.BeanEditContext;

/**
 * Used to check to make sure that the BeanEditor is properly pushing a BeanEditContext into the environment.
 */
public class BeanEditContextVerifier 
{
    @Environmental
    private BeanEditContext context;

    void beginRender(MarkupWriter writer)
    {
        writer.write("Bean class from context is: " + context.getBeanClass().getName());
    }
}
