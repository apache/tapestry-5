// Copyright 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.web.internal;

import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.hibernate.web.internal.HibernateEntityValueEncoder;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HibernateEntityValueEncoderTest extends IOCTestCase
{
    private Registry registry;
    private PropertyAccess access;
    private TypeCoercer typeCoercer;

    @BeforeClass
    public void setup()
    {
        registry = buildRegistry();

        access = registry.getService(PropertyAccess.class);
        typeCoercer = registry.getService(TypeCoercer.class);
    }

    @AfterClass
    public void cleanup()
    {
        registry.shutdown();

        registry = null;
        access = null;
        typeCoercer = null;
    }

    @Test
    public void to_client_id_null()
    {
        Session session = mockSession();
        Logger logger = mockLogger();

        replay();

        SampleEntity entity = new SampleEntity();

        HibernateEntityValueEncoder<SampleEntity> encoder = new HibernateEntityValueEncoder<SampleEntity>(
                SampleEntity.class, "id", session, access, typeCoercer, logger);

        assertNull(encoder.toClient(entity));

        verify();
    }

    @Test
    public void to_value_not_found()
    {
        Session session = mockSession();
        Logger logger = mockLogger();

        expect(session.get(SampleEntity.class, new Long(12345))).andReturn(null);

        logger.error("Unable to convert client value '12345' into an entity instance.");

        replay();

        SampleEntity entity = new SampleEntity();

        HibernateEntityValueEncoder<SampleEntity> encoder = new HibernateEntityValueEncoder<SampleEntity>(
                SampleEntity.class, "id", session, access, typeCoercer, logger);

        assertNull(encoder.toValue("12345"));

        verify();
    }

    @Test
    public void to_value_bad_type_coercion()
    {
        Session session = mockSession();
        Logger logger = mockLogger();

        replay();

        HibernateEntityValueEncoder<SampleEntity> encoder = new HibernateEntityValueEncoder<SampleEntity>(
                SampleEntity.class, "id", session, access, typeCoercer, logger);

        try
        {
            encoder.toValue("xyz");
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(
                    ex,
                    "Exception converting 'xyz' to instance of java.lang.Long (id type for entity org.apache.tapestry5.internal.hibernate.SampleEntity)");
        }

        assertNull(encoder.toValue(""));

        verify();
    }

    protected final Session mockSession()
    {
        return newMock(Session.class);
    }
}
