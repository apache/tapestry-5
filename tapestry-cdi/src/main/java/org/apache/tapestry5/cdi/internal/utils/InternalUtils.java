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
package org.apache.tapestry5.cdi.internal.utils;

import static org.apache.tapestry5.cdi.BeanHelper.getQualifiers;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utility methods including a method that checks if a bean is managed by tapestry-ioc or not 
 *
 */
public final class InternalUtils {

	private static Logger logger = LoggerFactory.getLogger(InternalUtils.class); 

	@SuppressWarnings("rawtypes")
	private static final Map<Class, Annotation[]> annotationsCache = new HashMap<Class, Annotation[]>();

	private static final List<String> tapestry_injectable_resources = Arrays.asList(
			new String[]{
					ComponentResources.class.getName(),
					ComponentResourceSelector.class.getName(),
					Messages.class.getName(),
					Locale.class.getName(),
					Logger.class.getName(),
					Block.class.getName()
			});

	/**
	 * Check if the injected field is managed by tapestry (resource or service)
	 * @param type the class type
	 * @param annotationProvider used to check the annotation of the class/field
	 * @param locator the objectLocator
	 * @return true if the injected field is managed by Tapestry, false otherwise
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean isManagedByTapestry(final Class type, final AnnotationProvider annotationProvider, final ObjectLocator locator){
		if(type == null) return false;
		/**
		 * We guess that in a tapestry webapp, injected resources are mostly from Tapestry
		 * Is a tapestry resource/service ? 
		 * First, we check if the injected resource is a tapestry one, 
		 * then we check if it has a <i>Symbol</i> or <i>Path</i> annotation and 
		 * finally if it's a tapestry service by asking the locator.
		 */
		try {
			if(	
					tapestry_injectable_resources.contains(type.getName()) 		
					|| isAnnotation(annotationProvider, Symbol.class) 
					|| isAnnotation(annotationProvider, Path.class)
					|| locator.getService(type)!=null
					){
				logger.debug(type+" is a Tapestry resources or service");
				return true;
			}
		} catch (RuntimeException e) {
			// do nothing, the service is not managed by tapestry
			logger.debug(type +" is not a known service of the tapestry registry");
		}
		return false;
	}

	private static <T extends Annotation> boolean isAnnotation(AnnotationProvider annotationProvider, final Class<T> annotation) {
		return annotationProvider.getAnnotation(
				new Annotation(){

					public Class<? extends Annotation> annotationType() {
						return annotation;
					}
				}.annotationType())!=null;
	}

	/**
	 * Returns the field's annotations corresponding to qualifiers for the current PlasticClass
	 * @param type the class type
	 * @param annotationProvider used to check the annotation of the class/field
	 * @return an annotation array
	 */
	@SuppressWarnings("rawtypes")
	public static Annotation[] getFieldQualifiers(final Class type,final AnnotationProvider annotationProvider) {
		logger.debug("Field type : "+type);
		final Annotation[] annotations;
		if (!annotationsCache.containsKey(type)) {
			synchronized (annotationsCache) {
				if (!annotationsCache.containsKey(type)) {
					logger.debug("Put qualifiers in cache for type : "+type);
					annotationsCache.put(type, getQualifiers(type));
				}
			}
		}
		annotations = annotationsCache.get(type);

		List<Annotation> qualifiers = new ArrayList<Annotation>();
		for (Annotation annotation : annotations) {
			boolean isAnnotationPresent = annotationProvider.getAnnotation(annotation.annotationType())!=null;
			logger.debug("Is "+type+" has qualifier : "+annotation.annotationType()+" ? "+isAnnotationPresent);
			if(isAnnotationPresent){
				logger.debug("Qualifier "+annotation+" found for "+type);
				qualifiers.add(annotation);
			}
		}
		return qualifiers.toArray(new Annotation[qualifiers.size()]);
	}

}
