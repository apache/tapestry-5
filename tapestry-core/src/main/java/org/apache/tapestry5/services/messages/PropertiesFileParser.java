// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services.messages;

import java.io.IOException;
import java.util.Map;

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CaseInsensitiveMap;

/**
 * Used when constructing a component's {@link Messages} object. Responsible for reading the
 * contents of an individual {@link Resource}.
 * 
 * @since 5.2.0
 */
public interface PropertiesFileParser
{
    /**
     * Read the contents of the file (which is expected to exist) and return it as
     * a Map of string keys and values (as {@link CaseInsensitiveMap} should be used. The implementation should not
     * attempt to cache any values (caching occurs at a higher level, as does reload logic).
     */
    Map<String, String> parsePropertiesFile(Resource resource) throws IOException;
}
