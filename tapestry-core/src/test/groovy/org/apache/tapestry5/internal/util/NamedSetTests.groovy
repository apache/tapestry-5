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

package org.apache.tapestry5.internal.util

import org.testng.Assert
import org.testng.annotations.Test

class NamedSetTests extends Assert
{
    @Test
    void empty_set_yields_rempty_names() {
        assert new NamedSet().names.empty
    }

    @Test
    void stored_names_retain_case() {
        NamedSet ns = new NamedSet()

        ns.put "Fred", 100
        ns.put "Barney", 200

        assert ns.get("fred") == 100
        assert ns.get("barney") == 200

        assert ns.names.sort() == ["Barney", "Fred"]
    }

    @Test
    void get_values_in_set() {
        NamedSet ns = new NamedSet()

        ns.put "Fred", 100
        ns.put "Barney", 200

        assert ns.values == [100, 200] as Set
    }

    @Test
    void replace_a_named_value() {
        NamedSet ns = new NamedSet()

        ns.put "Fred", 100
        ns.put "Barney", 200

        assert ns.get("fred") == 100
        assert ns.get("barney") == 200

        ns.put "FRED", 110
        ns.put "barNEY", 120

        assert ns.get("fred") == 110
        assert ns.get("barney") == 120

        assert ns.names.sort() == ["FRED", "barNEY"]
    }

    @Test
    void put_if_new_does_not_overrwrite() {
        NamedSet ns = new NamedSet()

        ns.put "Fred", 100
        ns.put "Barney", 200

        assert ns.get("fred") == 100
        assert ns.get("barney") == 200

        assert ns.putIfNew("FRED", 110) == false
        assert ns.putIfNew("Wilma", 300) == true

        assert ns.get("fred") == 100
        assert ns.get("barney") == 200
        assert ns.get("wilma") == 300

        assert ns.names.sort() == ["Barney", "Fred", "Wilma"]
    }

    @Test
    void missing_key_returns_null() {
        NamedSet ns = new NamedSet()


        assert ns.get("anything") == null
    }
}
