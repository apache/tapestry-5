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
package org.apache.tapestry5.beanvalidator;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newSet;

import java.util.Set;

import org.apache.tapestry5.json.JSONObject;

/**
 * Describes a single client-side constraint.
 *
 */
public final class ClientConstraintDescriptor
{
   private final Class annotationClass;
   private final String validatorName;
   private final Set<String> attributes;

   public ClientConstraintDescriptor(final Class annotationClass,
         final String validatorName, final String... attributes) 
   {
     this.annotationClass = annotationClass;
     this.validatorName = validatorName;
     this.attributes = newSet(attributes);
   }
   
   /**
    * Returns the annotation describing the constraint declaration.
    */
   public Class getAnnotationClass() 
   {
     return this.annotationClass;
   }

   /**
    * Returns the name of the client-side validator.
    */
   public String getValidatorName() 
   {
     return this.validatorName;
   }

   /**
    * Returns a map containing the annotation attribute names as keys and the annotation attribute values as value.
    * This map is passed to the client-side validator as a {@link JSONObject}.
    */
   public Set<String> getAttributes() 
   {
     return this.attributes;
   }
}