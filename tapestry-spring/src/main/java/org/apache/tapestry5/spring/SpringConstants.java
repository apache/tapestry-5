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

package org.apache.tapestry5.spring;

/**
 * Constants used with the Spring integration library.
 *
 * @since 5.1.0.0
 */
public class SpringConstants
{
    /**
     * If true, then Tapestry will attempt to use an externally configured Spring ApplicationContext rather than create
     * its own. This will disable the ability to inject Tapestry IoC services and objects into Spring beans. This
     * <em>must</em> be configured as a conetxt &lt;init-parameter&gt; in web.xml.
     */
    public static final String USE_EXTERNAL_SPRING_CONTEXT = "tapestry.use-external-spring-context";
}
