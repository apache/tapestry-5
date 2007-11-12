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
 * Used for some testing where we want a model with XML style semantics.
 */
public final class XMLMarkupModel extends DefaultMarkupModel
{

    /**
     * Always returns ABBREVIATE.
     */
    @Override
    public EndTagStyle getEndTagStyle(String element)
    {
        return EndTagStyle.ABBREVIATE;
    }
}