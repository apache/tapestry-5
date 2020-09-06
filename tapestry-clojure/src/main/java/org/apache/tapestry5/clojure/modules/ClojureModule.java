// Copyright 2012-2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.clojure.modules;

import clojure.lang.RT;
import clojure.lang.Var;
import org.apache.tapestry5.clojure.ClojureBuilder;
import org.apache.tapestry5.clojure.MethodToFunctionSymbolMapper;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.internal.clojure.AnnotationMapper;
import org.apache.tapestry5.internal.clojure.ClojureBuilderImpl;
import org.apache.tapestry5.internal.clojure.DefaultMapper;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import java.util.List;

public class ClojureModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ClojureBuilder.class, ClojureBuilderImpl.class);
    }

    public static MethodToFunctionSymbolMapper buildMethodToFunctionMapper(List<MethodToFunctionSymbolMapper> configuration, ChainBuilder builder)
    {
        return builder.build(MethodToFunctionSymbolMapper.class, configuration);
    }

    @Contribute(MethodToFunctionSymbolMapper.class)
    public static void defaultMappers(OrderedConfiguration<MethodToFunctionSymbolMapper> configuration)
    {
        configuration.add("Annotation", new AnnotationMapper());
        configuration.add("Default", new DefaultMapper(), "after:*");
    }

    @Startup
    public static void launchClojure() {
        Var require = RT.var("clojure.core", "require");
    }
}
