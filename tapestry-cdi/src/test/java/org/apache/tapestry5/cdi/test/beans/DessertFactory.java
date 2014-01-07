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
package org.apache.tapestry5.cdi.test.beans;

import org.apache.tapestry5.cdi.test.annotation.CustomDessert;
import org.apache.tapestry5.cdi.test.annotation.DessertTime;

import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DessertFactory {

    @Produces
    @CustomDessert
    public Dessert getCustomDessert(){
        Dessert d = new IceCreamImpl();
        d.changeName();
        return d;
    }

    @Produces
    @DessertTime
    public Dessert getGoodDessert(@New DessertImpl dImpl,@New BrownieImpl brownie,@New IceCreamImpl iceCream){
        Calendar today = new GregorianCalendar();
        int hourOfDay = today.get(Calendar.HOUR_OF_DAY);
        if(hourOfDay < 12){
            return dImpl;
        }if(hourOfDay == 12){
            return iceCream;
        }else{
            return brownie;
        }

    }
}
