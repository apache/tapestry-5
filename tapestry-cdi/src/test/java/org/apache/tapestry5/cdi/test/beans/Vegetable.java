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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import java.io.Serializable;

@ConversationScoped
public class Vegetable implements Serializable{
    private String name = "salad";
    private String secondName = "tomato";

    @Inject
    private
    javax.enterprise.context.Conversation conversation;

    @PostConstruct
    public void init(){
        if(conversation.isTransient()){
            conversation.begin();
        }
        throw new IllegalStateException();
    }


    public String getName(){
        return name;
    }


    public void changeName(){
        name = secondName;
    }

    public void getEndConversation(){
        if(!conversation.isTransient()){
            conversation.end();
        }
        throw new IllegalStateException();
    }

    public boolean getCheckName(){
        return name.equals(secondName);
    }
    public String getSecondName(){
        return secondName;
    }
}
