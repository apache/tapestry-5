// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.spring;

import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.internal.spring.SpringModuleDef;
import org.apache.tapestry5.ioc.def.ModuleDef;

import javax.servlet.ServletContext;

/**
 * Add logic to setup for Spring integration at startup.  In 5.1, this means creating a Spring ApplicationContext, and
 * wiring parts of it to resolve Tapestry objects.  In {@linkplain org.apache.tapestry5.spring.SpringConstants#USE_EXTERNAL_SPRING_CONTEXT
 * compatibility mode}, this means locating the externally configuration context and exposing each bean in it as a
 * Tapestry IoC service.
 */
public class TapestrySpringFilter extends TapestryFilter
{
    @Override
    protected ModuleDef[] provideExtraModuleDefs(ServletContext context)
    {
        return new ModuleDef[] {
                new SpringModuleDef(context)
        };
    }
}
