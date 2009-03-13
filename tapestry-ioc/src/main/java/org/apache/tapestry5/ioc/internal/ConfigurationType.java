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

package org.apache.tapestry5.ioc.internal;

/**
 * Defines the three types of configurations a service may request.
 */
public enum ConfigurationType
{

    /**
     * @see org.apache.tapestry5.ioc.Configuration
     */
    UNORDERED,
    /**
     * @see org.apache.tapestry5.ioc.OrderedConfiguration
     */
    ORDERED,
    /**
     * @see org.apache.tapestry5.ioc.MappedConfiguration
     */
    MAPPED
}
