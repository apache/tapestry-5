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

package org.apache.tapestry5.internal.hibernate;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.Test;

@Test
public class DefaultHibernateConfigurerFilterTest
{
    public void testConfigure() throws Exception
    {
        Configuration config = createMock(Configuration.class);
        expect(config.configure()).andReturn(config);

        replay(config);
        new DefaultHibernateConfigurer().configure(config);
        verify(config);
    }
}
