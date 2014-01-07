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

import javax.inject.Inject;

import org.apache.tapestry5.cdi.test.annotation.Iced;

public class Menu {

    private Dessert dessert;

    @Inject
    void initQuery(@Iced Dessert dessert){

            this.dessert = dessert;

    }

    public String getDessert(){
        if(dessert !=null){
            return dessert.getName();
        }else{
            return "no dessert";
        }

    }
}
