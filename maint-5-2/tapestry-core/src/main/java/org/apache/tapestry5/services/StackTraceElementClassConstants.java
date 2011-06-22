// Copyright 2009 The Apache Software Foundation
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
 * CSS classes, from the default CSS stylesheet, used with {@link org.apache.tapestry5.services.StackTraceElementAnalyzer}.
 *
 * @since 5.1.0.0
 */
public class StackTraceElementClassConstants
{
    /**
     * An omitted frame, because it is not interesting (such as a dynamically generated proxy). Usually invisible.
     */
    public static final String OMITTED = "t-omitted-frame";

    /**
     * Part of the application's code base, and therefore highlighted.
     */
    public static final String USER_CODE = "t-usercode-frame";
}
