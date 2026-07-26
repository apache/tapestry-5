# buildSrc: Tapestry Build Conventions

Gradle convention plugins and build utilities shared across all subprojects.
All convention files live in `src/main/groovy/` as precompiled script plugins.

---

## Convention hierarchy

```text
tapestry.java-convention                    applied to every subproject by root build.gradle
 └─ tapestry.testing-base-convention        JUnit Platform runner, shared test config
    ├─ tapestry.junit5-convention             + Jupiter API + engine
    │   ├─ tapestry.junit5-spock-convention       + Spock BOM / spock-core
    │   └─ tapestry.junit4-legacy-convention      + Vintage engine (JUnit 4 tests)
    └─ tapestry.testng-convention             + TestNG + EasyMock + testng-engine
```

---

## Conventions

### `tapestry.java-convention`

Applied automatically to every subproject by the root `build.gradle`.

*   Java 11 source/target compatibility
*   `provided` configuration (compile-only scope, like Maven `<scope>provided</scope>`)
*   Dependency version constraints to keep library versions consistent across modules
*   JAR manifest: `Automatic-Module-Name` (JPMS) and `LICENSE`/`NOTICE` in `META-INF`

---

### `tapestry.testing-base-convention`

Foundation for all test conventions.

**Do not apply directly**

Use one of the specialized conventions below.

Configures every `Test` task in the project via `tasks.withType(Test)`:

*   JUnit Platform as the runner (required by all engines: Jupiter, TestNG, Vintage)
*   `junit-platform-launcher` and the JUnit BOM on the runtime classpath
*   Standard system properties: file encoding, locale, CI flag, Selenium wait timeout
*   Consistent test logging: full exception format, pass/skip/fail events, progress counter

Integration tests bind 9090/8443 by default (`@TapestryTestConfiguration`).
To move a single run off those ports, when something else on the machine already holds them:

```bash
./gradlew :tapestry-jpa:testNG -Dtapestry.port=9191 -Dtapestry.ssl-port=8544
```

Gradle's `-D` only reaches the daemon, so the convention forwards these two to the test workers.
`SeleniumTestCase` reads them as a fallback when `testng.xml` declares no such parameter.

### `tapestry.junit5-convention`

Use when the module's test sources use JUnit 5 Jupiter (`@Test`, `@ExtendWith`, ...).

Adds Jupiter API to the test compile classpath and the Jupiter engine at runtime.

```groovy
plugins {
    id 'tapestry.junit5-convention'
}
```

### `tapestry.junit5-spock-convention`

Use when the module writes tests in Spock (Groovy BDD framework).

Extends `junit5-convention` with the Spock BOM and `spock-core`.
Spock runs on the JUnit Platform — no additional runner config needed.

```groovy
plugins {
    id 'tapestry.junit5-spock-convention'
}
```

### `tapestry.junit4-legacy-convention`

Use **only** for modules that still have JUnit 4 test sources (`@RunWith`, `@Rule`, ...).

Extends `junit5-convention` with the JUnit Vintage engine, so legacy tests run alongside any Jupiter tests without touching test source code.
Prefer migrating tests over adding this convention to new modules.

```groovy
plugins {
    id 'tapestry.junit4-legacy-convention'
}
```

### `tapestry.testng-convention`

Use when the module's test sources use TestNG annotations (`@Test`, `@BeforeMethod`, `@DataProvider`, ...).

Adds TestNG and EasyMock to the test compile classpath.
The `testng-engine` dependency at runtime lets the standard `test` task (JUnit Platform) discover and run TestNG unit tests automatically.

```groovy
plugins {
    id 'tapestry.testng-convention'
}
```

#### Modules with Selenium integration tests

When a module has both TestNG unit tests and Selenium integration tests, _native_ TestNG **must** be used for the integration tests!

This is because Selenium tests currently rely on `@BeforeTest` / `ITestContext` scoping that only works correctly with the native TestNG runner and a `testng.xml` suite file.
Each `<test>` element gets its own server instance with the right webapp.

The required additions to the module's `build.gradle`:

```groovy
// Exclude integration classes from the JUnit Platform 'test' task
tasks.named('test') {
    exclude '**/integration/**'   // adjust pattern to match your integration tests
}

// Native TestNG task: uses testng.xml for correct ITestContext grouping
tasks.register('testNG', Test) {
    group = 'verification'

    useTestNG {
        suiteXmlFiles << project.file('src/test/resources/testng.xml')
    }
}

// Include in the check lifecycle
tasks.named('check') {
    dependsOn 'testNG'
}
```

The `testng.xml` suite file for these modules should contain **only** integration `<test>` elements.

Unit tests are handled by the `test` task via testng-engine by Jupiter and must not appear in `testng.xml`.

The task **must** be named `testNG`: the convention gives every task with that name a permit on the
`integrationTestMutex` build service, described below.
A differently named task gets no permit and will race the others for the ports.

#### Serializing integration tests

Every Selenium suite binds the same ports, so no two may run at once.
Rather than disabling parallel builds (not a simple flag on project-level), the convention registers
an `IntegrationTestMutex` build service with `maxParallelUsages = 1` and declares it on each `testNG` task.
Gradle then admits one integration test task at a time while compilation, asset generation and the unit test suites still run in parallel.

---

## Helper classes (`t5build` package)

**`GenerateChecksums`**: custom Gradle task type that generates MD5/SHA-256 checksum
files for release archives.
