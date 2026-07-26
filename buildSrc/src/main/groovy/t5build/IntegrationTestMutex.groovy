package t5build

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * A build service with no behaviour, used purely as a mutex.
 *
 * <p>Selenium integration tests bind fixed ports (9090/8443 by default, see
 * {@code @TapestryTestConfiguration}), so two modules testing at the same time fight over one
 * socket and the loser dies with "Failed to bind".
 * <p>Registering this service with {@code maxParallelUsages = 1} and declaring it on
 * the integration test tasks tells Gradle to allow only one of them at a time, whereas
 * compilation, asset generation and the unit test suites carry on in parallel.
 *
 * @see tapestry.testng-convention.gradle
 */
abstract class IntegrationTestMutex implements BuildService<BuildServiceParameters.None>
{
}