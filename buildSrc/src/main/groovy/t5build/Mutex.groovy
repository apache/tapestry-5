package t5build

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * A build service with no behaviour, used purely as a mutex.
 *
 * <p>Registered with {@code maxParallelUsages = 1} and declared on a task with
 * {@code usesService}, it makes Gradle admit one such task at a time, whereas compilation,
 * asset generation and the unit test suites carry on in parallel.
 *
 * <p>Two kinds of task need this, and they share the single permit:
 *
 * <ul>
 * <li><b>Selenium integration tests</b> bind fixed ports (9090/8443 by default, see
 * {@code @TapestryTestConfiguration}), so two modules testing at the same time fight over one
 * socket and the loser dies with "Failed to bind".</li>
 * <li><b>JMH benchmarks</b> measure the machine they run on. A second benchmark task, or a
 * Selenium suite, running beside one competes for the same cores and makes the numbers
 * meaningless rather than merely slow.</li>
 * </ul>
 *
 * <p>The permit only holds back tasks that ask for it. It cannot keep a compilation or a unit
 * test suite off the machine while a benchmark runs; {@code --max-workers=1} does that.
 *
 * @see tapestry.testng-convention.gradle
 * @see tapestry.jmh-convention.gradle
 */
abstract class Mutex implements BuildService<BuildServiceParameters.None>
{
}
