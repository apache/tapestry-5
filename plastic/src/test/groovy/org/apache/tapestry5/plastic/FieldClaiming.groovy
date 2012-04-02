// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic

import testsubjects.SingleField

class FieldClaiming extends AbstractPlasticSpecification
{
    def "get fields ignores claimed fields"() {
        setup:
        def pc = mgr.getPlasticClass(SingleField.name)
        def f = pc.unclaimedFields.first()

        expect:
        f.name == "myField"
        ! f.claimed

        when:
        def f2 = f.claim("my tag")

        then:
        f2.is(f)

        f.claimed
        pc.unclaimedFields == []
        pc.allFields == [f]
    }

    def "a field may only be claimed once"() {
        setup:
        def pc = mgr.getPlasticClass(SingleField.name)
        def f = pc.unclaimedFields.first()

        f.claim "[first tag]"

        when:
        f.claim "[second tag]"

        then:
        def e = thrown(IllegalStateException)
        e.message == "Field myField of class testsubjects.SingleField can not be claimed by [second tag] as it is already claimed by [first tag]."
    }
}
