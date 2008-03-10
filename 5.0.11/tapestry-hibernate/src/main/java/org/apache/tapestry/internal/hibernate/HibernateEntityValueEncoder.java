// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.hibernate;

import java.io.Serializable;

import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.property.Getter;

public final class HibernateEntityValueEncoder<E> implements ValueEncoder<E> {
	private final Class<E> _entityClass;
	private final PersistentClass _persistentClass;
	private final Session _session;
	private final TypeCoercer _typeCoercer;
	private final Getter _idGetter;
	
	public HibernateEntityValueEncoder(Class<E> entityClass, PersistentClass persistentClass, Session session, TypeCoercer typeCoercer) {
		super();
		_entityClass = entityClass;
		_persistentClass = persistentClass;
		_session = session;
		_typeCoercer = typeCoercer;
		
		Property property = _persistentClass.getIdentifierProperty();
		_idGetter = property.getPropertyAccessor(_entityClass).getGetter(_entityClass, property.getName());
	}
		
	public String toClient(E value) {
		Object id = _idGetter.get(value);
		return _typeCoercer.coerce(id, String.class);
	}

	@SuppressWarnings("unchecked")
	public E toValue(String clientValue) {
		Class<?> idType = _idGetter.getReturnType();
		
		Object id = _typeCoercer.coerce(clientValue, idType);
		Serializable ser = Defense.cast(id, Serializable.class, "id");
		return (E)_session.get(_entityClass, ser);
	}
	
}
