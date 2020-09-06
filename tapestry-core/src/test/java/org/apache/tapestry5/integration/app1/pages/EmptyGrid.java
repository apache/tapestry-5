// Copyright 2014 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class EmptyGrid
{
 
  @Property
  @Persist
  private boolean removeExplicitModel;
  
  @Inject
  private BeanModelSource beanModelSource;
  
  @Inject
  private Messages messages;
  
  private Random random = new Random();
  
  public List getSource()
  {
    return Collections.emptyList();
  }
  
  public BeanModel getModel()
  {
    BeanModel<Object> model = beanModelSource.createDisplayModel(Object.class, messages);
    model.add("random", new PropertyConduit() {
      
      public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
      {
        return null;
      }
      
      public void set(Object instance, Object value)
      {
        throw new UnsupportedOperationException();
      }
      
      public Class getPropertyType()
      {
        return Long.class;
      }

      public Type getPropertyGenericType()
      {
        return Long.class;
      }
      
      public Object get(Object instance)
      {
        return random.nextLong();
      }
    });
    return model;
  }

  void onRemoveModel()
  {
    removeExplicitModel = true;  
  }
  
}
