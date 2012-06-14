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
 * Describes a transformed class about to be loaded.
 */
public interface PlasticClassEvent
{
    /**
     * The primary class name, i.e., the class being created or transformed.
     */
    String getPrimaryClassName();

    /**
     * The name of the actual class, which will differ from the primary class name
     * for some types.
     */
    String getClassName();

    /**
     * Identifies what type of class is being loaded. Typically, the supporting and method invocation
     * types will <em>precede</em> the primary class.
     */
    ClassType getType();

    /**
     * The bytecode for the class, disassembled and formatted as a string. This is useful
     * for debugging purposes.
     */
    String getDissasembledBytecode();
}
