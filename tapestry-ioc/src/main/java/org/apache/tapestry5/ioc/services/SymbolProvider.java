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

/**
 * A provider of values for symbols.
 */
public interface SymbolProvider
{
    /**
     * Returns the value for the symbol, or null if this provider can not provide a value. The value itself may contain
     * symbols that will be recursively expanded.
     *
     * @param symbolName
     * @return the value or null
     */
    String valueForSymbol(String symbolName);
}
