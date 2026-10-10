<!--
SPDX-License-Identifier: Apache-2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Benchmarking Tapestry

Tapestry provides a [JMH](https://github.com/openjdk/jmh) Gradle convention so that performance claims can be checked instead of argued about.
They should live in a `src/jmh/java` source set in a module.

Benchmarks are **not** part of `check` or `build`, as JMH might take minutes or even longer and has no place in CI.
`./gradlew jmhCompileAll` compiles every benchmark source set, which is enough to keep them from rotting silently.

## How to Run Benchmarks

```bash
./gradlew :tapestry-core:jmhList                                        # what is there
./gradlew :tapestry-core:jmh                                            # all of it, properly (slow)
./gradlew :commons:jmh -Pjmh.include=TypeCoercer                        # one group
./gradlew :commons:jmh -Pjmh.include=Coercion -Pjmh.quick               # a rough number, fast
./gradlew :tapestry-ioc:jmh -Pjmh.include=Localized -Pjmh.profilers=gc  # with allocation rates
```

Every option is documented at the top of [`buildSrc/src/main/groovy/tapestry.jmh-convention.gradle`](buildSrc/src/main/groovy/tapestry.jmh-convention.gradle).

A benchmark measures the machine it runs on, so the `jmh` tasks hold a single build-service permit and run one at a time, even under a parallel build.
`./gradlew jmh` across several modules therefore takes as long as all of them added up, instead of producing numbers measured while the other modules fought for the same cores.

The two that matter most:

- **`-Pjmh.quick`**\
  One fork, three short warmup and three short measurement iterations.
  Use this while iterating on a change.
  Do not quote its numbers, and **do not** use it on the _cold/startup_ benchmarks!
  Those measure a single shot in a fresh JVM, and forcing three measurement iterations warms two of the three. Shorten those with `-Pjmh.forks` instead.

- **`-Pjmh.profilers=gc`**\
  Allocation rate per operation.
  If an operation runs _too fast_ (e.g., nanoseconds). allocation rate might be the primary concern.

Results are written as JSON to `<module>/build/reports/jmh/results.json`, so a before and after run can be diffed.
THe [JMH Visualizer](https://jmh.morethan.io) helps interpreting results, or your favorite AI can, too.

## What is Measured, and Why

Benchmarks should be grouped by which cost they belong to.

Cold benchmarks run single-shot in fresh JVMs (interpreted, unlinked, JIT-free) because that is genuinely the code path a starting application takes.
Their spread is wide, so read the min and max, not only the mean.

### Startup and page loading

| Benchmark                        | Module        | Question                                                                             |
|----------------------------------|---------------|--------------------------------------------------------------------------------------|
| `RegistryStartupBenchmark`       | tapestry-ioc  | What does building the IoC registry cost, and how much of it is eager instantiation? |
| `PlasticTransformationBenchmark` | plastic       | What does one class transformation cost? One service proxy?                          |
| `PageLoadBenchmark`              | tapestry-core | Registry startup, first page, second page, and warm re-assembly, separated.          |

### Per request

This is where a long-running application spends its time, and where most of the current benchmarks are pointed.
Several of them are really about allocation rather than nanoseconds, so run them with
`-Pjmh.profilers=gc`.

| Benchmark                         | Module        | Question                                                                                              |
|-----------------------------------|---------------|-------------------------------------------------------------------------------------------------------|
| `TypeCoercerBenchmark`            | commons       | How much of a warm coercion is bookkeeping? Does the read lock scale?                                 |
| `StringCoercionBenchmark`         | commons       | What do the coercions a request actually performs cost, per target type?                              |
| `CaseInsensitiveMapBenchmark`     | commons       | What does recomputing a case-insensitive hash per lookup cost?                                        |
| `MessagesBenchmark`               | commons       | What does a message lookup cost, and what does adding arguments add?                                  |
| `ServiceProxyBenchmark`           | tapestry-ioc  | What does the service proxy add over a plain call?                                                    |
| `LocalizedNameGeneratorBenchmark` | tapestry-ioc  | What does a localized resource lookup allocate?                                                       |
| `URLEncoderBenchmark`             | tapestry-core | What does encoding an event context value cost when nothing needs encoding — and when something does? |
| `DocumentRenderBenchmark`         | tapestry-core | Building the DOM against serialising it, over a page-sized tree.                                      |
| `MarkupEscapingBenchmark`         | tapestry-core | Which escaper design in `AbstractMarkupModel` wins, on markup that escapes and markup that does not?  |

### Anonymous classes against lambdas

| Benchmark                    | Module  | Question                                                                                                |
|------------------------------|---------|---------------------------------------------------------------------------------------------------------|
| `CoercionStyleBenchmark`     | commons | The real refactoring, on real code: `BasicTypeCoercions` as written against a lambda translation of it. |
| `AnonymousVsLambdaBenchmark` | commons | The same comparison isolated into invocation, creation and megamorphic dispatch.                        |

The two styles differ in three ways that pull in opposite directions, which is why this is worth
measuring rather than assuming:

- **Allocation.**\
  A non-capturing lambda is linked once and returned as a singleton.
  A non-capturing anonymous class allocates every time it is evaluated.
  A _capturing_ lambda allocates too, so the win exists only where the anonymous class captured nothing.

- **Startup.**\
  An anonymous class is one more class file to find, read, define and verify.
  A lambda is an `invokedynamic` bootstrap that spins a hidden class on first execution.
  That cost lands on exactly the paths that make startup and first page load slow.

- **Steady state.**\
  Both end up as a virtual call on an object. Expected to be a wash.
  But `AnonymousVsLambdaBenchmark.invoke*` actually measures it.

## Writing a new one

Add `id 'tapestry.jmh-convention'` to the module's `plugins` block and put the benchmark in `src/jmh/java`.

Two things worth copying from the existing benchmarks:

- **Say what the benchmark is asking**, not what it calls.
  A class comment that names the hypothesis and the number that would confirm or refute it is what makes a result usable six months later.

- **Give every comparison a control.**
  `AnonymousVsLambdaBenchmark.invokeAnonymous` is only meaningful next to `invokeLambda`.
  `TypeCoercerBenchmark.alreadyAssignable` exists so the other coercion numbers have a floor to be read against.
