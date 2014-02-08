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
package org.apache.tapestry5.cdi.test.beans.ws;

import javax.ejb.Stateless;
import javax.jws.WebService;

@Stateless
@WebService(
		serviceName = "HelloWorldService", 
		portName = "HelloWorldPort", 
		endpointInterface = "org.apache.tapestry5.cdi.test.beans.ws.HelloWorldService", 
		targetNamespace = "https://github.com/apache/tapestry-5/")
public class HelloWorldServiceImpl implements HelloWorldService {

    public String sayHello() {
        return "Hello World!";
    }

    public String sayHelloToName(final String name) {
          return "Hello "+name;
    }
}
