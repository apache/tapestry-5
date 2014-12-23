package ioc.specs

import org.apache.tapestry5.ioc.Locatable
import org.apache.tapestry5.ioc.internal.BasicTypeCoercions
import org.apache.tapestry5.ioc.internal.services.ClasspathScannerImpl
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl
import org.apache.tapestry5.ioc.services.ClasspathMatcher
import spock.lang.Specification

class ClasspathScannerImplSpec extends Specification {

    // TAP5-2096
    def "can locate classes inside a subpackage, inside an extracted JAR file"() {
        setup:

        ClasspathMatcher matchAll = { packagePage, fileName -> true } as ClasspathMatcher

        def scannerJob = new ClasspathScannerImpl.Job(matchAll, Thread.currentThread().getContextClassLoader(), new ClasspathURLConverterImpl())

        when:
        URL url = Locatable.class.getResource('Locatable.class');
        scannerJob.scanDir("org/apache/tapestry5/ioc/", new File(url.getPath()).parentFile)
        while (!scannerJob.queue.isEmpty()) {
            def queued = scannerJob.queue.pop();

            queued.run();
        }

        def classes = scannerJob.matches

        then:
        classes.contains(BasicTypeCoercions.name.replaceAll(/\./, "/") + '.class')
    }


}
