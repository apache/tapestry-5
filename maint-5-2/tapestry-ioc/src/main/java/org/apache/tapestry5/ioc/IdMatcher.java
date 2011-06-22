// Copyright 2006 The Apache Software Foundation
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

/**
 * A matcher of <em>fully qualified<em> ids.
 */
public interface IdMatcher
{
    /**
     * Returns true if the provided input id matches the pattern defined by this matcher instance.
     *
     * @param id the fully qualfied id
     * @return true on match, false otherwise
     */
    boolean matches(String id);
}
