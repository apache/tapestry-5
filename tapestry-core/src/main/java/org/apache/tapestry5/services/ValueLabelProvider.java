// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.services;

/**
 * An object capable of providing a user-presentable label from a value. A special case exists for
 * {@linkplain org.apache.tapestry5.internal.services.EnumValueLabelProvider handling enum types}.
 * 
 * @since 5.4
 */
public interface ValueLabelProvider<V>
{

    /**
     * Gets label from the value. The label is used as user-presentable label for a value.
     * @param value the value to be label provided from
     * @return
     */
    public String getLabel(V value);

}
