// Copyright 2009, 2010 The Apache Software Foundation
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
package org.apache.tapestry5.hibernate.web;

import org.apache.tapestry5.PersistenceConstants;

/**
 * Constants for persistent field strategies.
 * 
 * @see org.apache.tapestry5.annotations.Persist#value()
 * @see PersistenceConstants
 * @since 5.2.0
 */
public class HibernatePersistenceConstants
{
    /**
     * If the field's value is a persistent Hibernate entity its type and primary key is stored in the
     * {@link org.apache.tapestry5.http.services.Session}. Otherwise,
     * the value is stored as per {@link PersistenceConstants#SESSION}.
     */
    public static final String ENTITY = "entity";
}
