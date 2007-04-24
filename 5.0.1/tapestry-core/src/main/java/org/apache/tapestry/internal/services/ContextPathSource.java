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

package org.apache.tapestry.internal.services;

/**
 * It is an object that can provide the context path of the web app. For example, the
 * {@link org.apache.tapestry.services.Request} is one such object.
 */
public interface ContextPathSource
{
    /**
     * Returns the context path. This always starts with a "/" character and does not end with one,
     * with the exception of servlets in the root context, which return the empty string.
     */
    String getContextPath();

}
