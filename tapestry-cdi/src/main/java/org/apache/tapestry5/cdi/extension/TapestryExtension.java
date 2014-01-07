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
package org.apache.tapestry5.cdi.extension;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.tapestry5.internal.InternalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An spi extension to exclude tapestry resources from CDI management
 * Veto each Tapestry pages, components and mixins to avoid CDI try to manage them
 * Without this extension, CDI will complain about Tapestry services not well loaded in injection points from the webapp's pages,components and mixins 
 */
public class TapestryExtension implements Extension {

	private static Logger logger = LoggerFactory.getLogger(TapestryExtension.class); 

	/**
	 * Exclude Tapestry resources from CDI management
	 * Veto each Tapestry pages, components and mixins
	 * @param pat a ProcessAnnotatedType
	 */
	protected <T> void excludeTapestryResources(@Observes final ProcessAnnotatedType<T> pat){
		String annotatedTypeClassName = pat.getAnnotatedType().getJavaClass().getName(); 
		logger.debug("Annotated type : "+annotatedTypeClassName);
		
		for (String subpackage : InternalConstants.SUBPACKAGES){
			if(annotatedTypeClassName.contains("."+subpackage+".")){
				logger.debug("Tapestry page/component/mixins found! - will be exclude from CDI management : "+annotatedTypeClassName);
				pat.veto();
			}
		}
	}
}
