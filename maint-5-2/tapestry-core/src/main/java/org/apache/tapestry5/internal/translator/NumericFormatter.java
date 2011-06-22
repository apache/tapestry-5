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

package org.apache.tapestry5.internal.translator;

import java.text.ParseException;

/**
 * Interface for defining the basic parse and toClient operations. The typical implementation is based on {@link
 * java.text.NumberFormat} but alternate implementations are used for BigInteger and BigDecimal.
 *
 * @since 5.1.0.1
 */
public interface NumericFormatter
{
    /**
     * Parses a value from the client in a locale-specific way.
     */
    Number parse(String clientValue) throws ParseException;

    /**
     * Formats a value for the client in a locale-specific way.
     */
    String toClient(Number value);
}
