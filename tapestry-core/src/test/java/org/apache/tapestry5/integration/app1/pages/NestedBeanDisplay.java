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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.Person;

public class NestedBeanDisplay
{
    @Persist
    @Property
    private Person parent;

    // Visibility changed from package-private to public due to smarter page invalidation
    // (classes in different classloaders cannot access non-public method
    // TODO: maybe find some way to avoid this?
    public Object initialize(Person person)
    {
        parent = person;

        return this;
    }
}
