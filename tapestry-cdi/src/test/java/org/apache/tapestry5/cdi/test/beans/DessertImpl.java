// Copyright 2013, 2024 The Apache Software Foundation
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

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Default;


@SessionScoped
@Default
public class DessertImpl implements Dessert{
    private String name = "Ice Cream Sandwich";

    private String secondName = "Jelly Bean";

    public String getName(){
        return name;
    }

    public String getOtherName(){
        return secondName;
    }

    public void setName(String name){
        this.name = name;
    }

    public void changeName(){
        name = secondName;
    }

    public boolean getCheckName(){
        return name.equals(secondName);
    }
}
