package ioc.specs

import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.internal.services.ClasspathScannerImpl
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl
import org.apache.tapestry5.ioc.services.ClasspathMatcher
import org.apache.tapestry5.ioc.util.IdAllocator
import spock.lang.Specification

class ClasspathScannerImplSpec extends Specification {

    // TAP5-2096
    def "can locate classes inside a subpackage, inside an extracted JAR file"() {
        setup:

        ClasspathMatcher matchAll = { packagePage, fileName -> true } as ClasspathMatcher

        def scannerJob = new ClasspathScannerImpl.Job(matchAll, Thread.currentThread().getContextClassLoader(), new ClasspathURLConverterImpl())

        when:
        URL url = Registry.class.getResource('Registry.class');
        scannerJob.scanDir("org/apache/tapestry5/ioc/", new File(url.getPath()).parentFile)
        while (!scannerJob.queue.isEmpty()) {
            def queued = scannerJob.queue.pop();

            queued.run();
        }

        def classes = scannerJob.matches

        then:

        // classes will contain many names, this choice is arbitrary. The point is, it is located on the file system
        // (not in a JAR) and it is in a package somewhere beneath the scanDir.
        classes.contains(IdAllocator.name.replaceAll(/\./, "/") + '.class')
    }


}
