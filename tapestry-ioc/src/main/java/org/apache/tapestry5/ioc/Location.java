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

package org.apache.tapestry5.ioc;

/**
 * A kind of tag applied to other objects to identify where they came from, in terms of a file (the resource), a line
 * number, and a column number. This is part of "line precise exception reporting", whereby errors at runtime can be
 * tracked backwards to the files from which they were parsed or otherwise constructed.
 */
public interface Location
{
    /**
     * The resource from which the object tagged with a location was derived.
     */
    Resource getResource();

    /**
     * The line number within the resource, if known, or -1 otherwise.
     */
    int getLine();

    /**
     * The column number within the line if known, or -1 otherwise.
     */
    int getColumn();
}
