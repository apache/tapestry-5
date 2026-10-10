// Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.security;

import org.apache.tapestry5.http.services.Request;
import org.easymock.EasyMock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// TAP5-2832
class LocalhostOnlyTest
{
    static Stream<String> whitelist_addresses()
    {
        return Stream.of(
            // Standard loopback
            "localhost",
            "127.0.0.1",

            // Full-form IPv6 loopback
            "0:0:0:0:0:0:0:1",
            "0:0:0:0:0:0:0:1%0",

            // Compressed IPv6 loopback
            "::1",

            // IPv6 loopback with arbitrary scope IDs (OS-dependent: Linux uses %lo, Windows uses numeric IDs)
            "::1%lo",
            "::1%eth0",
            "::1%12",
            "0:0:0:0:0:0:0:1%lo",
            "0:0:0:0:0:0:0:1%eth0",

            // IPv4-mapped IPv6 loopback (dual-stack environments)
            "::ffff:127.0.0.1",
            "::ffff:7f00:1"
        );
    }

    private Request mockRequest(String remoteAddr)
    {
        Request request = EasyMock.mock(Request.class);
        EasyMock.expect(request.getRemoteAddr()).andReturn(remoteAddr);
        EasyMock.replay(request);
        return request;
    }

    @ParameterizedTest
    @MethodSource("whitelist_addresses")
    void localhost_address_is_on_whitelist(String remoteAddr)
    {
        // ARRANGE
        Request request = mockRequest(remoteAddr);

        // ACT
        boolean result = new LocalhostOnly().isRequestOnWhitelist(request);

        // ASSERT
        assertTrue(result, "Expected '" + remoteAddr + "' to be on the whitelist");
    }

    static Stream<String> non_whitelist_addresses()
    {
        return Stream.of(
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "remotehost.example.com",
            "2001:db8::1",
            "fe80::1",
            "::2"
        );
    }

    @ParameterizedTest
    @MethodSource("non_whitelist_addresses")
    void non_localhost_address_is_not_on_whitelist(String remoteAddr)
    {
        // ARRANGE
        Request request = mockRequest(remoteAddr);

        // ACT
        boolean result = new LocalhostOnly().isRequestOnWhitelist(request);

        // ASSERT
        assertFalse(result, "Expected '" + remoteAddr + "' to be NOT on the whitelist");
    }
}
