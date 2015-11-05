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

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;

public class SelectDemo
{

    @Property
    @Persist
    private String color;

    @Property
    @Persist
    private String month;

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

}
