// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.parser;

/**
 * Defines the different types of {@link org.apache.tapestry5.internal.parser.TemplateToken}s. Each value maps to a
 * particular subclass of TemplateToken.
 */
public enum TokenType
{

    ATTRIBUTE, CDATA, COMMENT, END_ELEMENT, START_COMPONENT, START_ELEMENT, TEXT, BODY, EXPANSION, PARAMETER, BLOCK, DTD,
    DEFINE_NAMESPACE_PREFIX,

    /**
     * @since 5.1.0.1
     */
    EXTENSION_POINT
}
