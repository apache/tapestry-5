// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.security;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Analyzes the current request to see if it is on the whitelist (of clients with access to certain key pages).
 * This is implemented as a chain-of-command of {@link WhitelistAnalyzer}s.
 *
 * @see org.apache.tapestry5.annotations.WhitelistAccessOnly
 * @since 5.3
 */
@UsesOrderedConfiguration(WhitelistAnalyzer.class)
public interface ClientWhitelist
{
    /**
     * Analyzes the current request, returning true if it is on the whitelist.
     *
     */
    boolean isClientRequestOnWhitelist();
}
