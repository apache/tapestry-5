// Copyright 2013 The Apache Software Foundation
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
package org.apache.tapestry5.cdi;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.cdi.internal.utils.InternalUtils;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ObjectProvider implementation that handles CDI beans
 * Check first if the bean is managed by tapestry-ioc
 * if so, the bean is ignored.
 *
 */
public class CDIObjectProvider implements ObjectProvider {

	private static Logger logger = LoggerFactory.getLogger(CDIObjectProvider.class); 

	/* (non-Javadoc)
	 * @see org.apache.tapestry5.ioc.ObjectProvider#provide(java.lang.Class, org.apache.tapestry5.ioc.AnnotationProvider, org.apache.tapestry5.ioc.ObjectLocator)
	 */

	public <T> T provide(Class<T> objectType,
			AnnotationProvider annotationProvider, ObjectLocator locator) {
		
		if(InternalUtils.isManagedByTapestry(objectType, annotationProvider, locator)){
			return null;
		}
		
		Annotation[] qualifiers = InternalUtils.getFieldQualifiers(objectType,annotationProvider);
		
		logger.debug("Try to load "+objectType+" - qualifiers ? : "+qualifiers.length);
		
		return BeanHelper.get(objectType, qualifiers);
	}

	
}
