// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

import java.util.List;

/**
 * Determines whether access to an asset is allowed, denied, or undetermined.
 * Each contributed authorizer makes up part of a chain of command for determining access.
 * Access is explicitly allowed if accessAllowed returns true.
 * Access is explicitly denied if accessDenied returns true.
 * Ordering depends on the order specified by the "order" parameter.
 * Hence, an implementation which specifies an order of:
 * ALLOW, DENY, and returns true from both accessAllowed and accessDenied
 * will allow access for all resources. With the same return values for the
 * access* methods but the order switched to DENY, ALLOW, access to all resources
 * would be denied.  It is possible for an authorizer to have "nothing 
 * to say" regarding a particular resource.  If accessAllowed returns false,
 * it does not mean that access is denied, merely that it is not explicitly allowed.
 * If accessDenied returns false, it does not mean that access is allowed, merely that
 * it is not explicitly denied.  Hence, if both accessAllowed and accessDenied return false,
 * control will pass to the next authorizer in the chain.
 * 
 */
public interface AssetPathAuthorizer
{
    
    /**
     * Types of orderings, either ALLOW or DENY.
     */
    enum Order {ALLOW, DENY;}
    
    /**
     * Specify the ordering for this authorizer.
     * @return the operations for this authorizer. 
     * Operations will be performed in the order returned by the iterator.
     * It is assumed that the authorizer correctly implements each form of
     * ordering returned. It is acceptable to only return only ALLOW or DENY.
     */
    List<Order> order();

    /**
     * Determines whether a request to "resourcePath" is allowed. 
     * @param resourcePath
     * @return true if access is explicitly allowed for the path. False otherwise.
     * For example, a whitelist implementation would return true if the resource
     * was listed, and false otherwise.  A blacklist implementation would return
     * false regardless of whether the path was in the blacklist.
     * Alternatively, if the blacklist specified an order of DENY, ALLOW, it could
     * return true from accessAllowed if the resource was not explicitly listed in its
     * blacklist.
     */
    boolean accessAllowed(String resourcePath);

    /**
     * 
     * @param resourcePath
     * @return true if access is explicitly prohibited for the path. False otherwise.
     * For example, a whitelist implementation would return true if the resource was
     * not explicitly listed, and false otherwise. A blacklist implementation would 
     * return true if the resource was explicitly denied, and false otherwise.
     */
    boolean accessDenied(String resourcePath);
}
