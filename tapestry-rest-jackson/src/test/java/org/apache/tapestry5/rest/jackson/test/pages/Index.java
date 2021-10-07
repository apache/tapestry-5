// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.rest.jackson.test.pages;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.rest.jackson.test.rest.entities.Attribute;
import org.apache.tapestry5.rest.jackson.test.rest.entities.User;
import org.apache.tapestry5.services.HttpStatus;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.util.TextStreamResponse;

public class Index 
{

    private static final Set<User> USERS = new HashSet<>();
    
    @Inject private PageRenderLinkSource pageRenderLinkSource;
    
    @RestInfo(returnType = User.class, produces = "application/json")
    @OnEvent(EventConstants.HTTP_GET)
    public Object getUserByEmail(String email)
    {
        final Optional<User> user = USERS.stream().filter(u -> email.equals(u.getEmail())).findFirst();
        return user.isPresent() ? user.get() : HttpStatus.notFound();
    }
    
    @RestInfo(returnType = int.class, produces = "text/plain")
    @OnEvent(EventConstants.HTTP_GET)
    public Object getUserCount(@StaticActivationContextValue("count") String ignored)
    {
        return new TextStreamResponse("UTF-8", String.valueOf(USERS.size()));
    }
    
    @RestInfo(consumes = "application/json")
    public Object onHttpPut(@RequestBody User user) throws UnsupportedEncodingException
    {
        HttpStatus status;
        if (!USERS.contains(user))
        {
            USERS.add(user);
            status = HttpStatus.created();
        }
        else
        {
            status = HttpStatus.ok();
        }
        return status.withContentLocation(
                pageRenderLinkSource.createPageRenderLinkWithContext(Index.class, user.getEmail()));
    }
    
    @RestInfo(returnType = User.class, produces = "application/json")
    @OnEvent(EventConstants.HTTP_GET)
    public User getExample(@StaticActivationContextValue("example") String example)
    {
        User user = new User();
        user.setEmail("fulano@fulano.com");
        user.setName("Fulano da Silva");
        user.getAttributes().add(new Attribute("favoriteColor", "blue"));
        return user;
    }

}
