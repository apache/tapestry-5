// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.clojure;

import clojure.lang.Symbol;
import org.apache.tapestry5.clojure.FunctionName;
import org.apache.tapestry5.clojure.MethodToFunctionSymbolMapper;

import java.lang.reflect.Method;

public class AnnotationMapper implements MethodToFunctionSymbolMapper
{
    @Override
    public Symbol mapMethod(String namespace, Method method)
    {
        FunctionName annotation = method.getAnnotation(FunctionName.class);

        if (annotation == null)
        {
            return null;
        }

        String name = annotation.value();

        if (name.contains("/"))
        {
            return Symbol.create(name);
        }

        return Symbol.create(namespace, name);
    }
}
