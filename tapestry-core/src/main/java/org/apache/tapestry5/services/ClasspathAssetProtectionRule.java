// Copyright 2018 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Chain-of-responsibility service which defines rules for blocking access to classpath resources 
 * based on their paths. Access is blocked if any rule says it should be blocked.
 * 
 * @see ComponentEventRequestHandler
 */
@UsesOrderedConfiguration(ClasspathAssetProtectionRule.class)
public interface ClasspathAssetProtectionRule
{
    /**
     * Tells whether the access to the resource with this path should be blocked or not.
     * If this rule doesn't concern the given path, it should return false.
     */
    public boolean block(String path);
}
