// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic

class ConstructorCallbackTests extends AbstractPlasticSpecification
{
    def "constructor callback invoked after field injection"()
    {
        String injectedValue = "value to inject into new field"
        String observedValue
        FieldHandle fieldHandle

        def mgr = createMgr({ PlasticClass pc ->

            fieldHandle = pc.introduceField(String.class, "newField").inject(injectedValue).handle

            pc.onConstruct({ instance, context ->

                observedValue = fieldHandle.get(instance)

            } as ConstructorCallback)
        } as PlasticClassTransformer)

        when:

        mgr.getClassInstantiator("testsubjects.Empty").newInstance()

        then:

        observedValue == injectedValue
    }
}
