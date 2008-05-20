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

package org.apache.tapestry.dom;

/**
 * Part of a {@link MarkupModel}, used to define how end tags are handled when the {@link Document} in rendered out as a
 * text stream.
 */
public enum EndTagStyle
{

    /**
     * Omit the end tag. Examples for HTML include the input, br and img elements.
     */
    OMIT,
    /**
     * Require an end tag always. This is the default for most elements in HTML.
     */
    REQUIRE,
    /**
     * Require an end tag, but abbreviate it if the element has no children. This is the only value used in XML
     * documents.
     */
    ABBREVIATE
}
