// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry.internal.services.AbstractSessionPersistentFieldStrategy;
import org.apache.tapestry.internal.services.PersistentFieldChangeImpl;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.PersistentFieldChange;
import org.apache.tapestry.services.PersistentFieldStrategy;
import org.apache.tapestry.services.Request;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;

/** Persists Hibernate entities by storing their id in the session. */
public class EntityPersistentFieldStrategy implements PersistentFieldStrategy {
    private static final Pattern KEY_PATTERN = Pattern.compile("^([^:]+):([^:]+):(.+)$");
    
	private final PersistentFieldStrategy _strategy;
    private final Session _session;
    private final TypeCoercer _typeCoercer;
    
	public EntityPersistentFieldStrategy(Session session, TypeCoercer typeCoercer, Request request) {
		super();
		_strategy = new EntityStrategy(request);
		_session = session;
		_typeCoercer = typeCoercer;
	}

	public void discardChanges(String pageName) {
		_strategy.discardChanges(pageName);
	}

	public Collection<PersistentFieldChange> gatherFieldChanges(String pageName) {
		Collection<PersistentFieldChange> changes = CollectionFactory.newList();
		for(PersistentFieldChange change : _strategy.gatherFieldChanges(pageName)) {
			if (change.getValue() == null) {
				changes.add(change);
				continue;
			}
			
			String key = change.getValue().toString();
			Matcher matcher = KEY_PATTERN.matcher(key);
			matcher.matches();
			
			String entityName = matcher.group(1);
			String idClassName = matcher.group(2);
			String stringId = matcher.group(3);
				
			try {
				Class<?> idClass = Class.forName(idClassName);
				Object idObj = _typeCoercer.coerce(stringId, idClass);
				
				Serializable id = Defense.cast(idObj, Serializable.class, "id");
				Object entity = _session.get(entityName, id);
				changes.add(new PersistentFieldChangeImpl(change.getComponentId(), change.getFieldName(), entity));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(HibernateMessages.badEntityIdType(entityName, idClassName, stringId), e);
			}
		}
		
		return changes;
	}

	/** Stores the entity id's as values in the form: entityName:idClass:id */
	public void postChange(String pageName, String componentId, String fieldName, Object newValue) {
		if (newValue != null) {
			try {
				String entityName = _session.getEntityName(newValue);
				ClassMetadata metadata = _session.getSessionFactory().getClassMetadata(newValue.getClass());
				Serializable id = metadata.getIdentifier(newValue, _session.getEntityMode());
				newValue = entityName + ":" + id.getClass().getCanonicalName() + ":" + _typeCoercer.coerce(id, String.class);
				
			} catch (HibernateException e) {
				throw new IllegalArgumentException(HibernateMessages.entityNotAttached(newValue), e);
			}
		}
		
		_strategy.postChange(pageName, componentId, fieldName, newValue);
	}

	/** We want to store the data in the session normally, we just need to control the values.
	 * We also need a separate instance so that we know it's using the right prefix for the values.
	*/
	private static final class EntityStrategy extends AbstractSessionPersistentFieldStrategy {

		public EntityStrategy(Request request) {
			super("entity:", request);
		}
		
	}
}
