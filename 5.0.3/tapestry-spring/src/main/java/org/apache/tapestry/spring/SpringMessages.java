// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.spring;

import java.util.Collection;

import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.MessagesImpl;
import org.springframework.web.context.ContextLoaderListener;

class SpringMessages
{
  private static final Messages MESSAGES = MessagesImpl.forClass(SpringMessages.class);

  static String failureObtainingContext(Throwable cause)
  {
    return MESSAGES.format("failure-obtaining-context", cause);
  }

  static String missingContext()
  {
    return MESSAGES.format("missing-context", ContextLoaderListener.class.getName());
  }

  static String contextStartup(Collection<String> beanNames)
  {
    return MESSAGES.format("context-startup", InternalUtils.joinSorted(beanNames));
  }

  static String beanAccessFailure(String beanName, Class beanType, Throwable cause)
  {
    return MESSAGES.format("bean-access-failure", beanName, beanType.getName(), cause);
  }
}
