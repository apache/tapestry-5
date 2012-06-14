// Copyright 2011, 2012 The Apache Software Foundation
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

/**
 * Allows a listener to be notified about classes about to be loaded by the manager's class loader.
 * Some classes are loaded by the same class loader but not transformed (for example, inner classes). These
 * do not generate events. In many cases, transforming a class will cause supporting classes (representing
 * {@link MethodInvocation}s, and other things) to be created; these do generate events, and can be distinguished
 * by the {@linkplain PlasticClassEvent#getType() event's type property}.
 * 
 * @see PlasticClassListenerHub
 */
public interface PlasticClassListener
{
    /**
     * Invoked just before a class is to be loaded. Separate events are fired for supporting classes before
     * the event for the primary class (the class being transformed or created from scratch).
     * 
     * @param event
     *            describes the class to be loaded, and gives access to its disassembled
     *            bytecode (for debugging purposes)
     */
    void classWillLoad(PlasticClassEvent event);
}
