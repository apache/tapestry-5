// Copyright 2010 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

public class SelectDemo
{

    @Property
    @Persist
    private String color;

    @Property
    @Persist
    private String month;
    
    @Property
    @Persist
    private NumberContainer number;


    @Property
    @Persist(PersistenceConstants.FLASH)
    private SelectModel monthModel;


    void setupRender(){
      monthModel = new SelectModelImpl(
          new OptionModelImpl("January", "January"),
          new OptionModelImpl("February", "February"),
          new OptionModelImpl("March", "March"),
          new OptionModelImpl("April", "April"),
          new OptionModelImpl("May", "May"),
          new OptionModelImpl("June", "June"),
          new OptionModelImpl("July", "July"),
          new OptionModelImpl("August", "August"),
          new OptionModelImpl("Semptember", "Semptember"),
          new OptionModelImpl("October", "October"),
          new OptionModelImpl("November", "November"),
          new OptionModelImpl("December", "December")
          );
    }


    @Property
    @Persist
    private NumberContainer selectedNumberContext;
    
    @InjectComponent
    private Zone zone;

    public List<NumberContainer> getNumberModel(){
      return CollectionFactory.newList(new NumberContainer(1), new NumberContainer(2));
    }

    public ValueEncoder<NumberContainer> getNumberEncoder(){
      return new ValueEncoder<NumberContainer>() {

        @Override
        public String toClient(NumberContainer value) {
          return Integer.toString(value.number);
        }

        @Override
        public NumberContainer toValue(String clientValue) {
           return new NumberContainer(Integer.parseInt(clientValue));
        }
      };
    }

    public NumberContainer getNumberContext(){
      return new NumberContainer(23);
    }

    @OnEvent(value=EventConstants.VALUE_CHANGED, component="number")
    Object onValueChangedFromNumber(NumberContainer number, NumberContainer context){
      selectedNumberContext = context;
      return zone.getBody();
    }
    
    public static final class NumberContainer {
      
      public NumberContainer(int number) {
        this.number = number;
      }
      
      public final int number;

      @Override
      public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj)
          return true;
        if (obj == null)
          return false;
        if (getClass() != obj.getClass())
          return false;
        NumberContainer other = (NumberContainer) obj;
        if (number != other.number)
          return false;
        return true;
      }

      @Override
      public String toString() {
        return Integer.toString(number);
      }
      
    }
}
