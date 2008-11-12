//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

public class FieldResourceInjectionModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(FieldResourceService.class);
        binder.bind(StringTransformer.class, FailedFieldInjectionStringTransformer.class);
    }

    public static void contributeFieldResourceService(Configuration<String> configuration)
    {
        configuration.add("Fred");
        configuration.add("Barney");
        configuration.add("Wilma");
        configuration.add("Betty");
    }
}
