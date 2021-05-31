# Support for Tapestry injections in Spock specifications
This sub-project, `tapestry-spock`, provides support for Tapestry injections in [Spock](https://spockframework.org/) specifications. Spock is a testing and specification framework for Java and Groovy applications.

## Usage example

```java
 @ImportModule(UniverseModule)
 class UniverseSpec extends Specification {
  
    @Inject
    UniverseService service
 
    UniverseService copy = service
 
    def "service knows the answer to the universe"() {
      expect:
      copy == service        // injection occurred before 'copy' was initialized
      service.answer() == 42 // what else did you expect?!
    }
  }
```

`@ImportModule` indicates which Tapestry module(s) should be started (and subsequently shut down). The deprecated `@SubModule` annotation is still supported for compatibility reasons.

`@Inject` marks fields which should be injected with a Tapestry service or symbol. Related Tapestry annotations, such as `@Service` and `@Symbol`, are also supported.

## Drop-in replacement for spock-tapestry
This software was originally part of the Spock project, known as `spock-tapestry`. The Tapestry project adopted the software as `tapestry-spock` to overcome the backwards incompatibilities introduced by Tapestry 5.7.

`tapestry-spock` works as a drop-in replacement for `spock-tapestry`. So, when upgrading to Tapestry 5.7, use 

`testRuntimeOnly "org.apache.tapestry:tapestry-spock:$tapestryVersion"` (Gradle)

instead of

`testRuntimeOnly "org.spockframework:spock-tapestry:$spockVersion"`.


## References

1. [https://issues.apache.org/jira/browse/TAP5-2668](https://issues.apache.org/jira/browse/TAP5-2668)
2. [https://github.com/spockframework/spock/issues/1312](https://github.com/spockframework/spock/issues/1312)
3. [https://github.com/spockframework/spock/pull/1315](https://github.com/spockframework/spock/pull/1315)
