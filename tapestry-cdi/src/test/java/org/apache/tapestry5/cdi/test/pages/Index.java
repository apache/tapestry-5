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

import javax.inject.Named;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.cdi.test.beans.CounterService;
import org.apache.tapestry5.cdi.test.beans.Dessert;
import org.apache.tapestry5.cdi.test.beans.NamedPojo;
import org.apache.tapestry5.cdi.test.beans.Pojo;
import org.apache.tapestry5.cdi.test.beans.Soup;
import org.apache.tapestry5.cdi.test.beans.StatelessEJBBean;
import org.apache.tapestry5.cdi.test.beans.Stereotyped;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Symbol;


public class Index {
    @javax.inject.Inject
    private Pojo pojo;

    @javax.inject.Inject
    @Named("named")
    private NamedPojo namedPojo;
    

    @javax.inject.Inject
    @Property
    private CounterService counterService;
    

    @javax.inject.Inject
    private Messages messageCDI;
    
    @org.apache.tapestry5.ioc.annotations.Inject
    private Messages messageTapestry;
    
    @javax.inject.Inject
    private ComponentResources resources;
    
    @javax.inject.Inject
    @Symbol(value=SymbolConstants.PRODUCTION_MODE)
    private boolean production_mode;
    
    @javax.inject.Inject
    private StatelessEJBBean statelessBean;

    
    @javax.inject.Inject
    private Soup soup1;
    
    @javax.inject.Inject
    private Soup soup2;

    @javax.inject.Inject
    private Dessert dessert;

    @javax.inject.Inject
    private Stereotyped stereotyped;

        
   
    public String getPojo() {
        return pojo.getName();
    }
    public String getNamedPojo() {
        return namedPojo.getName();
    }
    
    public String getMessageCDI(){
    	return messageCDI.get("messagecdi");
    }
    
    public String getMessageTapestry(){
    	return messageTapestry.get("messagetapestry");
    }
    public String getStatelessEJB(){
    	return statelessBean.helloStatelessEJB();
    }

    public String getRequestScopePojo(){
    	if(soup1 !=null){
    		soup1.changeName();
    		return "request:"+soup1.getName().equals(soup2.getName());
    	}
    	return "";
    }

    public String getSessionScopePojo(){
        if(dessert != null){
            return "session:"+dessert.getName().equals(dessert.getOtherName());
        }
         return "";
     }

    public String getStereotype(){
        if(stereotyped != null){
            return "stereotype:"+stereotyped.getCheckName();
        }
         return "";
     }


    public void onActivate(){
    	counterService.increment();
    }
}
