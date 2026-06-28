// Copyright 2011, 2012, 2026 The Apache Software Foundation
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
import org.apache.tapestry5.services.security.WhitelistAnalyzer;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * Standard analyzer that places requests from loopback addresses onto the whitelist.
 * Recognized forms include IPv4 (127.x.x.x), IPv6 (::1, 0:0:0:0:0:0:0:1), compressed
 * or with arbitrary scope IDs (e.g. ::1%lo, 0:0:0:0:0:0:0:1%eth0), and IPv4-mapped
 * IPv6 loopback (::ffff:127.0.0.1).
 *
 * @since 5.3
 */
public class LocalhostOnly implements WhitelistAnalyzer
{
    // Common loopback address literals that need no parsing.
    // Adding "localhost" avoids a potential hostname lookup
    // via InetAddress.getByName().
    private static final Set<String> LOOPBACK_LITERALS = Set.of(
        "localhost",
        "127.0.0.1",
        "::1",
        "0:0:0:0:0:0:0:1"
    );

    public boolean isRequestOnWhitelist(Request request)
    {
        String remoteAddr = request.getRemoteAddr();

        // Fastpath
        if (LOOPBACK_LITERALS.contains(remoteAddr))
        {
            return true;
        }

        // Strip IPv6 scope ID (e.g. ::1%lo, 0:0:0:0:0:0:0:1%eth0) before parsing
        int percentIdx = remoteAddr.indexOf('%');
        String addr = (percentIdx >= 0) ? remoteAddr.substring(0, percentIdx) : remoteAddr;

        try
        {
            InetAddress inetAddress = InetAddress.getByName(addr);

            if (inetAddress.isLoopbackAddress())
                return true;

            // InetAddress.isLoopbackAddress() returns false for IPv4-mapped IPv6 loopback
            // (e.g. ::ffff:127.0.0.1). Extract the embedded IPv4 address and check that instead.
            if (inetAddress instanceof Inet6Address)
            {
                byte[] bytes = inetAddress.getAddress();
                if (isIPv4MappedAddress(bytes))
                {
                    InetAddress embedded = InetAddress.getByAddress(new byte[]{ bytes[12], bytes[13], bytes[14], bytes[15] });
                    return embedded.isLoopbackAddress();
                }
            }

            return false;
        }
        catch (UnknownHostException e)
        {
            return false;
        }
    }

    // IPv4-mapped IPv6 addresses have the form ::ffff:a.b.c.d, encoded as 16 bytes:
    //  - bytes  0–9:  all zero
    //  - bytes 10–11: 0xff 0xff  (the "mapped" marker)
    //  - bytes 12–15: the IPv4 address
    private static boolean isIPv4MappedAddress(byte[] bytes)
    {
        if (bytes.length != 16)
        {
            return false;
        }

        // Bytes 0–9 must all be zero
        for (int i = 0; i < 10; i++)
        {
            if (bytes[i] != 0) {
                return false;
            }
        }

        // Bytes 10–11 must be 0xff 0xff
        return bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
    }
}
