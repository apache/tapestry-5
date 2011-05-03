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

package org.apache.tapestry5.plastic;

import org.apache.tapestry5.plastic.PlasticManager.PlasticManagerBuilder;

/**
 * Options used when transforming classes. By default, no options are enabled, they must be specifically enabled
 * using {@link PlasticManagerBuilder#enable(TransformationOption)}.
 */
public enum TransformationOption
{
    /**
     * When enabled, fields that have a {@link FieldConduit} will write the value sent to, or returned by, the conduit.
     * This is essential for debugging of the transformed class.
     * 
     * @see PlasticField#setConduit(FieldConduit)
     * 
     */
    FIELD_WRITEBEHIND;
}
