// Copyright 2026 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services.assets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.modules.AssetsModule;
import org.apache.tapestry5.services.ClasspathAssetProtectionRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exercises every rule contributed by
 * {@link AssetsModule#contributeClasspathAssetProtectionRule}.
 * Rather than re-implementing the rules, it captures the real contributions and composes them
 * exactly as the service does ("blocked if any rule says so"), so the assertions can never
 * drift from the production logic.
 */
class ClasspathAssetProtectionRuleTest
{
    /**
     * Builds the real, composed protection rule for the given mode. Also asserts that no two
     * contributions share an id (building throws otherwise). Package-visible so the handler test
     * can reuse the real rules instead of a mirror.
     */
    static ClasspathAssetProtectionRule realRule(boolean productionMode)
    {
        final Map<String, ClasspathAssetProtectionRule> rules = new LinkedHashMap<>();

        OrderedConfiguration<ClasspathAssetProtectionRule> configuration =
                new OrderedConfiguration<ClasspathAssetProtectionRule>()
        {
            @Override
            public void add(String id, ClasspathAssetProtectionRule object, String... constraints)
            {
                if (rules.put(id, object) != null)
                {
                    throw new IllegalStateException("Duplicate contribution id: " + id);
                }
            }

            @Override
            public void override(String id, ClasspathAssetProtectionRule object, String... constraints)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addInstance(String id, Class<? extends ClasspathAssetProtectionRule> clazz, String... constraints)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public void overrideInstance(String id, Class<? extends ClasspathAssetProtectionRule> clazz, String... constraints)
            {
                throw new UnsupportedOperationException();
            }
        };

        AssetsModule.contributeClasspathAssetProtectionRule(configuration, productionMode);

        return path -> rules.values().stream().anyMatch(rule -> rule.block(path));
    }

    private static final ClasspathAssetProtectionRule PRODUCTION = realRule(true);

    private static final ClasspathAssetProtectionRule DEVELOPMENT = realRule(false);

    /**
     * Covers all extension rules, the dot-file rule, every exact file-name rule (case-insensitively),
     * and path-separator independence.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            // --- extension rules (BLOCKED_FILE_SUFFIXES) ---
            "com/example/Service.class",
            "com/example/messages.properties",
            "com/example/config.xml",
            "com/example/page.tml",
            "META-INF/MANIFEST.MF",         // .mf, matched case-insensitively
            "certs/server.pem",
            "certs/store.jks",
            "certs/cert.p12",
            "certs/key.p8",
            "certs/key.pkcs8",
            "certs/signature.asc",
            "certs/secret.gpg",
            "certs/secret.pgp",
            "certs/app.keystore",
            "certs/server.key",
            "certs/cert.pfx",
            // --- dot-file rule (must match on the file name, not just the root) ---
            "config/.env",
            "a/b/.gitignore",
            ".npmrc",
            // --- exact file-name rules (BLOCKED_FILE_NAMES), case-insensitive ---
            "build/Dockerfile",
            "build/DOCKERFILE",
            "ssh/id_rsa",
            "ssh/id_ed25519",
            "ssh/id_ecdsa",
            "ID_RSA",
            // --- path-separator independence (backslashes) ---
            "config\\server.pem",
            "build\\Dockerfile",
    })
    void blockedInEveryMode(String path)
    {
        assertTrue(PRODUCTION.block(path), "should be blocked (production mode): " + path);
        assertTrue(DEVELOPMENT.block(path), "should be blocked (development mode): " + path);
    }

    /**
     * Guard against over-blocking: ordinary web assets, and near-misses of the sensitive rules,
     * must still be served. Notably {@code id_rsa.pub} (a public key) is not blocked, because the
     * name rules match exactly rather than by prefix.
     * Extension-less names are no longer blocked by any rule (whether such a path is a directory
     * is decided by the request handler at stream time, not here).
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "assets/app.js",
            "assets/app.min.js",
            "styles/site.css",
            "img/logo.png",
            "fonts/font.woff2",
            "data/config.json",
            "docs/readme.txt",
            "keys/id_rsa.pub",              // public key: exact-match rule must NOT block it
            "build/Dockerfile.template",    // not an exact "dockerfile" match
            "docs/LICENSE",                 // extension-less file: no longer blocked by a rule
            "META-INF/services",            // extension-less name (folder-ness decided at stream time)
            "bin/run",                      // extension-less file
    })
    void allowedInEveryMode(String path)
    {
        assertFalse(PRODUCTION.block(path), "should be served (production mode): " + path);
        assertFalse(DEVELOPMENT.block(path), "should be served (development mode): " + path);
    }

    /**
     * Source maps are the one mode-dependent rule: blocked in production, served in development.
     */
    @Test
    void sourceMapsBlockedOnlyInProduction()
    {
        assertTrue(PRODUCTION.block("assets/app.js.map"), "source map must be blocked in production");
        assertFalse(DEVELOPMENT.block("assets/app.js.map"), "source map should be served in development");
    }

    /**
     * Building the chain throws on a duplicate contribution id, so a successful build is the
     * assertion that all ids are distinct.
     */
    @Test
    void contributionIdsAreUnique()
    {
        assertNotNull(realRule(true));
        assertNotNull(realRule(false));
    }
}
