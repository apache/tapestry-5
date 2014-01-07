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
package org.apache.tapestry5.cdi.test.pages;

import org.apache.tapestry5.cdi.test.annotation.Choco;
import org.apache.tapestry5.cdi.test.annotation.CustomDessert;
import org.apache.tapestry5.cdi.test.annotation.DessertTime;
import org.apache.tapestry5.cdi.test.annotation.Iced;
import org.apache.tapestry5.cdi.test.beans.*;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DessertPage {

    @Inject
    @Iced
    private Dessert dessert1;

    @Choco
    @Inject
    private Dessert dessert2;

    @CustomDessert
    @Inject
    private Dessert dessert3;

    @DessertTime
    @Inject
    private Dessert dessert4;

    @Inject
    private Menu menu;


    public String getQualifier1() {
        if (dessert1 != null) {
            return "dessert1:" + dessert1.getName().equals(new IceCreamImpl().getName());
        } else {
            return "";
        }
    }

    public String getQualifier2() {
        if (dessert1 != null) {
            return "dessert2:" + dessert2.getName().equals(new BrownieImpl().getName());
        } else {
            return "";
        }
    }

    public String getQualifier3() {
        if (dessert3 != null) {
            return "dessert3:" + dessert3.getName().equals(new IceCreamImpl().getOtherName());
        } else {
            return "";
        }
    }

    public String getQualifier4() {
        Calendar today = new GregorianCalendar();
        int hourOfDay = today.get(Calendar.HOUR_OF_DAY);
        if (dessert4 != null) {
            if (hourOfDay < 12) {
                return "dessert4:" + dessert4.getName().equals(new DessertImpl().getName());

            }
            if (hourOfDay == 12) {
                return "dessert4:" + dessert4.getName().equals(new IceCreamImpl().getName());
            } else {
                return "dessert4:" + dessert4.getName().equals(new BrownieImpl().getName());
            }
        }else{
            return "";
        }
    }

    public String getQualifier5(){
       return "dessert5:" + menu.getDessert().equals(new IceCreamImpl().getName());
    }

}
