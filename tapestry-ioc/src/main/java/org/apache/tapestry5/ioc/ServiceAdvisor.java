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

package org.apache.tapestry5.ioc;

/**
 * Provided by a {@link org.apache.tapestry5.ioc.AdvisorDef} to perform the advice (by invoking methods on a {@link
 * org.apache.tapestry5.ioc.MethodAdviceReciever}).
 *
 * @since 5.1.0.0
 */
public interface ServiceAdvisor
{
    /**
     * Passed the reciever, allows the code (usually a method on a module class) to advice some or all methods.
     *
     * @param methodAdviceReciever
     */
    void advise(MethodAdviceReciever methodAdviceReciever);
}
