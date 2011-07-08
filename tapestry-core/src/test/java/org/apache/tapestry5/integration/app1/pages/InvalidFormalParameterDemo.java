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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.integration.app1.components.Count;

/**
 * @since 5.3
 */
public class InvalidFormalParameterDemo {

    // This has to be done using @Component, because non-matching attributes
    // in the template are quietly dropped. By placing it here, in the @Component annotation,
    // there's no ambiguity ... the developer expects there to be a parameter with the given name.
    @Component(parameters = {
            "start=5",
            "end=100",
            "value=var:index",
            "step=5" // not a formal parameter
    })
    private Count counter;
}
