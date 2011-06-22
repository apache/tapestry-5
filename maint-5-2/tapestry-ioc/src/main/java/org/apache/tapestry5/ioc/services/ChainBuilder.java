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

package org.apache.tapestry5.ioc.services;

import java.util.List;

/**
 * A service which can assemble an implementation based on a command interface, and an ordered list of objects
 * implementing that interface (the "commands"). This is an implementation of the Gang of Four Chain Of Command
 * pattern.
 * <p/>
 * For each method in the interface, the chain implementation will call the corresponding method on each command object
 * in turn (with the order defined by the list). If any of the command objects return true, then the chain of command
 * stops and the initial method invocation returns true. Otherwise, the chain of command continues to the next command
 * (and will return false if none of the commands returns true).
 * <p/>
 * For methods whose return type is not boolean, the chain stops with the first non-null (for object types), or non-zero
 * (for numeric types). The chain returns the value that was returned by the command. If the method return type is void,
 * all commands will be invoked.
 * <p/>
 * Method invocations will also be terminated if an exception is thrown.
 */
public interface ChainBuilder
{
    /**
     * Creates a chain instance from a command interface and a list of commands (implementing the interface).
     */
    <T> T build(Class<T> commandInterface, List<T> commands);
}
